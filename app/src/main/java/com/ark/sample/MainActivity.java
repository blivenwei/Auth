package com.ark.sample;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.ark.auth.Auth;
import com.ark.auth.AuthCallback;
import com.ark.auth.UserInfoForThird;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void click_wx_login(View view) {
        Auth.withWX(this)
                .setAction(Auth.Login)
                .build(new AuthCallback() {

                    @Override
                    public void onSuccessForLogin(@NonNull UserInfoForThird info) {
                        super.onSuccessForLogin(info);
                        Log.e("TAG", "onSuccessForLogin: " + info.nickname);
                    }

                    @Override
                    public void onCancel() {
                        super.onCancel();
                    }

                    @Override
                    public void onFailed(@NonNull String code, @NonNull String msg) {
                        super.onFailed(code, msg);
                        Log.e("TAG", "onFailed: " + code + "," + msg);
                    }
                });
    }
}
