package com.ark.auth;

import android.app.Activity;
import android.content.Context;

public abstract class BaseAuthBuildForZFB extends BaseAuthBuild {
    protected boolean isShowLoading = true;
    protected String mOrderInfo;
    protected String mUri;

    protected BaseAuthBuildForZFB(Context context) {
        super(context, Auth.WithZFB);
    }


    @Override
    public BaseAuthBuildForZFB setAction(@Auth.ActionZFB int action) {
        mAction = action;
        return this;
    }


    public BaseAuthBuildForZFB payOrderInfo(String orderInfo) {
        mOrderInfo = orderInfo;
        return this;
    }


    public BaseAuthBuildForZFB payIsShowLoading(boolean isShow) {
        isShowLoading = isShow;
        return this;
    }


    public BaseAuthBuildForZFB rouseWeb(String uri) {
        mUri = uri;
        return this;
    }

    protected abstract void pay(Activity activity);
}