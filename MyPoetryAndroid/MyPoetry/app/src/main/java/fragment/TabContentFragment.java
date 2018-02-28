package fragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Application;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.tts.auth.AuthInfo;
import com.baidu.tts.chainofresponsibility.logger.LoggerProxy;
import com.baidu.tts.client.SpeechError;
import com.baidu.tts.client.SpeechSynthesizer;
import com.baidu.tts.client.SpeechSynthesizerListener;
import com.baidu.tts.client.TtsMode;
import com.example.lenovo.mypoetry.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
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

import static android.content.Context.BIND_AUTO_CREATE;


public class TabContentFragment extends Fragment implements View.OnClickListener {
    private static String TAG = "TabContentFragment";

    private View contentView;
    private MyApplication myApplication;
    private OkHttpClient okHttpClient;
    private Context context;
    private IntentFilter intentFilter;
    private MyBroadcastReceiver myBroadcastReceiver;
    private boolean isSlowShow = true;

    private LinearLayout fastShowView;
    private Poetry poetry;
    private String content = "";
    private String res;

    private TextView tv_title;
    private TextView tv_author;
    private TextView tv_poetry;

    private ImageView playVoice;
    private ImageView view_collect;
    private TextView tv_recorderHint;
    private boolean isPlay = false;

    private ImageView recoder;
    private ImageView bt_stop;
    private ImageView bt_upload;
    private ImageView bt_recordPlay;
    private ImageView bt_cancel;
    private MediaRecorder mediaRecorder;// 录音类
    private File recordDir;
    private File recordFile;// 以文件的形式保存
    private boolean sdCardExit;
    private ProgressDialog progressDialog;
    private AudioService audioService;
    private int currProgress = 0, maxProgress = 100;
    private boolean isEnd = false, isStart = false;

    private Intent intentPlay;
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

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //TODO
            toast(res);
        }
    };

    // 接收百度语音的日志信息
    private Handler mainHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                print(msg.toString());
            }
        }
    };

    private Handler audioHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            progressDialog.setProgress(currProgress);
            if (!isEnd) {
                progressDialog.cancel();
            }
        }
    };

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        intentFilter = new IntentFilter();
        intentFilter.addAction("MyPoetry");

        myBroadcastReceiver = new MyBroadcastReceiver();
        context.registerReceiver(myBroadcastReceiver, intentFilter);

        intentPlay = new Intent(getActivity(), AudioService.class);
        getActivity().bindService(intentPlay, conn, Context.BIND_AUTO_CREATE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        contentView = inflater.inflate(R.layout.fragment_tab_content, null);
        initView();
        myApplication = (MyApplication) getActivity().getApplication();
        okHttpClient = new OkHttpClient();
        isSlowShow = true;

        //初始化 百度语音
        initPermission();
        initTTs();
        return contentView;
    }

    private void initView() {
        fastShowView = (LinearLayout) contentView.findViewById(R.id.poetry_fast_show);
        Typeface typeface = Typeface.createFromAsset(getActivity().getAssets(),"font/font2.ttf");
        tv_title = (TextView) contentView.findViewById(R.id.poetry_title);
        tv_author = (TextView) contentView.findViewById(R.id.poetry_author);
        tv_poetry = (TextView) contentView.findViewById(R.id.poetry_content);

        tv_title.setTypeface(typeface);
        tv_author.setTypeface(typeface);
        tv_poetry.setTypeface(typeface);

        view_collect = (ImageView) contentView.findViewById(R.id.view_collect);
        playVoice = (ImageView) contentView.findViewById(R.id.play_voice);

        recoder = (ImageView) contentView.findViewById(R.id.voice_recorder);
        bt_stop = (ImageView) contentView.findViewById(R.id.voice_recorder_stop);
        bt_cancel = (ImageView) contentView.findViewById(R.id.op_cancel);
        bt_upload = (ImageView) contentView.findViewById(R.id.voice_upload);
        bt_recordPlay = (ImageView) contentView.findViewById(R.id.recoder_play);
        tv_recorderHint = (TextView) contentView.findViewById(R.id.tv_recoder_hint);

        sdCardExit = Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED);
        /* 取得SD Card路径作为录音的文件位置 */
        if (sdCardExit) {
            recordDir = Environment.getExternalStorageDirectory();
        }
        fastShowView.setOnClickListener(this);
        view_collect.setOnClickListener(this);
        recoder.setOnClickListener(this);
        bt_stop.setOnClickListener(this);
        bt_recordPlay.setOnClickListener(this);
        bt_upload.setOnClickListener(this);
        bt_cancel.setOnClickListener(this);
        playVoice.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.poetry_fast_show:
                isSlowShow = false;
                break;
            case R.id.voice_recorder://录音
                startRecorder();
                break;
            case R.id.voice_recorder_stop:
                stopRecorder();
                break;
            case R.id.voice_upload://上传
                uploadRecord(myApplication.getPhoneNumber(), poetry.getId(), poetry.getTitle(), recordFile.getAbsolutePath());
                bt_cancel.callOnClick();
                break;
            case R.id.recoder_play://录音试听
                //showPlayProgreessDialog();
                if (isStart) {
                    isStart = false;
                    bt_recordPlay.setImageResource(R.drawable.play);
                    audioService.destoryMediaPlayer();
                } else {
                    isStart = true;
                    audioService.setPlayUrl(recordFile.getAbsolutePath());
                    audioService.setHandler(new Handler() {
                        @Override
                        public void handleMessage(Message msg) {
                            super.handleMessage(msg);
                            isEnd = true;
                            bt_recordPlay.callOnClick();
                        }
                    });
                    audioService.play();
                    bt_recordPlay.setImageResource(R.drawable.stop);
                }

                break;
            case R.id.op_cancel:
                bt_recordPlay.setVisibility(View.GONE);
                bt_cancel.setVisibility(View.GONE);
                bt_upload.setVisibility(View.GONE);
                recoder.setVisibility(View.VISIBLE);
                break;
            case R.id.play_voice://机读
                if (!isPlay) {
                    isPlay = true;
                    speak();
                } else {
                    isPlay = false;
                    stop();
                }
                break;
            case R.id.view_collect://收藏
                doCollect();
                break;
            default:
                break;
        }
    }

    //录音结束
    private void stopRecorder() {
        if (recordFile != null) {
            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;
            toast("结束录音");
            resetRecord();
            bt_recordPlay.setVisibility(View.VISIBLE);
            bt_cancel.setVisibility(View.VISIBLE);
            bt_upload.setVisibility(View.VISIBLE);

        }
    }

    //录音开始
    private void startRecorder() {
        try {
            if (!sdCardExit) {
                toast("没有SD Card");
                return;
            }

            recoder.setVisibility(View.GONE);
            bt_stop.setVisibility(View.VISIBLE);
            tv_recorderHint.setVisibility(View.VISIBLE);

            recordFile = File.createTempFile(String.valueOf(System.currentTimeMillis()), ".amr", recordDir);
            //toast(recordFile.getAbsolutePath());
            mediaRecorder = new MediaRecorder();
            // 设置声源 麦克风
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            // 设置音频文件的格式
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.DEFAULT);
            // 设置音频文件的编码
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
            mediaRecorder.setOutputFile(recordFile.getAbsolutePath());
            mediaRecorder.prepare();
            mediaRecorder.start();
            toast("开始录音");

        } catch (IOException e) {
            toast("开始录音失败");
            recoder.setVisibility(View.VISIBLE);
            resetRecord();
            e.printStackTrace();
        }
    }

    //试听进度条 删
    public void showPlayProgreessDialog() {
        isEnd = false;
        audioService.setPlayUrl(recordFile.getAbsolutePath());
        Log.e(TAG, "test1");
        progressDialog = new ProgressDialog(getActivity());
        maxProgress = audioService.getMusicDuration() / 1000;
        progressDialog.setMax(maxProgress);
        progressDialog.setProgress(0);
        Log.e(TAG, maxProgress+" test2");
        progressDialog.setTitle("录音试听:");
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                Log.e(TAG, "test3");
                isEnd = true;
                audioService.destoryMediaPlayer();
            }
        });
        progressDialog.show();

        audioService.play();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (currProgress < maxProgress && !isEnd) {
                    try {
                        Thread.sleep(100);
                        currProgress = audioService.getMusicCurrentPosition() / 1000;
                        Log.e(TAG, currProgress + "");
                        audioHandler.sendEmptyMessage(0);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                audioHandler.sendEmptyMessage(0);
            }
        }).start();
    }

    //上传录音
    public void uploadRecord(String userPhone, String poetryId, String title, String filePath) {
        try {
            title = URLEncoder.encode(title,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        File file = new File(filePath);
        RequestBody fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file);
        RequestBody multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM)
                .addPart(Headers.of(
                        "Content-Disposition",
                        "form-data; name=\"phoneNumber\"")
                        , RequestBody.create(null, userPhone))
                .addPart(Headers.of(
                        "Content-Disposition",
                        "form-data; name=\"poetryId\"")
                        , RequestBody.create(null, poetryId))
                .addPart(Headers.of(
                        "Content-Disposition",
                        "form-data; name=\"poetryTitle\"")
                        , RequestBody.create(null, title))
                .addPart(Headers.of(
                        "Content-Disposition",
                        "form-data; name=\"file\"; filename=" + file.getName())
                        , fileBody).build();

        Request request = new Request.Builder().url(MyHttpUtil.UPLOAD_FILE)
                .addHeader("User-Agent", "android")
                .header("Content-Type", "text/html; charset=utf-8;")
                .post(multipartBody)//传参数、文件或者混合，改一下就行请求体就行
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                res = "网络超时";
                handler.sendEmptyMessage(0);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    res = response.body().string();
                    handler.sendEmptyMessage(0);
                }
            }
        });
    }

    private void resetRecord() {
        bt_stop.setVisibility(View.GONE);
        tv_recorderHint.setVisibility(View.GONE);
    }

    //收藏操作
    public void doCollect() {
        String url = MyHttpUtil.getCollectUrl(myApplication.getPhoneNumber(), poetry.getId(), poetry.getTitle());
        //toast(url);
        Request request = new Request.Builder().url(url).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                res = "网络超时";
                handler.sendEmptyMessage(0);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                res = response.body().string();
                handler.sendEmptyMessage(1);
            }
        });
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String msg = intent.getStringExtra("Msg");
            if ("PoetryUpdate".equals(msg)) {
                //接收数据到达广播
                bindDataForView();
            } else if ("UserLogin".equals(msg)) {
                //接收用户登录的广播，改变button为可见
                view_collect.setVisibility(View.VISIBLE);
                recoder.setVisibility(View.VISIBLE);
            }
        }
    }

    private Handler showPoetryHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            tv_poetry.setText(content);
        }
    };

    //展示数据
    private void bindDataForView() {
        content = "";
        isSlowShow = true;
        poetry = myApplication.getCurrPoetry();
        //toast(poetry.getTitle());
        tv_title.setText(poetry.getTitle());
        tv_author.setText(poetry.getAuthor());

        new Thread(new Runnable() {
            @Override
            public void run() {
                String[] contentArr = poetry.getContent();
                for (int i = 0; i < contentArr.length; i++) {
                    for (int j = 0; j < contentArr[i].length(); j++) {
                        if (isSlowShow) {
                            try {
                                Thread.sleep(80);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        content += contentArr[i].charAt(j);
                        if (isSlowShow) {
                            showPoetryHandler.sendEmptyMessage(0);
                        }
                    }
                    if (isSlowShow) {
                        try {
                            Thread.sleep(1200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                if (!isSlowShow) {
                    showPoetryHandler.sendEmptyMessage(0);
                }
            }
        }).start();

    }

    @Override
    public void onDestroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stop();
            mSpeechSynthesizer.release();
            mSpeechSynthesizer = null;
            print("释放资源成功");
        }
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        context.unregisterReceiver(myBroadcastReceiver);
        getActivity().unbindService(conn);
        getActivity().stopService(intentPlay);
    }

    private void toast(String str) {
        Toast.makeText(getActivity(), str, Toast.LENGTH_SHORT).show();
    }


    /**
     * 百度语音api
     */
    protected String appId = "10604986";
    protected String appKey = "wb9WhdIdc5OVHYpogqUIfiXK";
    protected String secretKey = "724c7e2402352450a57512c0fa984b0c";
    // TtsMode.MIX; 离在线融合，在线优先； TtsMode.ONLINE 纯在线； 没有纯离线
    private TtsMode ttsMode = TtsMode.ONLINE;
    private static final String TEMP_DIR = "/sdcard/baiduTTS";

    // 请确保该PATH下有这个文件
    private static final String TEXT_FILENAME = TEMP_DIR + "/" + "bd_etts_text.dat";
    // 请确保该PATH下有这个文件 male是男声 female女声
    private static final String MODEL_FILENAME = TEMP_DIR + "/" + "bd_etts_speech_male.dat";

    protected SpeechSynthesizer mSpeechSynthesizer;

    /**
     * 注意此处为了说明流程，故意在UI线程中调用。
     * 实际集成中，该方法一定在新线程中调用，并且该线程不能结束。具体可以参考NonBlockSyntherizer的写法
     */
    private void initTTs() {
        LoggerProxy.printable(true); // 日志打印在logcat中
        boolean isMix = ttsMode.equals(TtsMode.MIX);
        boolean isSuccess;
        if (isMix) {
            // 检查2个离线资源是否可读
            isSuccess = checkOfflineResources();
            if (!isSuccess) {
                return;
            } else {
                print("离线资源存在并且可读, 目录：" + TEMP_DIR);
            }
        }
        // 日志更新在UI中，可以换成MessageListener，在logcat中查看日志
        SpeechSynthesizerListener listener = new UiMessageListener(mainHandler);

        // 获取实例
        mSpeechSynthesizer = SpeechSynthesizer.getInstance();
        mSpeechSynthesizer.setContext(getContext());

        // 设置listener
        mSpeechSynthesizer.setSpeechSynthesizerListener(listener);

        // 设置appId，appKey.secretKey
        int result = mSpeechSynthesizer.setAppId(appId);
        checkResult(result, "setAppId");
        result = mSpeechSynthesizer.setApiKey(appKey, secretKey);
        checkResult(result, "setApiKey");

        // 支持离线的话，需要设置离线模型
        if (isMix) {
            // 检查离线授权文件是否下载成功，离线授权文件联网时SDK自动下载管理，有效期3年，3年后的最后一个月自动更新。
            isSuccess = checkAuth();
            if (!isSuccess) {
                return;
            }
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
            // 声学模型文件路径 (离线引擎使用)， 注意TEXT_FILENAME必须存在并且可读
            mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
        }

        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEAKER, "0");
        // 设置合成的音量，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_VOLUME, "9");
        // 设置合成的语速，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_SPEED, "5");
        // 设置合成的语调，0-9 ，默认 5
        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_PITCH, "5");

        mSpeechSynthesizer.setParam(SpeechSynthesizer.PARAM_MIX_MODE, SpeechSynthesizer.MIX_MODE_DEFAULT);

        mSpeechSynthesizer.setAudioStreamType(AudioManager.MODE_IN_CALL);

        // x. 额外 ： 自动so文件是否复制正确及上面设置的参数
        Map<String, String> params = new HashMap<>();
        // 复制下上面的 mSpeechSynthesizer.setParam参数
        // 上线时请删除AutoCheck的调用
        if (isMix) {
            params.put(SpeechSynthesizer.PARAM_TTS_TEXT_MODEL_FILE, TEXT_FILENAME);
            params.put(SpeechSynthesizer.PARAM_TTS_SPEECH_MODEL_FILE, MODEL_FILENAME);
        }
        InitConfig initConfig = new InitConfig(appId, appKey, secretKey, ttsMode, params, listener);
        AutoCheck.getInstance(getActivity().getApplicationContext()).check(initConfig, new Handler() {
            @Override
            /**
             * 开新线程检查，成功后回调
             */
            public void handleMessage(Message msg) {
                if (msg.what == 100) {
                    AutoCheck autoCheck = (AutoCheck) msg.obj;
                    synchronized (autoCheck) {
                        String message = autoCheck.obtainDebugMessage();
                        print(message); // 可以用下面一行替代，在logcat中查看代码
                        // Log.w("AutoCheckMessage", message);
                    }
                }
            }

        });

        // 6. 初始化
        result = mSpeechSynthesizer.initTts(ttsMode);
        checkResult(result, "initTts");

    }

    private boolean checkAuth() {
        AuthInfo authInfo = mSpeechSynthesizer.auth(ttsMode);
        if (!authInfo.isSuccess()) {
            // 离线授权需要网站上的应用填写包名。本demo的包名是com.baidu.tts.sample，定义在build.gradle中
            String errorMsg = authInfo.getTtsError().getDetailMessage();
            print("【error】鉴权失败 errorMsg=" + errorMsg);
            return false;
        } else {
            print("验证通过，离线正式授权文件存在。");
            return true;
        }
    }

    private boolean checkOfflineResources() {
        String[] filenames = {TEXT_FILENAME, MODEL_FILENAME};
        for (String path : filenames) {
            File f = new File(path);
            if (!f.canRead()) {
                print("[ERROR] 文件不存在或者不可读取，请从assets目录复制同名文件到：" + path);
                print("[ERROR] 初始化失败！！！");
                return false;
            }
        }
        return true;
    }

    private void speak() {
        if (mSpeechSynthesizer == null) {
            print("[ERROR], 初始化失败");
            return;
        }
        String speakStr = tv_poetry.getText().toString();
        speakStr = speakStr.replaceAll("\\d+", "");
        int result = mSpeechSynthesizer.speak(speakStr);
        print("合成并播放 按钮已经点击");
        checkResult(result, "speak");
    }

    private void stop() {
        print("停止合成引擎 按钮已经点击");
        int result = mSpeechSynthesizer.stop();
        checkResult(result, "stop");
    }

    private void print(String message) {
        Log.i(TAG, message);
        //poetryContent.append(message + "\n");
    }

    private void checkResult(int result, String method) {
        if (result != 0) {
            print("error code :" + result + " method:" + method + ", 错误码文档:http://yuyin.baidu.com/docs/tts/122 ");
        }
    }

    /**
     * android 6.0 以上需要动态申请权限
     */
    private void initPermission() {
        String[] permissions = {
                Manifest.permission.INTERNET,
                Manifest.permission.ACCESS_NETWORK_STATE,
                Manifest.permission.MODIFY_AUDIO_SETTINGS,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_SETTINGS,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.RECORD_AUDIO
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(getContext(), perm)) {//this->getContext()
                toApplyList.add(perm);
                // 进入到这里代表没有权限.
            }
        }
        String[] tmpList = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(getActivity(), toApplyList.toArray(tmpList), 123);//this->getActivity()
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // 此处为android 6.0以上动态授权的回调，用户自行实现。
    }
}