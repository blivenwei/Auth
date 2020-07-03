package com.ark.auth;

import android.content.Context;

public abstract class BaseAuthBuild {
    protected int mAction = Auth.ErrorNotAction;                          // 事件
    protected int mWith;                                                  // 第三方标记
    protected String mSign;                                               // 任务标记
    protected Context mContext;                                           // 上下文
    protected AuthCallback mCallback;                                     // 回调函数

    public BaseAuthBuild(Context context, @Auth.WithThird int with) {
        mContext = context;
        mWith = with;
        mSign = String.valueOf(System.currentTimeMillis());
        init();
    }

    protected abstract void init();

    protected void destroy() {
        Auth.removeBuilder(this);
        mContext = null;
        mCallback = null;
    }

    public abstract BaseAuthBuild setAction(int action);

    public void build(AuthCallback callback) {
        if (callback == null) {
            destroy();
            throw new NullPointerException("AuthCallback is null");
        } else if (mContext == null) {
            destroy();
            callback.onFailed(String.valueOf(Auth.ErrorParameter), "Context is null");
        } else if (mAction == Auth.ErrorNotAction) {
            callback.onFailed(String.valueOf(Auth.ErrorParameter), "未设置 Action, 请调用 setAction(action)");
            destroy();
        } else {
            mCallback = callback;
            mCallback.setWith(mWith, mAction);
            mCallback.onStart();
            Auth.addBuilder(this);
        }
    }
}