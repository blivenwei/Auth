package com.ark.auth;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;

import java.util.ArrayList;

public abstract class BaseAuthBuildForWB extends BaseAuthBuild {
    protected String mTitle;                                          // 标题
    protected String mText;                                           // 文本
    protected String mDescription;                                    // 描述
    protected Bitmap mBitmap;                                         // 图片
    protected String mUrl;                                            // Url

    protected Uri mUri;                                               // 微博 Uri 地址
    protected boolean mStory = false;                                 // 微博 是否分享到微博故事, 仅支持单图 和 视频
    protected boolean mMultiImage = false;                            // 是否是多图分享
    protected ArrayList<Uri> mImagePathList;                          // 微博 多图路径地址

    protected BaseAuthBuildForWB(Context context) {
        super(context, Auth.WithWB);
    }

    protected abstract Controller getController(Activity activity);


    @Override
    public BaseAuthBuildForWB setAction(@Auth.ActionWB int action) {
        mAction = action;
        return this;
    }

    /**
     * 是否分享到微博故事, 仅支持单图 和 视频
     * 如果分享视频到微博故事, shareVideoUri shareVideoTitle shareVideoText shareVideoDescription 将失效, 只使用 uri 内容
     * , Uri 为本地视频
     */

    public BaseAuthBuildForWB shareToStory() {
        mStory = true;
        return this;
    }


    public BaseAuthBuildForWB shareText(String text) {
        mText = text;
        return this;
    }


    public BaseAuthBuildForWB shareImage(Bitmap bitmap) {              // imageData 大小限制为 2MB
        mBitmap = bitmap;
        return this;
    }


    public BaseAuthBuildForWB shareImageText(String text) {
        mText = text;
        return this;
    }

    /**
     * 分享多张图片, 本地图片 Uri 集合, shareImage 失效
     */

    public BaseAuthBuildForWB shareImageMultiImage(ArrayList<Uri> list) {
        mMultiImage = true;
        mImagePathList = list;
        return this;
    }

    /**
     * 分享图片到微博故事时调用, shareImage shareImageText 将失效, 只使用 uri 内容, Uri 为本地图片
     */

    public BaseAuthBuildForWB shareImageUri(Uri uri) {
        mUri = uri;
        return this;
    }


    public BaseAuthBuildForWB shareLinkTitle(String title) {
        mTitle = title;
        return this;
    }


    public BaseAuthBuildForWB shareLinkDescription(String description) {
        mDescription = description;
        return this;
    }


    public BaseAuthBuildForWB shareLinkImage(Bitmap bitmap) {
        mBitmap = bitmap;
        return this;
    }

    /**
     * 网络链接
     */

    public BaseAuthBuildForWB shareLinkUrl(String url) {
        mUrl = url;
        return this;
    }


    public BaseAuthBuildForWB shareLinkText(String text) {
        mText = text;
        return this;
    }


    public BaseAuthBuildForWB shareVideoTitle(String title) {
        mTitle = title;
        return this;
    }


    public BaseAuthBuildForWB shareVideoText(String text) {
        mText = text;
        return this;
    }


    public BaseAuthBuildForWB shareVideoDescription(String description) {
        mDescription = description;
        return this;
    }

    /**
     * 本地视频 Uri
     */

    public BaseAuthBuildForWB shareVideoUri(Uri uri) {
        mUri = uri;
        return this;
    }

    public interface Controller {
        void destroy();

        void callbackShare();

        void callbackSso(int requestCode, int resultCode, Intent data);
    }
}