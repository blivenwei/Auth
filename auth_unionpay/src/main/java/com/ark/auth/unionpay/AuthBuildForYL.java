package com.ark.auth.unionpay;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import com.ark.auth.*;
import com.unionpay.UPPayAssistEx;

public class AuthBuildForYL extends BaseAuthBuildForYL {

    private AuthBuildForYL(Context context) {
        super(context);
    }

    public static Auth.AuthBuildFactory getFactory() {
        return new Auth.AuthBuildFactory() {
            @Override
            public <T extends BaseAuthBuild> T getAuthBuild(Context context) {
                //noinspection unchecked
                return (T) new AuthBuildForYL(context);
            }
        };
    }

    @Override
    protected BaseAuthBuildForYL.Controller getController(Activity activity) {
        return new Controller(this, activity);
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
            default:
                mCallback.onFailed(String.valueOf(Auth.ErrorParameter), "银联暂未支持的 Action");
                destroy();
        }
    }

    private void pay(Activity activity) {
        if (TextUtils.isEmpty(mOrderInfo)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加 OrderInfo, 使用 payOrderInfo(info) ");
            activity.finish();
        } else {
            int i;
            if (mTest) {                                            // 银联测试环境
                i = UPPayAssistEx.startPay(activity, null, null, mOrderInfo, "01");
            } else {                                                // 银联正式环境
                i = UPPayAssistEx.startPay(activity, null, null, mOrderInfo, "00");
            }
            if (UPPayAssistEx.PLUGIN_VALID == i) {                  // 该终端已经安装控件，并启动控件

            } else if (UPPayAssistEx.PLUGIN_NOT_FOUND == i) {       // 手机终端尚未安装支付控件，需要先安装支付控件
                mCallback.onFailed(String.valueOf(Auth.ErrorUninstalled), "请安装银联支付控件 ");
                activity.finish();
            }
        }
    }

    static class Controller implements BaseAuthBuildForYL.Controller {
        private AuthBuildForYL mBuild;
        private Activity mActivity;

        Controller(AuthBuildForYL build, Activity activity) {
            mBuild = build;
            mActivity = activity;
        }

        @Override
        public void pay() {
            mBuild.pay(mActivity);
        }

        @Override
        public void destroy() {
            if (mActivity != null) {
                mActivity.finish();
                mActivity = null;
            }
            if (mBuild != null) {
                mBuild.destroy();
                mBuild = null;
            }
        }

        @Override
        public void callback(int requestCode, int resultCode, Intent data) {
            if (data != null && data.getExtras() != null) {
                String str = data.getExtras().getString("pay_result");
                if ("success".equalsIgnoreCase(str)) {
                    mBuild.mCallback.onSuccessForPay(String.valueOf(resultCode), "银联支付成功");
                } else if ("fail".equalsIgnoreCase(str)) {
                    mBuild.mCallback.onFailed(String.valueOf(resultCode), "银联支付失败");
                } else if ("cancel".equalsIgnoreCase(str)) {
                    mBuild.mCallback.onCancel();
                }
            }
            destroy();
        }
    }
}