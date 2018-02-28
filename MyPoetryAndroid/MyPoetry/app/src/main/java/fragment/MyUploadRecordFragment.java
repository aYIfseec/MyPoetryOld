package fragment;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.mypoetry.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import activity.MainActivity;
import model.MyApplication;
import model.Poetry;
import model.RecordHold;
import model.RecordListHoldView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import service.AudioService;
import utils.MyHttpUtil;
import utils.ParseJSONUtil;

/**
 * Created by Administrator on 2018/1/11.
 */
public class MyUploadRecordFragment extends Fragment {
    private View view;
    private Context context;
    private MyApplication myApplication;
    private OkHttpClient okHttpClient;

    private ListView recordListView;
    private List<RecordHold> recordList;
    private TextView play_count;
    private int currPosition = -1;
    private ImageView currPlayImageView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isPlaying = false;
    private AudioService audioService;
    private int recordId;
    private String poetryTitle;
    private TextView no_data_hint;
    private Handler playHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            currPlayImageView.callOnClick();
        }
    };
    private Handler stopHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //TODO 刷新音频时 停止播放
            if (isPlaying) {
                currPlayImageView.callOnClick();
            }
        }
    };
    private String res;
    private Handler showMsgHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            toast(res);
            if("已删除".equals(res)) {
                String url = MyHttpUtil.getMyUploadRecord(myApplication.getPhoneNumber(), 1);
                Log.e("url", url);
                new GetRecordsTask(recordListView, recordListAdapter).execute(url);
            }
        }
    };

    private void toast(String res) {
        Toast.makeText(getActivity(),res,Toast.LENGTH_SHORT).show();
    }

    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            audioService = ((AudioService.MyBinder) serviceBinder).getService();
            Log.e("audioService", audioService.toString() + "");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            audioService = null;
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.fragment_record_list, null);
        okHttpClient = new OkHttpClient();
        initView();
        myApplication = (MyApplication) getActivity().getApplication();
        Intent intentPlay = new Intent(getActivity(), AudioService.class);
        getActivity().bindService(intentPlay, conn, Context.BIND_AUTO_CREATE);
        return view;
    }

    private void initView() {
        recordListView = (ListView) view.findViewById(R.id.record_list_view);
        no_data_hint = (TextView) view.findViewById(R.id.no_data_hint);
        no_data_hint.setText("收藏夹是空的，快去收藏喜欢的诗词吧！");
        recordListView.setEmptyView(no_data_hint);
        recordListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                recordId  = recordList.get(position).getId();
                poetryTitle = recordList.get(position).getPoetryTitle();
                showDeleteDialog();
                return true;
            }
        });
        recordList = new ArrayList<>();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                String url = MyHttpUtil.getMyUploadRecord(myApplication.getPhoneNumber(), 1);
                new GetRecordsTask(recordListView, recordListAdapter).execute(url);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    class GetRecordsTask extends AsyncTask<String, Void, Void> {

        private ListView rListView;
        private BaseAdapter listAdapter;

        public GetRecordsTask(ListView rListView, BaseAdapter listAdapter) {
            this.rListView = rListView;
            this.listAdapter = listAdapter;
        }

        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected Void doInBackground(String... params) {
            String url = params[0];
            try {
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setConnectTimeout(5000);
                //使用缓存提高处理效率
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                recordList = ParseJSONUtil.jsonStrToMyRecordList(sb.toString());
            } catch (IOException e) {
                e.printStackTrace();

            }
            return null;
        }

        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            //adapter数据更新后通知列表更新
            listAdapter.notifyDataSetChanged();
            rListView.setAdapter(listAdapter);
            stopHandler.sendEmptyMessage(0);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            String url = MyHttpUtil.getMyUploadRecord(myApplication.getPhoneNumber(), 1);
            new GetRecordsTask(recordListView, recordListAdapter).execute(url);
        }
    }

    private void showDeleteDialog() {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());
        deleteDialog.setTitle("请选择:").setMessage("您要删除【" + poetryTitle + "】的录音吗？");
        deleteDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doDelete();
            }
        });
        deleteDialog.show();
    }

    private void doDelete() {
        String url = MyHttpUtil.getDoDeleteUrl(recordId);
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                res = "操作失败";
                showMsgHandler.sendEmptyMessage(0);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                res = response.body().string();
                showMsgHandler.sendEmptyMessage(1);
            }

        });
    }

    private BaseAdapter recordListAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return recordList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            RecordListHoldView holdView = null;

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().from(getActivity()).inflate(R.layout.record_list_item, null);
                holdView = new RecordListHoldView();
                holdView.tv_record_id = (TextView) convertView.findViewById(R.id.tv_record_id);
                holdView.tv_upload_user_name = (TextView) convertView.findViewById(R.id.tv_upload_user_name);
                holdView.tv_upload_time = (TextView) convertView.findViewById(R.id.tv_upload_time);
                holdView.play_upload_voice = (ImageView) convertView.findViewById(R.id.play_upload_voice);
                holdView.voice_record_path = (TextView) convertView.findViewById(R.id.voice_record_path);
                holdView.tv_play_count = (TextView) convertView.findViewById(R.id.tv_play_count);
                holdView.do_praise = (ImageView) convertView.findViewById(R.id.do_praise);
                holdView.tv_praise_count = (TextView) convertView.findViewById(R.id.tv_praise_count);
                convertView.setTag(holdView);
            }
            holdView = (RecordListHoldView) convertView.getTag();
            RecordHold record = recordList.get(position);
            holdView.tv_record_id.setText(record.getId() + "");
            holdView.tv_upload_user_name.setText(record.getPoetryTitle());
            holdView.tv_upload_time.setText(record.getUploadTime());
            holdView.play_upload_voice.setOnClickListener(new InnerOnClickListen(position));
            holdView.play_upload_voice.setTag("play_upload_voice" + position);
            holdView.voice_record_path.setText(record.getRecordPath());
            holdView.tv_play_count.setText("" + record.getPlayCount());
            holdView.tv_play_count.setTag("play_count" + position);
            holdView.do_praise.setOnClickListener(new InnerOnClickListen(position));
            holdView.tv_praise_count.setText(record.getLike() + "");
            holdView.tv_praise_count.setTag("parise_count" + position);
            //convertView.setOnClickListener(new InnerOnClickListen(position));
            return convertView;
        }
    };

    private class InnerOnClickListen implements View.OnClickListener {
        public int pos;

        public InnerOnClickListen(int pos) {
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            RecordHold record = recordList.get(pos);

            switch (v.getId()) {
                case R.id.do_praise:
                    break;
                case R.id.play_upload_voice:
                    if (currPosition != pos && currPlayImageView != null) {//若有其它在放的音频，要先停止
                        currPlayImageView.setImageResource(R.drawable.play);
                        isPlaying = false;
                    }
                    if (isPlaying) {
                        Toast.makeText(context, "停止" + pos, Toast.LENGTH_SHORT).show();
                        isPlaying = false;
                        currPlayImageView.setImageResource(R.drawable.play);
                        currPlayImageView = null;
                        doStopPlay();
                    } else {
                        isPlaying = true;
                        currPosition = pos;
                        Toast.makeText(context, "播放" + pos, Toast.LENGTH_SHORT).show();
                        record.setPlayCount(record.getPlayCount() + 1);
                        play_count = ((TextView) recordListView.findViewWithTag("play_count" + pos));
                        play_count.setText(record.getPlayCount() + "");
                        currPlayImageView = (ImageView) recordListView.findViewWithTag("play_upload_voice" + pos);
                        currPlayImageView.setImageResource(R.drawable.stop);
                        doPlay(recordList.get(pos).getRecordPath(), recordList.get(pos).getId() + "");
                    }
                    break;
            }
        }


    }

    private void doPlay(String recordPath, String recordId) {
        String url = MyHttpUtil.getDoPlayUrl(recordId);
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                //res = "操作失败";
                //handler.sendEmptyMessage(0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                //res = response.body().string();
                //handler.sendEmptyMessage(1);
            }
        });

        url = MyHttpUtil.getPlayNetPath(recordPath);
        audioService.setPlayUrl(url);
        audioService.setHandler(playHandler);
        //TODO
        audioService.play();
    }

    private void doStopPlay() {
        audioService.destoryMediaPlayer();
    }
}