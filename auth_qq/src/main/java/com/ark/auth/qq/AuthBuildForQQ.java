package com.ark.auth.qq;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import com.ark.auth.*;
import com.tencent.connect.UserInfo;
import com.tencent.connect.share.QQShare;
import com.tencent.connect.share.QzonePublish;
import com.tencent.connect.share.QzoneShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONObject;

@SuppressWarnings("unused")
public class AuthBuildForQQ extends BaseAuthBuildForQQ {

    private Tencent mTencent;

    private AuthBuildForQQ(Context context) {
        super(context);
    }

    public static Auth.AuthBuildFactory getFactory() {
        return new Auth.AuthBuildFactory() {

            @Override
            public <T extends BaseAuthBuild> T getAuthBuild(Context context) {
                //noinspection unchecked
                return (T) new AuthBuildForQQ(context);
            }
        };
    }

    public Tencent getTencent() {
        return mTencent;
    }

    @Override
    public BaseAuthBuildForQQ.Controller getController(Activity activity) {
        return new Controller(this, activity);
    }

    @Override
    protected void init() {
        if (TextUtils.isEmpty(Auth.AuthBuilderInit.getInstance().getQQAppID())) {
            throw new IllegalArgumentException("QQAppID was empty");
        } else {
            mTencent = Tencent.createInstance(Auth.AuthBuilderInit.getInstance().getQQAppID(),
                    mContext.getApplicationContext());
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        mTencent = null;
        if (mImageList != null) {
            mImageList.clear();
            mImageList = null;
        }
    }

    @Override
    public void build(AuthCallback callback) {
        super.build(callback);
        if (mTencent.isQQInstalled(mContext)) {
            Intent intent = new Intent(mContext, AuthActivity.class);
            intent.putExtra("Sign", mSign);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            mContext.startActivity(intent);
        } else {
            mCallback.onFailed(String.valueOf(Auth.ErrorUninstalled), "请安装QQ客户端");
            destroy();
        }
    }

    private void share(Activity activity, IUiListener listener) {
        switch (mAction) {
            case Auth.ShareImage:
                shareImage(activity, listener);
                break;
            case Auth.ShareMusic:
                shareMusic(activity, listener);
                break;
            case Auth.ShareVideo:
                shareVideo(activity, listener);
                break;
            case Auth.ShareProgram:
                shareProgram(activity, listener);
                break;
            default:
                mCallback.onFailed(String.valueOf(Auth.ErrorParameter), "QQ 暂未支持的 Action");
                activity.finish();
                break;
        }
    }

    private void shareImage(Activity activity, IUiListener listener) {
        if (mMood) {
            final Bundle params = new Bundle();
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                    QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHMOOD);
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY,
                    mTitle);
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL,
                    mImageList);
            Bundle extParams = new Bundle();
            extParams.putString(QzonePublish.HULIAN_EXTRA_SCENE,
                    mScene);
            extParams.putString(QzonePublish.HULIAN_CALL_BACK,
                    mBack);
            params.putBundle(QzonePublish.PUBLISH_TO_QZONE_EXTMAP, extParams);
            mTencent.publishToQzone(activity, params, listener);
        } else if (TextUtils.isEmpty(mTitle)) {
            if (TextUtils.isEmpty(mImageUrl)) {
                mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                        "必须添加Image本地路径, 且不为空, 使用 shareImageUrl(url), 若使用多图分享或图文分享,还需要添加 Title, 使用 shareImageTitle(str) ");
                activity.finish();
            } else {
                Bundle params = new Bundle();
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_IMAGE);
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_LOCAL_URL, mImageUrl);
                params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mName);
                if (mQzone != null) {
                    if (mQzone) {
                        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
                                QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
                    } else {
                        params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
                                QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
                    }
                }
                mTencent.shareToQQ(activity, params, listener);
            }
        } else if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加跳转链接, 且不为空, 使用 shareImageTargetUrl(url) ");
            activity.finish();
        } else if (mMultiImage) {
            final Bundle params = new Bundle();
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                    QzoneShare.SHARE_TO_QZONE_TYPE_IMAGE_TEXT);
            params.putString(QzoneShare.SHARE_TO_QQ_TITLE, mTitle);             // 必填
            params.putString(QzoneShare.SHARE_TO_QQ_SUMMARY, mDescription);     // 选填
            params.putString(QzoneShare.SHARE_TO_QQ_TARGET_URL, mUrl);          // 必填
            params.putStringArrayList(QzoneShare.SHARE_TO_QQ_IMAGE_URL, mImageList);
            mTencent.shareToQzone(activity, params, listener);
        } else {
            final Bundle params = new Bundle();
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
            params.putString(QQShare.SHARE_TO_QQ_TITLE, mTitle);
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, mDescription);
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL, mUrl);
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, mImageUrl);
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mName);
            if (!TextUtils.isEmpty(mArk)) {
                params.putString(QQShare.SHARE_TO_QQ_ARK_INFO, mArk);
            }
            if (mQzone != null) {
                if (mQzone) {
                    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
                            QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
                } else {
                    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
                            QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
                }
            }
            mTencent.shareToQQ(activity, params, listener);
        }
    }

    private void shareMusic(Activity activity, IUiListener listener) {
        if (TextUtils.isEmpty(mTitle)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加标题, 使用 shareMusicTitle(str) ");
            activity.finish();
        } else if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加点击后跳转链接, 且不为空, 使用 shareMusicTargetUrl(url) ");
            activity.finish();
        } else if (TextUtils.isEmpty(mAudioUrl)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加音乐链接, 不支持本地音乐, 使用 shareMusicUrl(url) ");
            activity.finish();
        } else {
            final Bundle params = new Bundle();
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_AUDIO);
            params.putString(QQShare.SHARE_TO_QQ_TITLE, mTitle);
            params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,
                    mUrl);
            params.putString(QQShare.SHARE_TO_QQ_AUDIO_URL,
                    mAudioUrl);
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,
                    mImageUrl);
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, mDescription);
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mName);
            if (mQzone != null) {
                if (mQzone) {
                    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
                            QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
                } else {
                    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
                            QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
                }
            }
            mTencent.shareToQQ(activity, params, listener);
        }
    }

    private void shareVideo(Activity activity, IUiListener listener) {
        if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加Video链接, 且不为空, 使用 shareVideoUrl(url) ");
            activity.finish();
        } else {
            final Bundle params = new Bundle();
            params.putInt(QzoneShare.SHARE_TO_QZONE_KEY_TYPE,
                    QzonePublish.PUBLISH_TO_QZONE_TYPE_PUBLISHVIDEO);
            params.putString(QzonePublish.PUBLISH_TO_QZONE_VIDEO_PATH, mUrl);
            Bundle extParams = new Bundle();
            extParams.putString(QzonePublish.HULIAN_EXTRA_SCENE,
                    mScene);
            extParams.putString(QzonePublish.HULIAN_CALL_BACK,
                    mBack);
            params.putBundle(QzonePublish.PUBLISH_TO_QZONE_EXTMAP, extParams);
            mTencent.publishToQzone(activity, params, listener);
        }
    }

    private void shareProgram(Activity activity, IUiListener listener) {
        if (TextUtils.isEmpty(mTitle)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加标题, 使用 shareProgramTitle(str) ");
            activity.finish();
        } else {
            final Bundle params = new Bundle();
            params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE, QQShare.SHARE_TO_QQ_TYPE_APP);
            params.putString(QQShare.SHARE_TO_QQ_TITLE, mTitle);
            params.putString(QQShare.SHARE_TO_QQ_SUMMARY, mDescription);
            params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL, mImageUrl);
            params.putString(QQShare.SHARE_TO_QQ_APP_NAME, mName);
            if (mQzone != null) {
                if (mQzone) {
                    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
                            QQShare.SHARE_TO_QQ_FLAG_QZONE_AUTO_OPEN);
                } else {
                    params.putInt(QQShare.SHARE_TO_QQ_EXT_INT,
                            QQShare.SHARE_TO_QQ_FLAG_QZONE_ITEM_HIDE);
                }
            }
            mTencent.shareToQQ(activity, params, listener);
        }
    }

    private void getInfo(JSONObject object) {
        new GetInfo(mCallback, mContext.getApplicationContext(), mTencent).execute(object);
    }

    private static class GetInfo extends AsyncTask<JSONObject, Void, UserInfo> {

        String openid;
        String access_token;
        int expires_in;
        long expires_time;
        private AuthCallback callback;
        private Context context;
        private Tencent tencent;

        GetInfo(AuthCallback callback, Context context, Tencent tencent) {
            this.callback = callback;
            this.context = context;
            this.tencent = tencent;
        }

        @Override
        protected UserInfo doInBackground(JSONObject... objects) {
            try {
                JSONObject object = objects[0];
                if (object != null && object.length() != 0 && object.optInt("ret", -1) == 0) {
                    openid = object.getString("openid");
                    access_token = object.getString("access_token");
                    expires_in = object.optInt("expires_in", 0);
                    expires_time = object.optLong("expires_time", 0);

                    if (!TextUtils.isEmpty(access_token) && !TextUtils.isEmpty(openid)) {
                        tencent.setAccessToken(access_token, String.valueOf(expires_in));
                        tencent.setOpenId(openid);
                    }
                    return new UserInfo(context, tencent.getQQToken());
                } else {
                    if (object != null) {
                        callback.onFailed(object.getString("code"), object.getString("msg"));
                    } else {
                        callback.onFailed(String.valueOf(Auth.ErrorUnknown), "QQ 登录失败，获取信息为空");
                    }
                    return null;
                }
            } catch (Exception e) {
                callback.onFailed(String.valueOf(Auth.ErrorUnknown), e.getMessage());
                return null;
            }
        }

        @Override
        protected void onPostExecute(UserInfo info) {
            super.onPostExecute(info);
            if (info != null) {
                info.getUserInfo(new IUiListener() {
                    @Override
                    public void onComplete(Object o) {
                        try {
                            UserInfoForThird info = new UserInfoForThird()
                                    .initForQQ((JSONObject) o, openid, access_token, expires_time,
                                            expires_in);
                            if (info != null) {
                                callback.onSuccessForLogin(info);
                            } else {
                                callback.onFailed(String.valueOf(Auth.ErrorUnknown),
                                        "QQ 登录失败，获取信息为空");
                            }
                            destroy();
                        } catch (Exception e) {
                            callback.onFailed(String.valueOf(Auth.ErrorUnknown), e.getMessage());
                            destroy();
                        }
                    }

                    @Override
                    public void onError(UiError uiError) {
                        callback.onFailed(String.valueOf(uiError.errorCode), uiError.errorMessage);
                        destroy();
                    }

                    @Override
                    public void onCancel() {
                        callback.onCancel();
                        destroy();
                    }
                });
            } else {
                //                callback.onFailed("QQ 登录失败");
                destroy();
            }
        }

        private void destroy() {
            callback = null;
            context = null;
            tencent = null;
        }
    }

    static class Controller implements BaseAuthBuildForQQ.Controller, IUiListener {

        private AuthBuildForQQ mBuild;
        private Activity mActivity;

        Controller(AuthBuildForQQ build, Activity activity) {
            mBuild = build;
            mActivity = activity;

            if (mBuild.mAction == Auth.Login) {
                mBuild.mTencent.login(mActivity, "all", this);
            } else {
                mBuild.share(mActivity, this);
            }
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
            Tencent.onActivityResultData(requestCode, resultCode, data, this);
        }

        @Override
        public void onComplete(Object o) {
            if (mBuild.mAction == Auth.Login) {
                mBuild.getInfo((JSONObject) o);
            } else {
                mBuild.mCallback.onSuccessForShare();
            }
            destroy();
        }

        @Override
        public void onError(UiError uiError) {
            mBuild.mCallback.onFailed(String.valueOf(uiError.errorCode), uiError.errorMessage);
            destroy();
        }

        @Override
        public void onCancel() {
            mBuild.mCallback.onCancel();
            destroy();
        }
    }
}