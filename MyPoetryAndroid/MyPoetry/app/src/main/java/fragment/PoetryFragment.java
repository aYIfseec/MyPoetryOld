package fragment;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.example.lenovo.mypoetry.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import activity.MainActivity;
import control.InitConfig;
import listener.UiMessageListener;
import model.MyApplication;
import model.Poetry;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import service.AudioService;
import utils.AutoCheck;
import utils.MyHttpUtil;
import utils.ParseJSONUtil;

import static android.content.Context.BIND_AUTO_CREATE;

/**
 * Created by Administrator on 2018/1/7.
 */

public class PoetryFragment extends Fragment {
    private static String TAG = "PoetryFragment";

    private Context context;
    private List<String> tabIndicators;//tab标题
    private List<Fragment> tabFragments;//碎片
    private PoetryFragment.ContentPagerAdapter contentAdapter;

    private MyApplication myApplication;
    private MainActivity mainActivity;
    private Poetry poetry;
    private OkHttpClient okHttpClient;

    private ViewPager viewPager;
    private View viewFragment;
    private TabLayout tabLayout;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            myApplication.setCurrPoetry(poetry);
            Intent i = new Intent("MyPoetry");
            i.putExtra("Msg","PoetryUpdate");
            context.sendBroadcast(i);
        }
    };

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        myApplication = (MyApplication)getActivity().getApplication();
        viewFragment = inflater.inflate(R.layout.fragment_poetry,null);
        viewPager = (ViewPager) viewFragment.findViewById(R.id.vp_content);
        tabLayout = (TabLayout) viewFragment.findViewById(R.id.tl_tab);
        context = getActivity();
        okHttpClient = new OkHttpClient();
        poetry = new Poetry();
        mainActivity = (MainActivity)getActivity();
        String url = getArguments().getString("requestUrl");

        getData(url);
        initTab();

        return viewFragment;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            String requestUrl = mainActivity.getRequestUrl();
            if (requestUrl != null) {
                getData(requestUrl);
                mainActivity.resetRequestUrl();
            }
        }
    }

    private void initTab() {
        tabIndicators = new ArrayList<>();
        tabIndicators.add("原文");
        tabIndicators.add("注释");
        tabIndicators.add("音频");

        tabFragments = new ArrayList<>();
        tabFragments.add(new TabContentFragment());
        tabFragments.add(new NotesFragment());
        tabFragments.add(new RecordListFragment());

        contentAdapter = new ContentPagerAdapter(getFragmentManager());
        viewPager.setAdapter(contentAdapter);
        viewPager.setOffscreenPageLimit(3);

        tabLayout.setTabMode(TabLayout.MODE_FIXED);
        ViewCompat.setElevation(tabLayout,0);
        tabLayout.setupWithViewPager(viewPager);
        for (int i = 0; i < tabIndicators.size(); i++) {
            TabLayout.Tab tabItem = tabLayout.getTabAt(i);
            if (tabItem != null) {
                tabItem.setCustomView(R.layout.tab_item_layout);
                TextView tv = (TextView) tabItem.getCustomView().findViewById(R.id.tv_menu_item);
                tv.setText(tabIndicators.get(i));
            }
        }
        tabLayout.getTabAt(0).getCustomView().setSelected(true);
    }

    public void getData(String requestUrl) {
        Request request = new Request.Builder().url(requestUrl).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String res = response.body().string();
                poetry = ParseJSONUtil.jsonStrToPoetry(res);
                handler.sendEmptyMessage(0);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    // Tab 适配器
    class ContentPagerAdapter extends FragmentPagerAdapter {

        public ContentPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return tabFragments.get(position);
        }

        @Override
        public int getCount() {
            return tabIndicators.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabIndicators.get(position);
        }
    }
}
