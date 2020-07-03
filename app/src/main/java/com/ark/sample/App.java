package com.ark.sample;

import android.app.Application;
import com.ark.auth.Auth;
import com.ark.auth.alipay.AuthBuildForZFB;
import com.ark.auth.qq.AuthBuildForQQ;
import com.ark.auth.unionpay.AuthBuildForYL;
import com.ark.auth.weibo.AuthBuildForWB;
import com.ark.auth.weixin.AuthBuildForWX;

public class App extends Application {

    private static App _instance;

    public static App get() {
        return _instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (_instance == null) {
            _instance = this;
        }
        initialize();
    }

    public void initialize() {
        Auth.init().setQQAppID("")
                .setWXAppID("wx9632a8c1420e03c7")
                .setWXSecret("e44d26c94448ab3b6f593e5d4519255f")
                .setWBAppKey("")
                .setWBRedirectUrl("")
                .setWBScope("")
                .addFactoryForQQ(AuthBuildForQQ.getFactory())
                .addFactoryForWB(AuthBuildForWB.getFactory())
                .addFactoryForWX(AuthBuildForWX.getFactory())
                .addFactoryForYL(AuthBuildForYL.getFactory())
                .addFactoryForZFB(AuthBuildForZFB.getFactory())
                .build();
    }
}
