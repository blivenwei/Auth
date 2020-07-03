package com.ark.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;

public abstract class BaseAuthBuildForYL extends BaseAuthBuild {
    protected String mOrderInfo;
    protected boolean mTest = false;

    protected BaseAuthBuildForYL(Context context) {
        super(context, Auth.WithYL);
    }

    protected abstract Controller getController(Activity activity);


    @Override
    public BaseAuthBuildForYL setAction(@Auth.ActionYL int action) {
        mAction = action;
        return this;
    }

    /**
     * 订单信息为交易流水号，即TN
     */

    public BaseAuthBuildForYL payOrderInfo(String orderInfo) {
        mOrderInfo = orderInfo;
        return this;
    }

    /**
     * 是否是测试环境, 默认false; true: 银联测试环境，该环境中不发生真实交易; false: 银联正式环境
     */

    public BaseAuthBuildForYL payIsTest(boolean test) {
        mTest = test;
        return this;
    }

    public interface Controller {
        void pay();

        void destroy();

        void callback(int requestCode, int resultCode, Intent data);
    }
}