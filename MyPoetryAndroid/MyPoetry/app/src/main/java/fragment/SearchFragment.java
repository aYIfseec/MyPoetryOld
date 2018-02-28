package fragment;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.example.lenovo.mypoetry.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import callback.ListViewItemClickCallBack;
import model.Poetry;
import utils.MyHttpUtil;
import utils.ParseJSONUtil;

/**
 * Created by Administrator on 2018/1/7.
 */

public class SearchFragment extends Fragment {

    private List<Poetry> poetryList;
    private ListView listView;
    private TextView no_search_data;
    private Context context;
    private String keyword;
    private ListViewItemClickCallBack clickCallBack;

    private BaseAdapter poetryListAdapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return poetryList.size();
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
            HoldView holdView = null;
            if (convertView == null) {
                convertView = getActivity().getLayoutInflater().from(getActivity()).inflate(R.layout.poetry_list_item, null);
                holdView = new HoldView();
                holdView.tv_title = (TextView) convertView.findViewById(R.id.poetry_list_title);
                holdView.tv_id = (TextView) convertView.findViewById(R.id.poetry_list_id);
            } else {
                holdView = (HoldView) convertView.getTag();
            }
            Poetry poetry = poetryList.get(position);
            holdView.tv_title.setText(poetry.getTitle());
            holdView.tv_id.setText(poetry.getId());
            convertView.setTag(holdView);
            return convertView;
        }

    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View contentView = inflater.inflate(R.layout.fragment_search, null);
        listView = (ListView) contentView.findViewById(R.id.poetry_list_view);
        //listView.setAdapter(poetryListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                clickCallBack.sendPoetryId(((TextView)view.findViewById(R.id.poetry_list_id)).getText().toString());
            }
        });
        keyword = getArguments().getString("keyword");
        if (keyword != null && !keyword.equals("")) {
            String searchUrl = MyHttpUtil.getValidUrl(keyword,1,20);
            Log.e("url", searchUrl);
            new SearchPoetryTask(listView, poetryListAdapter).execute(searchUrl);
        }
        return contentView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        clickCallBack = (ListViewItemClickCallBack) getActivity();
    }

    class SearchPoetryTask extends AsyncTask<String, Void, Void> {

        private ListView poetryListView;
        private BaseAdapter listAdapter;
        private String searchResponse;

        public SearchPoetryTask(ListView poetryListView, BaseAdapter listAdapter) {
            this.poetryListView = poetryListView;
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
                poetryList = ParseJSONUtil.jsonStrToPoetryList(sb.toString());
                Log.e("search", "searchResponse = " + sb.toString());
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
            poetryListView.setAdapter(listAdapter);
            poetryListView.setEmptyView(no_search_data);
        }
    }
}
class HoldView{
    protected TextView tv_title;
    protected TextView tv_id;
}

