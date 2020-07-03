package com.ark.auth;

import android.content.Context;
import android.text.TextUtils;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.HashSet;

public class Auth {

    public static final int ErrorUninstalled = -9991;       // 错误码，未安装客户端
    public static final int ErrorNotAction = -9992;         // 错误码，未设置Action
    public static final int ErrorParameter = -9993;         // 错误码，参数错误
    public static final int ErrorUnknown = -9994;           // 错误码，未知错误

    public static final int Pay = 100;                      // 微信、支付宝、银联，支付

    public static final int Rouse = 111;
    // 唤起微信、支付宝，目前作为签约支付       微信(无回调) 唤起WebView，

    public static final int Login = 121;                    // 微信、微博、QQ，登录

    public static final int ShareText = 131;                // 微信、微博 分享文本
    public static final int ShareLink = 132;                // 微信、 微博 分享链接
    public static final int ShareImage = 133;               // 微信、微博、QQ 分享图片
    public static final int ShareVideo = 134;               // 微信、微博、QQ 分享视频
    public static final int ShareMusic = 135;               // 微信、QQ 分享音乐
    public static final int ShareProgram = 136;             // 微信、QQ 分享小程序、应用

    public static final int WithWX = 141;                   // 微信 第三方标记
    public static final int WithWB = 142;                   // 微博 第三方标记
    public static final int WithQQ = 143;                   // QQ 第三方标记
    public static final int WithZFB = 144;                  // 支付宝 第三方标记
    public static final int WithYL = 145;                   // 银联 第三方标记

    static HashSet<BaseAuthBuild> mBuilderSet = new HashSet<>();

    private Auth() {
    }

    static void addBuilder(BaseAuthBuild build) {
        mBuilderSet.add(build);
    }

    static void removeBuilder(BaseAuthBuild build) {
        mBuilderSet.remove(build);
    }


    static BaseAuthBuild getBuilder(String sign) {
        if (!TextUtils.isEmpty(sign)) {
            for (BaseAuthBuild build : mBuilderSet) {
                if (sign.equals(build.mSign)) {
                    return build;
                }
            }
        }
        return null;
    }


    public static AuthBuilderInit init() {
        return new AuthBuilderInit();
    }


    public static BaseAuthBuildForQQ withQQ(Context context) {
        return AuthBuilderInit.getInstance().getFactoryForQQ().getAuthBuild(context);
    }


    public static BaseAuthBuildForWB withWB(Context context) {
        return AuthBuilderInit.getInstance().getFactoryForWB().getAuthBuild(context);
    }


    public static BaseAuthBuildForWX withWX(Context context) {
        return AuthBuilderInit.getInstance().getFactoryForWX().getAuthBuild(context);
    }


    public static BaseAuthBuildForYL withYL(Context context) {
        return AuthBuilderInit.getInstance().getFactoryForYL().getAuthBuild(context);
    }


    public static BaseAuthBuildForZFB withZFB(Context context) {
        return AuthBuilderInit.getInstance().getFactoryForZFB().getAuthBuild(context);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionWX {

    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionWB {

    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionQQ {

    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionZFB {

    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface ActionYL {

    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface WithThird {

    }

    public static class AuthBuilderInit {

        private static AuthBuilderInit mInstance;

        private String QQAppID;

        private String WXAppID;
        private String WXSecret;

        private String WBAppKey;
        private String WBRedirectUrl;
        private String WBScope;

        private AuthBuildFactory mFactoryForQQ;
        private AuthBuildFactory mFactoryForWB;
        private AuthBuildFactory mFactoryForWX;
        private AuthBuildFactory mFactoryForYL;
        private AuthBuildFactory mFactoryForZFB;

        AuthBuilderInit() {
        }


        public static AuthBuilderInit getInstance() {
            if (mInstance != null) {
                return mInstance;
            } else {
                throw new NullPointerException("添加依赖配置, 初始化");
            }
        }


        public String getQQAppID() {
            if (TextUtils.isEmpty(QQAppID)) {
                throw new NullPointerException("请配置 QQAppID");
            } else {
                return QQAppID;
            }
        }


        public AuthBuilderInit setQQAppID(String appId) {
            QQAppID = appId;
            return this;
        }


        public String getWXAppID() {
            if (TextUtils.isEmpty(WXAppID)) {
                throw new NullPointerException("请配置 WXAppID");
            } else {
                return WXAppID;
            }
        }

        public AuthBuilderInit setWXAppID(String appID) {
            WXAppID = appID;
            return this;
        }


        public String getWXSecret() {
            if (TextUtils.isEmpty(WXSecret)) {
                throw new NullPointerException("请配置 WXSecret");
            } else {
                return WXSecret;
            }
        }


        public AuthBuilderInit setWXSecret(String secret) {
            WXSecret = secret;
            return this;
        }


        public String getWBAppKey() {
            if (TextUtils.isEmpty(WBAppKey)) {
                throw new NullPointerException("请配置 WBAppKey");
            } else {
                return WBAppKey;
            }
        }


        public AuthBuilderInit setWBAppKey(String key) {
            WBAppKey = key;
            return this;
        }


        public String getWBRedirectUrl() {
            if (TextUtils.isEmpty(WBRedirectUrl)) {
                throw new NullPointerException("请配置 WBRedirectUrl");
            } else {
                return WBRedirectUrl;
            }
        }


        public AuthBuilderInit setWBRedirectUrl(String url) {
            WBRedirectUrl = url;
            return this;
        }


        public String getWBScope() {
            if (TextUtils.isEmpty(WBScope)) {
                throw new NullPointerException("请配置 WBScope");
            } else {
                return WBScope;
            }
        }


        public AuthBuilderInit setWBScope(String scope) {
            WBScope = scope;
            return this;
        }


        private AuthBuildFactory getFactoryForQQ() {
            if (mFactoryForQQ == null) {
                throw new NullPointerException("添加QQ依赖, 并配置初始化");
            } else {
                return mFactoryForQQ;
            }
        }


        private AuthBuildFactory getFactoryForWB() {
            if (mFactoryForWB == null) {
                throw new NullPointerException("添加微博依赖, 并配置初始化");
            } else {
                return mFactoryForWB;
            }
        }


        private AuthBuildFactory getFactoryForWX() {
            if (mFactoryForWX == null) {
                throw new NullPointerException("添加微信依赖, 并配置初始化");
            } else {
                return mFactoryForWX;
            }
        }


        private AuthBuildFactory getFactoryForYL() {
            if (mFactoryForYL == null) {
                throw new NullPointerException("添加银联依赖, 并配置初始化");
            } else {
                return mFactoryForYL;
            }
        }


        private AuthBuildFactory getFactoryForZFB() {
            if (mFactoryForZFB == null) {
                throw new NullPointerException("添加支付宝依赖, 并配置初始化");
            } else {
                return mFactoryForZFB;
            }
        }


        public AuthBuilderInit addFactoryForQQ(AuthBuildFactory factory) {
            mFactoryForQQ = factory;
            return this;
        }


        public AuthBuilderInit addFactoryForWB(AuthBuildFactory factory) {
            mFactoryForWB = factory;
            return this;
        }


        public AuthBuilderInit addFactoryForWX(AuthBuildFactory factory) {
            mFactoryForWX = factory;
            return this;
        }


        public AuthBuilderInit addFactoryForYL(AuthBuildFactory factory) {
            mFactoryForYL = factory;
            return this;
        }


        public AuthBuilderInit addFactoryForZFB(AuthBuildFactory factory) {
            mFactoryForZFB = factory;
            return this;
        }

        public void build() {
            mInstance = this;
        }
    }

    public abstract static class AuthBuildFactory {


        public abstract <T extends BaseAuthBuild> T getAuthBuild(Context context);
    }
}