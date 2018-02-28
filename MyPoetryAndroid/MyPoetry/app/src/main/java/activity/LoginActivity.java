package activity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.lenovo.mypoetry.R;

import java.io.IOException;
import java.util.regex.Pattern;

import model.User;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import utils.EncryptUtil;
import utils.MyHttpUtil;
import utils.ParseJSONUtil;

/**
 * Created by Administrator on 2018/1/8.
 */

public class LoginActivity extends AppCompatActivity {

    private static final String REGEX_PHONE_NUM = "[1][358]\\d{9}";
    private static final String REGEX_PASSWORD = "^[a-zA-Z0-9]{6,20}$";//6-20位字母+数字
    private EditText edt_phoneNum, edt_password;
    private Button bt_login, bt_register;
    private String nullStr = "", loginRes;

    private OkHttpClient okHttpClient;
    private ProgressDialog waitingDialog;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            waitingDialog.cancel();
            if (loginRes != null && !nullStr.equals(loginRes)) { //登录成功
                //toast(loginRes);
                Intent intent = new Intent();
                intent.putExtra("user",loginRes);//TODO
                setResult(RESULT_OK, intent); //此处的intent可以用A传过来intent，或者使用新的intent
                finish();
            } else {
                toast(R.string.login_fail);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_login);
        initView();
        okHttpClient = new OkHttpClient();
    }

    private void initView() {
        edt_phoneNum = (EditText) findViewById(R.id.edt_phone_num);
        edt_password = (EditText) findViewById(R.id.edt_password);
        bt_login = (Button) findViewById(R.id.btn_login);
        bt_register = (Button) findViewById(R.id.btn_register);

        edt_phoneNum.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {    //失去焦点
                    String text = ((EditText)v).getText().toString();
                    if (!nullStr.equals(text) && !isValid(text)) {
                        toast(R.string.phone_num_novalid);
                    }
                }
            }
        });
        edt_password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {    //失去焦点
                    String text = ((EditText)v).getText().toString();
                    if (!nullStr.equals(text) && !isValidPassword(text)) {
                        toast(R.string.password_novalid);
                    }
                }
            }
        });
        bt_login.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String phoneNum = edt_phoneNum.getText().toString();
                if (isValid(phoneNum)) {
                    String password = edt_password.getText().toString();
                    if (isValidPassword(password)) {
                        password = EncryptUtil.encrypt(password);
                        waitingDialog = new ProgressDialog(LoginActivity.this);
                        waitingDialog.setTitle("登录中");
                        waitingDialog.setMessage("请稍候...");
                        waitingDialog.setIndeterminate(true);
                        waitingDialog.setCancelable(false);//不可取消
                        waitingDialog.show();
                        doLogin(MyHttpUtil.getLoginUrl(phoneNum, password));
                    } else {
                        toast(R.string.password_novalid);
                    }
                } else {
                    toast(R.string.phone_num_novalid);
                }
            }
        });
        bt_register.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
    }

    private void doLogin(String loginUrl) {
        Request request = new Request.Builder().url(loginUrl).build();
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                loginRes = "网络超时";
                handler.sendEmptyMessage(0);
            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                loginRes = response.body().string();
                handler.sendEmptyMessage(1);
            }
        });
    }

    public boolean isValid(String phoneNum) {
        if (phoneNum == null || nullStr.equals(phoneNum)) {
            return false;
        }
        return Pattern.matches(REGEX_PHONE_NUM, phoneNum);
    }

    public boolean isValidPassword(String password) {
        if (password == null || nullStr.equals(password)) {
            return false;
        }
        return Pattern.matches(REGEX_PASSWORD, password);
    }

    private void toast(int strId) {
        Toast.makeText(LoginActivity.this,strId, Toast.LENGTH_SHORT).show();
    }
    private void toast(String str) {
        Toast.makeText(LoginActivity.this,str, Toast.LENGTH_SHORT).show();
    }
}
