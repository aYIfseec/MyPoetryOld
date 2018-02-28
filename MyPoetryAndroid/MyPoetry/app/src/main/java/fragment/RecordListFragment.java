package fragment;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
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

public class RecordListFragment  extends Fragment {
    private View view;
    private IntentFilter intentFilter;
    private Context context;
    private MyBroadcastReceiver myBroadcastReceiver;
    private MyApplication myApplication;
    private OkHttpClient okHttpClient;

    private ListView recordListView;
    private List<RecordHold> recordList;
    private Poetry poetry;
    private TextView play_count, parise_count, no_data_hint;
    private int currPosition = -1;
    private ImageView currPlayImageView;
    private SwipeRefreshLayout swipeRefreshLayout;
    private boolean isPlaying = false;
    private boolean isLogin;
    private AudioService audioService;
    private Handler playHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            currPlayImageView.callOnClick();
        }
    };

    private Handler stopHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //TODO 刷新音频时 停止播放
            if (isPlaying) {
                currPlayImageView.callOnClick();
            }
        }
    };

    private Intent intentPlay;
    private ServiceConnection conn = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder serviceBinder) {
            audioService = ((AudioService.MyBinder) serviceBinder).getService();
            Log.e("audioService", audioService.toString()+"");
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
        view = inflater.inflate(R.layout.fragment_record_list,null);
        okHttpClient = new OkHttpClient();
        initView();
        myApplication = (MyApplication) getActivity().getApplication();
        if (myApplication.getUser() == null) {
            isLogin = false;
        } else {
            isLogin = true;
        }
//        audioService = ((MainActivity)getActivity()).getAudioService();
        intentPlay = new Intent(getActivity(), AudioService.class);
        getActivity().bindService(intentPlay, conn, Context.BIND_AUTO_CREATE);
        return view;
    }

    private void initView() {
        recordListView = (ListView) view.findViewById(R.id.record_list_view);
        no_data_hint = (TextView) view.findViewById(R.id.no_data_hint);
        no_data_hint.setText("还没有人朗读这首诗词，快去上传你的朗读音频吧！");
        recordListView.setEmptyView(no_data_hint);
        recordList = new ArrayList<>();
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(true);
                String url = MyHttpUtil.getRecordListUrl(poetry.getId(), 1);
                new RecordListFragment.GetRecordsTask(recordListView, recordListAdapter).execute(url);
                swipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            String url = MyHttpUtil.getRecordListUrl(poetry.getId(), 1);
            new RecordListFragment.GetRecordsTask(recordListView, recordListAdapter).execute(url);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        intentFilter = new IntentFilter();
        intentFilter.addAction("MyPoetry");
        myBroadcastReceiver = new MyBroadcastReceiver();
        context.registerReceiver(myBroadcastReceiver, intentFilter);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context.unregisterReceiver(myBroadcastReceiver);
        getActivity().unbindService(conn);
        getActivity().stopService(intentPlay);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("Msg");
            if ("PoetryUpdate".equals(msg)) {
                poetry = myApplication.getCurrPoetry();
                String url = MyHttpUtil.getRecordListUrl(poetry.getId(), 1);
                Log.e("url", url);
                new RecordListFragment.GetRecordsTask(recordListView, recordListAdapter).execute(url);
            } else if ("UserLogin".equals(msg)){
                myApplication = (MyApplication) getActivity().getApplication();
                if (myApplication.getUser() == null) {
                    isLogin = false;
                } else {
                    isLogin = true;
                }
            }
        }
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
                HttpURLConnection conn = (HttpURLConnection)new URL(url).openConnection();
                conn.setConnectTimeout(5000);
                //使用缓存提高处理效率
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                String line = null;
                StringBuilder sb = new StringBuilder();
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                recordList = ParseJSONUtil.jsonStrToRecordList(sb.toString());
                //Log.e("recordList", sb.toString());
                //
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
            holdView.tv_record_id.setText(record.getId()+"");
            holdView.tv_upload_user_name.setText(record.getName());
            holdView.tv_upload_time.setText(record.getUploadTime());
            holdView.play_upload_voice.setOnClickListener(new InnerOnClickListen(position));
            holdView.play_upload_voice.setTag("play_upload_voice"+position);
            holdView.voice_record_path.setText(record.getRecordPath());
            holdView.tv_play_count.setText(""+record.getPlayCount());
            holdView.tv_play_count.setTag("play_count"+position);
            holdView.do_praise.setOnClickListener(new InnerOnClickListen(position));
            holdView.tv_praise_count.setText(record.getLike()+"");
            holdView.tv_praise_count.setTag("parise_count"+position);
            return convertView;
        }
    };

    private class InnerOnClickListen implements View.OnClickListener{
        public int pos;

        public InnerOnClickListen(int pos) {
            this.pos = pos;
        }

        @Override
        public void onClick(View v) {
            RecordHold record = recordList.get(pos);

            switch (v.getId())  {
                case R.id.do_praise:
                    if (isLogin) {
                        record.setLike(record.getLike() + 1);
                        parise_count = ((TextView) recordListView.findViewWithTag("parise_count" + pos));
                        parise_count.setText(record.getLike() + "");
                        doParise(record.getId() + "");
                    } else {
                        Toast.makeText(getActivity(),"登录后才能点赞", Toast.LENGTH_SHORT).show();
                    }
                break;
                case R.id.play_upload_voice:
                    if (currPosition != pos && currPlayImageView != null) {//若有其它在放的音频，要先停止
                        currPlayImageView.setImageResource(R.drawable.play);
                        isPlaying = false;
                    }
                    if (isPlaying) {
                            //Toast.makeText(context, "停止" + pos, Toast.LENGTH_SHORT).show();
                            isPlaying = false;
                            currPlayImageView.setImageResource(R.drawable.play);
                            currPlayImageView = null;
                            doStopPlay();
                    } else {
                            isPlaying = true;
                            currPosition = pos;
                            //Toast.makeText(context, "播放" + pos, Toast.LENGTH_SHORT).show();
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
        audioService.play();
    }

    private void doStopPlay() {
        audioService.destoryMediaPlayer();
    }

    private void doParise(String recordId) {
        String url = MyHttpUtil.getDoPariseUrl(recordId);
        //toast(url);
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
    }
}

