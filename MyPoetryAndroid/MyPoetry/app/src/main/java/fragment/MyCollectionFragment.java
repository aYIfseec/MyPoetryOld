package fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lenovo.mypoetry.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import callback.ListViewItemClickCallBack;
import model.CollectionModel;
import model.MyApplication;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.MyHttpUtil;
import utils.ParseJSONUtil;

/**
 * Created by Administrator on 2018/1/12.
 */

public class MyCollectionFragment extends Fragment {

    private View view;
    private ListView collectionListView;
    private List<CollectionModel> collectionModels;
    private String phoneNumber;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ListViewItemClickCallBack clickCallBack;
    private String cancelCollectId;
    private OkHttpClient okHttpClient;
    private String res;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (res != null && !"".equals(res)) {
                getData();
                toast(res);
            }
        }
    };

    private void toast(String res) {
        Toast.makeText(getActivity(), res, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        clickCallBack = (ListViewItemClickCallBack) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        view = inflater.inflate(R.layout.collection_list, null);
        okHttpClient = new OkHttpClient();
        collectionListView = (ListView) view.findViewById(R.id.collection_list_view);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                getData();
            }
        });
        collectionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String poetry_id = ((TextView)view.findViewById(R.id.tv_get_poetry_id)).getText().toString();
                clickCallBack.sendPoetryId(poetry_id);
            }
        });
        collectionListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String title = ((TextView)view.findViewById(R.id.tv_item_title)).getText().toString();
                String tv_collect_id = ((TextView)view.findViewById(R.id.tv_collect_id)).getText().toString();
                showDeleteDialog(title, tv_collect_id);
                return true;
            }
        });
        phoneNumber = ((MyApplication)getActivity().getApplication()).getPhoneNumber();
        Log.e("TEST",phoneNumber);
        getData();
        return view;
    }

    private void getData() {
        swipeRefreshLayout.setRefreshing(true);

        String url = MyHttpUtil.getCollectionListUrl(phoneNumber, 1);
        new GetCollectionTask(collectionListView, collectionListAdapter).execute(url);
        swipeRefreshLayout.setRefreshing(false);
    }


    private BaseAdapter collectionListAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return collectionModels.size();
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
            ListHold holdView = null;

            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().from(getActivity()).inflate(R.layout.collection_list_item, null);
                holdView = new ListHold();
                holdView.tv_title = (TextView) convertView.findViewById(R.id.tv_item_title);
                holdView.tv_collect_time = (TextView) convertView.findViewById(R.id.tv_collect_time);
                holdView.tv_collect_id = (TextView) convertView.findViewById(R.id.tv_collect_id);
                holdView.tv_poetry_id = (TextView) convertView.findViewById(R.id.tv_get_poetry_id);
            } else {
                holdView = (ListHold) convertView.getTag();
            }
            CollectionModel cm = collectionModels.get(position);
            holdView.tv_title.setText(cm.getPoetryTitle());
            holdView.tv_collect_time.setText(cm.getCollectTime());
            holdView.tv_collect_id.setText(cm.getId()+"");
            holdView.tv_poetry_id.setText(cm.getPoetryId());
            convertView.setTag(holdView);
            return convertView;
        }
    };

    private void showDeleteDialog(String title, String tv_collect_id) {
        cancelCollectId = tv_collect_id;
        //toast(cancelCollectId);
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());
        deleteDialog.setTitle("请选择:").setMessage("您要删除【" + title + "】吗？");
        deleteDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doCancelCollect();
            }
        });
        deleteDialog.show();
    }

    private void doCancelCollect() {
        String url = MyHttpUtil.getCancelCollectUrl(cancelCollectId);
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                res = "操作失败";
                handler.sendEmptyMessage(0);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                res = response.body().string();
                handler.sendEmptyMessage(1);
            }

        });
    }

    class GetCollectionTask extends AsyncTask<String, Void, Void> {

        private ListView listView;
        private BaseAdapter listAdapter;

        public GetCollectionTask(ListView listView, BaseAdapter listAdapter) {
            this.listView = listView;
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
                collectionModels = ParseJSONUtil.jsonStrToCollectionList(sb.toString());
                Log.e("RES",sb.toString());
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
            listView.setAdapter(listAdapter);
        }
    }

    class ListHold{
        protected TextView tv_title;
        protected TextView tv_collect_time;
        protected TextView tv_collect_id;
        protected TextView tv_poetry_id;
    }
}
