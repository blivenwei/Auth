package com.ark.auth.weixin;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.text.TextUtils;
import com.ark.auth.*;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.OpenWebview;
import com.tencent.mm.opensdk.modelmsg.*;
import com.tencent.mm.opensdk.modelpay.PayReq;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;
import org.json.JSONObject;

import java.util.List;

@SuppressWarnings("unused")
public class AuthBuildForWX extends BaseAuthBuildForWX {

    private static IWXAPI mApi;

    private AuthBuildForWX(Context context) {
        super(context);
    }

    public static Auth.AuthBuildFactory getFactory() {
        return new Auth.AuthBuildFactory() {
            @Override
            public <T extends BaseAuthBuild> T getAuthBuild(Context context) {
                //noinspection unchecked
                return (T) new AuthBuildForWX(context);
            }
        };
    }

    public IWXAPI getApi() {
        return mApi;
    }

    @Override
    protected BaseAuthBuildForWX.Controller getController(Activity activity) {
        return new Controller(this, activity);
    }

    @Override
    protected void init() {
        if (TextUtils.isEmpty(Auth.AuthBuilderInit.getInstance().getWXAppID())) {
            throw new IllegalArgumentException("WECHAT_APPID was empty");
        } else if (mApi == null) {
            mApi = WXAPIFactory
                    .createWXAPI(mContext, Auth.AuthBuilderInit.getInstance().getWXAppID(), true);
            mApi.registerApp(Auth.AuthBuilderInit.getInstance().getWXAppID());
        }
    }

    @Override
    protected void destroy() {
        super.destroy();
        mBitmap = null;
    }

    @Override
    public AuthBuildForWX shareToSession() {                            // 描述分享到朋友圈可以不传,聊天界面和收藏必须传
        mShareType = SendMessageToWX.Req.WXSceneSession;
        return this;
    }

    @Override
    public AuthBuildForWX shareToTimeline() {
        mShareType = SendMessageToWX.Req.WXSceneTimeline;
        return this;
    }

    @Override
    public AuthBuildForWX shareToFavorite() {
        mShareType = SendMessageToWX.Req.WXSceneFavorite;
        return this;
    }

    @Override
    public void build(AuthCallback callback) {
        super.build(callback);
        if (!isWXAppInstalled()) {
            destroy();
        } else if (mAction != Auth.Login && mAction != Auth.Pay && mAction != Auth.Rouse
                && mShareType == -100) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加分享类型, 使用 shareToSession(),shareToTimeline(),shareToFavorite() ");
            destroy();
        } else {
            switch (mAction) {
                case Auth.Pay:
                    pay();
                    break;
                case Auth.Rouse:
                    rouseWeb();
                    break;
                case Auth.Login:
                    login();
                    break;
                case Auth.ShareText:
                    shareText();
                    break;
                case Auth.ShareImage:
                    shareBitmap();
                    break;
                case Auth.ShareLink:
                    shareLink();
                    break;
                case Auth.ShareVideo:
                    shareVideo();
                    break;
                case Auth.ShareMusic:
                    shareMusic();
                    break;
                case Auth.ShareProgram:
                    shareProgram();
                    break;
                default:
                    mCallback.onFailed(String.valueOf(Auth.ErrorParameter), "微信暂未支持的 Action");
                    destroy();
                    break;
            }
        }
    }

    private boolean isWXAppInstalled() {
        if (!mApi.isWXAppInstalled()) {
            final PackageManager packageManager = mContext
                    .getPackageManager();
            List<PackageInfo> pInfo = packageManager.getInstalledPackages(0);
            if (pInfo != null) {
                for (int i = 0; i < pInfo.size(); i++) {
                    String pn = pInfo.get(i).packageName;
                    if (pn.equalsIgnoreCase("com.tencent.mm")) {
                        return true;
                    }
                }
            }
            mCallback.onFailed(String.valueOf(Auth.ErrorUninstalled), "请安装微信客户端");
            return false;
        } else if (mApi.getWXAppSupportAPI() < Build.PAY_SUPPORTED_SDK_INT) {
            mCallback.onFailed(String.valueOf(Auth.ErrorUnknown), "微信客户端版本过低");
            return false;
        } else {
            return true;
        }
    }

    private void shareText() {
        // 分享文本到微信, 文本描述，分享到朋友圈可以不传,聊天界面和收藏必须传
        if (TextUtils.isEmpty(mText)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter), "必须添加文本, 使用 shareText(str) ");
            destroy();
        } else if (mShareType != SendMessageToWX.Req.WXSceneTimeline && TextUtils
                .isEmpty(mDescription)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加文本描述, 使用 shareTextDescription(str) ");
            destroy();
        } else {
            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = new WXTextObject(mText);
            msg.description = mDescription;
            msg.title = mTitle;

            share(msg);
        }
    }

    private void shareBitmap() {
        if (mBitmap == null) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加 Bitmap, 且不为空, 使用 shareImage(bitmap) ");
            destroy();
        } else {
            // imageData 大小限制为 10MB, 缩略图大小限制为 32K
            Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, 120, 120, true);

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = new WXImageObject(mBitmap);
            msg.thumbData = Utils.bmpToByteArray(thumbBmp, false);
            msg.title = mTitle;

            share(msg);
        }
    }

    private void shareMusic() {
        if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加音乐链接, 且不为空, 使用 shareMusicUrl(url) ");
            destroy();
        } else if (mBitmap == null) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加音乐缩略图, 且不为空, 使用 shareMusicImage(bitmap) ");
            destroy();
        } else if (mTitle == null) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加音乐标题, 使用 shareMusicTitle(title) ");
            destroy();
        } else {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, 120, 120, true);
            WXMusicObject musicObject = new WXMusicObject();
            musicObject.musicUrl = mUrl;                                            // 音乐链接

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = musicObject;
            msg.title = mTitle;                                                     // 音乐标题，必传，但是可是是空字符串
            msg.description = mDescription;                                         // 音乐描述，可不传
            msg.thumbData = Utils.bmpToByteArray(thumbBmp, false);      // 缩略图大小限制为32K

            share(msg);
        }
    }

    private void shareLink() {
        if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加链接, 且不为空, 使用 shareLinkUrl(url) ");
            destroy();
        } else if (mBitmap == null) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加链接缩略图, 且不为空, 使用 shareLinkImage(bitmap) ");
            destroy();
        } else if (mTitle == null) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加链接标题, 使用 shareLinkTitle(title) ");
            destroy();
        } else {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, 120, 120, true);
            WXWebpageObject webObject = new WXWebpageObject();
            webObject.webpageUrl = mUrl;

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = webObject;
            msg.title = mTitle;
            msg.description = mDescription;
            msg.thumbData = Utils.bmpToByteArray(thumbBmp, false);      // 缩略图大小限制为32K

            share(msg);
        }
    }

    private void shareVideo() {
        if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加视频链接, 且不为空, 使用 shareVideoUrl(url) ");
            destroy();
        } else if (mBitmap == null) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加视频缩略图, 且不为空, 使用 shareVideoImage(bitmap) ");
            destroy();
        } else if (mTitle == null) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加视频标题, 使用 shareVideoTitle(title) ");
            destroy();
        } else {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, 120, 120, true);
            WXVideoObject videoObject = new WXVideoObject();
            videoObject.videoUrl = mUrl;                                            // 视频链接

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = videoObject;
            msg.title = mTitle;
            msg.description = mDescription;
            msg.thumbData = Utils.bmpToByteArray(thumbBmp, false);      // 缩略图大小限制为32K

            share(msg);
        }
    }

    private void shareProgram() {
        if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加小程序链接, 且不为空, 使用 shareProgramUrl(url) ");
            destroy();
        } else if (TextUtils.isEmpty(mID)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加小程序ID, 使用 shareProgramId(id) ");
            destroy();
        } else if (TextUtils.isEmpty(mPath)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加小程序Path, 使用 shareProgramPath(path) ");
            destroy();
        } else if (mBitmap == null) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加小程序缩略图, 且不为空, 使用 shareProgramImage(bitmap) ");
            destroy();
        } else if (mTitle == null) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加小程序标题, 使用 shareProgramTitle(title) ");
            destroy();
        } else if (mShareType != SendMessageToWX.Req.WXSceneSession) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter), "目前只支持分享到会话 ");
            destroy();
        } else {
            Bitmap thumbBmp = Bitmap.createScaledBitmap(mBitmap, 120, 120, true);
            WXMiniProgramObject programObject = new WXMiniProgramObject();
            programObject.webpageUrl = mUrl;                                        // 低版本微信打开该 url
            programObject.userName = mID;                                           // 跳转小程序的原始 ID
            programObject.path = mPath;                                             // 小程序的Path

            WXMediaMessage msg = new WXMediaMessage();
            msg.mediaObject = programObject;
            msg.title = mTitle;
            msg.description = mDescription;
            msg.thumbData = Utils.bmpToByteArray(thumbBmp, false);      // 缩略图大小限制为32K

            share(msg);
        }
    }

    private void share(WXMediaMessage msg) {
        if (msg == null) {
            mCallback.onFailed(String.valueOf(Auth.ErrorUnknown), "分享失败, Auth 内部错误");
            destroy();
        } else {
            SendMessageToWX.Req req = new SendMessageToWX.Req();
            req.transaction = mSign;                                                // 用于唯一标识一个请求
            req.scene = mShareType;
            req.message = msg;
            mApi.sendReq(req);
        }
    }

    private void pay() {
        if (TextUtils.isEmpty(mPartnerId)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加 PartnerId, 使用 payPartnerId(id) ");
            destroy();
        } else if (TextUtils.isEmpty(mPrepayId)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加 PrepayId, 使用 payPrepayId(id) ");
            destroy();
        } else if (TextUtils.isEmpty(mPackageValue)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加 PackageValue, 使用 payPackageValue(value) 固定值: \"Sign=WXPay\" ");
            destroy();
        } else if (TextUtils.isEmpty(mNonceStr)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加 NonceStr, 使用 payNonceStr(str) ");
            destroy();
        } else if (TextUtils.isEmpty(mTimestamp)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter),
                    "必须添加 Timestamp, 使用 payTimestamp(time) ");
            destroy();
        } else if (TextUtils.isEmpty(mPaySign)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter), "必须添加 Sign, 使用 paySign(sign) ");
            destroy();
        } else {
            mSign = mPrepayId;
            PayReq req = new PayReq();
            req.transaction = mSign;         // 回调时这个标记为 null, 只有 prePayId 可用, 所以使用 prePayId 作为标记

            req.appId = Auth.AuthBuilderInit.getInstance().getWXAppID();
            req.partnerId = mPartnerId;
            req.prepayId = mPrepayId;
            req.packageValue = mPackageValue;
            req.nonceStr = mNonceStr;
            req.timeStamp = mTimestamp;
            req.sign = mPaySign;
            mApi.sendReq(req);
        }
    }

    private void rouseWeb() {
        if (TextUtils.isEmpty(mUrl)) {
            mCallback.onFailed(String.valueOf(Auth.ErrorParameter), "必须添加 Url, 使用 rouseWeb(url) ");
            destroy();
        } else {
            OpenWebview.Req req = new OpenWebview.Req();
            req.transaction = mSign;         // 回调时这个标记和设置的不一样, 无法作为判断依据
            req.url = mUrl;
            mApi.sendReq(req);
        }
    }

    private void login() {                                                  // 微信登录, 1 获取微信 code
        if (mApi.isWXAppInstalled()) {
            SendAuth.Req req = new SendAuth.Req();
            req.scope = "snsapi_userinfo";
            req.state = mSign;
            req.transaction = mSign;
            mApi.sendReq(req);
        } else {
            mCallback.onFailed(String.valueOf(Auth.ErrorUninstalled), "请安装微信客户端");
        }
    }

    private void getInfo(
            String code) {                                             // 通过 AuthActivity 调用
        new AuthBuildForWX.GetInfo(mCallback).execute(code);
    }

    private static class GetInfo extends AsyncTask<String, Void, UserInfoForThird> {

        private AuthCallback callback;                                      // 回调函数

        GetInfo(AuthCallback callback) {
            this.callback = callback;
        }

        @Override
        protected UserInfoForThird doInBackground(String... strings) {
            try {
                String j2 = getToken(strings[0]);
                JSONObject object2 = new JSONObject(j2);
                String refresh_token = object2.getString("refresh_token");

                String j3 = refreshToken(refresh_token);
                JSONObject object3 = new JSONObject(j3);
                String access_token = object3.getString("access_token");
                String openid = object3.getString("openid");
                long expires_in = object3.getLong("expires_in");

                if (checkToken(access_token, openid)) {
                    return new UserInfoForThird()
                            .initForWX(getUserInfo(access_token, openid), access_token,
                                    refresh_token, openid, expires_in);
                }
                return null;
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(UserInfoForThird info) {
            super.onPostExecute(info);
            if (info != null) {
                callback.onSuccessForLogin(info);
            } else {
                callback.onFailed(String.valueOf(Auth.ErrorUnknown), "微信登录失败");
            }
            callback = null;
        }

        // 微信登录, 2 通过 code 获取 refresh_token
        private String getToken(String code) throws Exception {
            String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid="
                    + Auth.AuthBuilderInit.getInstance().getWXAppID()
                    + "&secret="
                    + Auth.AuthBuilderInit.getInstance().getWXSecret()
                    + "&code="
                    + code
                    + "&grant_type=authorization_code";
            return Utils.get(url);
        }

        // 微信登录, 3 通过 refresh_token 刷新 access_token
        private String refreshToken(String token) throws Exception {
            String url = "https://api.weixin.qq.com/sns/oauth2/refresh_token?appid="
                    + Auth.AuthBuilderInit.getInstance().getWXAppID()
                    + "&grant_type=refresh_token"
                    + "&refresh_token="
                    + token;
            return Utils.get(url);
        }

        // 微信登录, 4 检验授权凭证（access_token）是否有效
        private boolean checkToken(String token, String openId) throws Exception {
            String url = "https://api.weixin.qq.com/sns/auth?access_token="
                    + token
                    + "&openid="
                    + openId;
            JSONObject object = new JSONObject(Utils.get(url));
            return object.getInt("errcode") == 0;
        }

        // 微信登录, 5 获取用户信息
        private String getUserInfo(String token, String openId) throws Exception {
            String url = "https://api.weixin.qq.com/sns/userinfo?access_token="
                    + token
                    + "&openid="
                    + openId;
            return Utils.get(url);
        }
    }

    static class Controller implements IWXAPIEventHandler, BaseAuthBuildForWX.Controller {

        private AuthBuildForWX mBuild;
        private Activity mActivity;

        Controller(AuthBuildForWX build, Activity activity) {
            mBuild = build;
            mActivity = activity;
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
        public void callback() {
            if (mApi != null) {
                mApi.handleIntent(mActivity.getIntent(), this);
            }
        }

        @Override
        public void onReq(BaseReq baseReq) {
            if (mBuild.mAction == Auth.Rouse) {
                mBuild.mCallback.onSuccessForRouse(String.valueOf(baseReq.getType()), "微信签约成功");
            }
            destroy();
        }

        @Override
        public void onResp(BaseResp resp) {
            if (resp != null) {
                switch (resp.errCode) {
                    case BaseResp.ErrCode.ERR_USER_CANCEL:
                    case BaseResp.ErrCode.ERR_AUTH_DENIED:
                        mBuild.mCallback.onCancel();
                        break;
                    case BaseResp.ErrCode.ERR_OK:
                        if (resp.getType() == ConstantsAPI.COMMAND_PAY_BY_WX) {
                            mBuild.mCallback
                                    .onSuccessForPay(String.valueOf(BaseResp.ErrCode.ERR_OK),
                                            "微信支付成功");
                        } else if (resp instanceof SendAuth.Resp && resp.getType()
                                == ConstantsAPI.COMMAND_SENDAUTH) {
                            mBuild.getInfo(((SendAuth.Resp) resp).code);
                        } else if (resp instanceof SendMessageToWX.Resp && resp.getType()
                                == ConstantsAPI.COMMAND_SENDMESSAGE_TO_WX) {
                            mBuild.mCallback.onSuccessForShare();
                        }
                        break;
                    default:
                        if (mBuild.mAction == Auth.Login) {
                            mBuild.mCallback.onFailed(String.valueOf(resp.errCode),
                                    TextUtils.isEmpty(resp.errStr) ? "微信登录失败" : resp.errStr);
                        } else if (mBuild.mAction == Auth.Pay) {
                            mBuild.mCallback.onFailed(String.valueOf(resp.errCode),
                                    TextUtils.isEmpty(resp.errStr) ? "微信支付失败" : resp.errStr);
                        } else if (mBuild.mAction != Auth.Rouse) {
                            mBuild.mCallback.onFailed(String.valueOf(resp.errCode),
                                    TextUtils.isEmpty(resp.errStr) ? "微信分享失败" : resp.errStr);
                        }
                }
            }
            destroy();
        }
    }
}