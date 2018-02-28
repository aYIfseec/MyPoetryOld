package activity;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lenovo.mypoetry.R;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.smssdk.EventHandler;
import cn.smssdk.SMSSDK;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.MyHttpUtil;

public class SignUpActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String APP_KEY = "23a22c6236ffc";//2398980c35106
    private static final String APP_SECRET = "fa97c8f81a40bf5f80a1886be8dfa939";//1cddb50172e1bfe79e749a14ee3644e0
    private String REGEX_MOBILE_SIMPLE =  "[1][358]\\d{9}";
    private static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,20}$";//6-20位字母+数字
    private String nullStr = "", registerRes;
    private OkHttpClient okHttpClient;
    private TimerTask tt;
    private Timer tm;
    private boolean phoneNumCheck = false;
    private boolean passwrodCheck = false;
    private EditText et_username;
    private EditText et_phonenum;
    private Button btn_check;
    private EditText et_checkecode;
    private Button btn_sure;
    private EditText passwrod;
    private EditText re_password;
    private Button do_register;
    private int TIME = 90;//倒计时90s
    public String country="86";//这是中国区号 可以使用getSupportedCountries();获得国家区号
    private String phone;
    private String passwordStr;
    private ProgressDialog waitingDialog;
    private static final int CODE_REPEAT = 1; //重新发送
    Handler hd = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == CODE_REPEAT) {
                btn_check.setEnabled(true);
                btn_sure.setEnabled(true);
                tm.cancel();//取消任务
                tt.cancel();//取消任务
                TIME = 90;//时间重置
                btn_check.setText("重新获取验证码");
            }else if(msg.what == 10) {
                btn_sure.setEnabled(true);
            } else {
                btn_check.setText(TIME + "重新获取验证码");
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            waitingDialog.cancel();
            if ("success".equals(registerRes)) {
                toast("注册成功，快登录试试吧！");
                SignUpActivity.this.finish();
            } else if("reregister".equals(registerRes)) {
                toast("注册失败，此号码已被注册");
            } else {
                toast("网络异常，注册失败！");
            }
        }
    };

    //短信回调
    EventHandler eh = new EventHandler(){
        @Override
        public void afterEvent(int event, int result, Object data) {
            if (result == SMSSDK.RESULT_COMPLETE) {
                if (event == SMSSDK.EVENT_SUBMIT_VERIFICATION_CODE) {
                    phoneNumCheck = true;
                    toast("验证成功");
                }else if (event == SMSSDK.EVENT_GET_VERIFICATION_CODE){       //获取验证码成功
                    toast("请求验证码成功，请稍候");
                }else if (event == SMSSDK.EVENT_GET_SUPPORTED_COUNTRIES){//获取国家区号类表回调
                }
            }else{//错误（包括验证失败）
                //错误码请参照http://wiki.mob.com/android-api-错误码参考
                ((Throwable)data).printStackTrace();
                String str = data.toString();
                hd.sendEmptyMessage(10);
                toast("验证码错误");
            }
        }
    };
    //吐司的一个小方法
    private void toast(final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SignUpActivity.this, str, Toast.LENGTH_SHORT).show();
            }
        });
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_register);
        okHttpClient = new OkHttpClient();
        SMSSDK.initSDK(this, APP_KEY, APP_SECRET);
        SMSSDK.registerEventHandler(eh); //注册短信回调（记得销毁，避免泄露内存）
        initView();
    }
    private void initView() {
        et_username = (EditText) findViewById(R.id.et_username);
        et_phonenum = (EditText) findViewById(R.id.et_phonenum);
        btn_check = (Button) findViewById(R.id.btn_check);
        et_checkecode = (EditText) findViewById(R.id.et_checkecode);
        btn_sure = (Button) findViewById(R.id.btn_sure);
        passwrod = (EditText) findViewById(R.id.edt_password);
        re_password = (EditText) findViewById(R.id.edt_re_password);
        do_register = (Button) findViewById(R.id.btn_do_register);

        btn_check.setOnClickListener(this);
        btn_sure.setOnClickListener(this);
        do_register.setOnClickListener(this);

        passwrod.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {    //失去焦点
                    String text = ((EditText)v).getText().toString();
                    if (!nullStr.equals(text) && !isValidPassword(text)) {
                        Toast.makeText(SignUpActivity.this,R.string.password_novalid, Toast.LENGTH_SHORT).show();
                    } else {
                        passwordStr = text;
                    }
                }
            }
        });

        re_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {    //失去焦点
                    checkPassword();
                }
            }
        });

    }

    private void checkPassword() {
        String text = re_password.getText().toString();
        if (passwordStr != null) {
            if (text != null && !nullStr.equals(text)) {
                if (!passwordStr.equals(text)) {
                    passwrodCheck = false;
                    toast("密码不一致！");
                } else {
                    passwrodCheck = true;
                }
            }
        } else {
            passwrodCheck = false;
            Toast.makeText(SignUpActivity.this,R.string.password_novalid, Toast.LENGTH_SHORT).show();
        }
    }

    public boolean isValidPassword(String password) {
        if (password == null || nullStr.equals(password)) {
            return false;
        }
        return Pattern.matches(REGEX_PASSWORD, password);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_check:
                phone = et_phonenum.getText().toString().trim().replaceAll("/s","");
                if (!TextUtils.isEmpty(phone)) {
                    Pattern pattern = Pattern.compile(REGEX_MOBILE_SIMPLE);
                    Matcher matcher = pattern.matcher(phone);
                    if (matcher.find()) {//匹配手机号格式
                         alterWarning();
                    } else {
                        toast("手机号格式错误");
                    }
                } else {
                    toast("请先输入手机号");
                }
                break;
            case R.id.btn_sure:
                    String code = et_checkecode.getText().toString().replaceAll("/s","");
                    if (!TextUtils.isEmpty(code) && code.length() == 4) {//判断验证码是否为空
                        btn_sure.setEnabled(false);
                        SMSSDK.submitVerificationCode( country,  phone,  code);//验证
                    }else{//如果用户输入的内容为空，提醒用户
                        toast("验证码格式错误！");
                    }
                break;
            case R.id.btn_do_register:
                if (phoneNumCheck) {
                    String text = et_username.getText().toString().trim();
                    if(text == null || nullStr.equals(text)) {
                        toast("您未填写昵称！");
                    } else {
                        // TODO 由于点击按钮时密码框没有失去焦点，导致
                        checkPassword();
                        if (phoneNumCheck) {
                            if (passwrodCheck) {
                                doRegister(text, phone, passwordStr);
                            } else {
                                toast("两次密码不一致！");
                            }
                        } else {
                            toast("请完成手机短信验证！");
                        }
                    }
                } else {
                    toast("请完成手机短息验证！");
                }
                break;
        }
    }

    private void doRegister(String text, String phone, String pswd) {

        waitingDialog = new ProgressDialog(this);
        waitingDialog.setTitle("注册中");
        waitingDialog.setMessage("请稍候...");
        waitingDialog.setIndeterminate(true);
        waitingDialog.setCancelable(false);//不可取消
        waitingDialog.show();

        String url = MyHttpUtil.getRegisterUrl(text, phone, pswd);
        Request request = new Request.Builder().url(url).build();

        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                registerRes = "fail";
                handler.sendEmptyMessage(0);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                registerRes = response.body().string();
                Log.e("registerRes", registerRes);
                handler.sendEmptyMessage(1);
            }

        });
    }

    //弹窗确认下发
    private void alterWarning() {
        // 通过sdk发送短信验证
        AlertDialog.Builder builder = new AlertDialog.Builder(this);  //先得到构造器
        builder.setTitle("提示"); //设置标题
        builder.setMessage("将会发送验证码到" + phone); //设置内容
        builder.setIcon(R.mipmap.ic_launcher);//设置图标，图片id即可
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            //设置确定按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss(); //关闭dialog
                // 通过sdk发送短信验证（请求获取短信验证码，在监听（eh）中返回）
                SMSSDK.getVerificationCode(country, phone);
                phoneNumCheck = false;
                //做倒计时操作
                Toast.makeText(SignUpActivity.this, "请稍候", Toast.LENGTH_SHORT).show();
                btn_check.setEnabled(false);
                btn_sure.setEnabled(true);
                tm = new Timer();
                tt = new TimerTask() {
                    @Override
                    public void run() {
                        hd.sendEmptyMessage(TIME--);
                    }
                };
                tm.schedule(tt,0,1000);
                }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() { //设置取消按钮
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Toast.makeText(SignUpActivity.this, "已取消", Toast.LENGTH_SHORT).show();
            }
        });
        //参数都设置完成了，创建并显示出来
        builder.create().show();
    }

    //销毁短信注册
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销回调接口registerEventHandler必须和unregisterEventHandler配套使用，否则可能造成内存泄漏。
        SMSSDK.unregisterEventHandler(eh);

    }
}
