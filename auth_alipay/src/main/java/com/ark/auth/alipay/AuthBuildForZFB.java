package com.ark.auth.alipay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.alipay.sdk.app.PayTask;
import com.ark.auth.*;

import java.util.Map;

@SuppressWarnings("unused")
public class AuthBuildForZFB extends BaseAuthBuildForZFB {

    private AuthBuildForZFB(Context context) {
        super(context);
    }

    public static Auth.AuthBuildFactory getFactory() {
        return new Auth.AuthBuildFactory() {

            @Override
            public <T extends BaseAuthBuild> T getAuthBuild(Context context) {
                //noinspection unchecked
                return (T) new AuthBuildForZFB(context);
            }
        };
    }

    /**
     * 获取 SDK 版本号
     */
    public static String getSDKVersion(AuthActivity activity) {
        PayTask payTask = new PayTask(activity);
        return payTask.getVersion();
    }

    @Override
    protected void init() {
    }

    @Override
    protected void destroy() {
        super.destroy();
    }

    @Override
    public void build(AuthCallback callback) {
        super.build(callback);
        switch (mAction) {
            case Auth.Pay:
                Intent intent = new Intent(mContext, AuthActivity.class);
                intent.putExtra("Sign", mSign);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                mContext.startActivity(intent);
                break;
            case Auth.Rouse:
                // 判断是否安装支付宝客户端(因为走SDK时，如果未安装支付宝，SDK会打开一个支付宝登录界面，但是走URI的不会这样做，所以在这里判断是否安装客户端并给予用户提示)
                if (!Utils.isAppInstalled(mContext, "com.eg.android.AlipayGphone")) {
                    mCallback.onFailed(String.valueOf(Auth.ErrorUninstalled), "请安装支付宝客户端！");
                } else if (TextUtils.isEmpty(mUri)) {
                    mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                            "必须添加 uri, 调用 rouseWeb(uri)");
                } else {
                    try {
                        Intent intent2 = new Intent(Intent.ACTION_VIEW, Uri.parse(mUri));
                        intent2.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        mContext.startActivity(intent2);
                        AliRouseActivity.mCallback = callback;
                    } catch (Exception e) {
                        mCallback.onFailed(String.valueOf(Auth.ErrorUnknown), e.getMessage());
                    }
                }
                destroy();
                break;
            default:
                mCallback.onFailed(String.valueOf(Auth.ErrorParameter), "支付宝暂未支持的 Action");
                destroy();
        }
    }

    @Override
    protected void pay(Activity activity) {
        if (TextUtils.isEmpty(mOrderInfo)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加 OrderInfo, 使用 payOrderInfo(info) ");
            activity.finish();
        } else {
            new AuthBuildForZFB.Pay(activity, mCallback, isShowLoading).execute(mOrderInfo);
        }
        destroy();
    }

    private static class Pay extends AsyncTask<String, Void, Map<String, String>> {

        boolean isShowLoading;
        private AuthCallback callback;                                      // 回调函数
        private Activity activity;

        Pay(Activity activity, AuthCallback callback, boolean isShow) {
            this.callback = callback;
            this.activity = activity;
            this.isShowLoading = isShow;
        }

        @Override
        protected Map<String, String> doInBackground(String... strings) {
            try {
                PayTask pay = new PayTask(activity);
                return pay.payV2(strings[0], isShowLoading);
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Map<String, String> resultMap) {
            super.onPostExecute(resultMap);
            if (resultMap != null) {
                String memo = null;
                String result = null;
                String resultStatus = null;

                for (String key : resultMap.keySet()) {
                    if (TextUtils.equals(key, "resultStatus")) {
                        resultStatus = resultMap.get(key);
                    } else if (TextUtils.equals(key, "result")) {
                        result = resultMap.get(key);
                    } else if (TextUtils.equals(key, "memo")) {
                        memo = resultMap.get(key);
                    }
                }
                // 对于支付结果，请商户依赖服务端的异步通知结果。同步通知结果，仅作为支付结束的通知。
                // 判断 resultStatus 为 9000 则代表支付成功
                if (TextUtils
                        .equals(resultStatus, "9000")) {               // 该笔订单是否真实支付成功，需要依赖服务端的异步通知
                    callback.onSuccessForPay(resultStatus, resultMap.toString());
                } else if (TextUtils.equals(resultStatus, "6001")) {
                    callback.onCancel();
                } else {                                                       // 判断resultStatus 为非“9000”则代表可能支付失败, 该笔订单真实的支付结果，需要依赖服务端的异步通知
                    if (TextUtils.equals(resultStatus,
                            "8000")) {           // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        callback.onSuccessForPay(resultStatus,
                                resultMap.toString());        // 默认为支付成功
                    } else {                                                   // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                        callback.onFailed(resultStatus, "支付宝支付失败");
                    }
                }
            } else {
                callback.onFailed(String.valueOf(Auth.ErrorUnknown), "支付宝支付失败");
            }
            activity.finish();
            callback = null;
            activity = null;
        }
    }
}