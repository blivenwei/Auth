package com.ark.auth;

import org.json.JSONObject;

public class UserInfoForThird {
    /**
     * 1 新浪微博 2 腾讯QQ 3 微信账号
     */
    public String fromId = "";                          // 第三方平台ID  1 新浪微博 2 腾讯QQ 3 微信账号
    public String aToken = "";                          // 第三方平台 access token
    public String rToken = "";                          // 第三方平台 refresh token
    public String userId = "";                          // 第三方平台 用户 ID
    public String openid = "";                          // 第三方平台 OPENID
    public String nickname = "";                        // 第三方平台 用户昵称
    public String expireIn = "0";                       // 第三方平台 token 有效期
    public String expireTime = "0";                     // 第三方平台 TOKEN 过期时间
    public String userLink = "";                        // 第三方平台 用户链接
    public String portrait = "";                        // 第三方平台 用户头像
    public String userInfo = "";                        // 第三方平台 用户信息 Json

    public UserInfoForThird initForWB(String json, String aToken, String rToken, String expiresTime)
            throws Exception {
        JSONObject object = new JSONObject(json);
        if (object.optInt("error_code", -1) == -1) {
            this.fromId = "1";                                                         // 来自微博
            this.nickname = object.optString("screen_name");                    // 用户昵称
            this.userId = object.optString("idstr");                            // 用户 id
            this.aToken = aToken;
            this.rToken = rToken;
            this.expireTime = expiresTime;
            this.portrait = object.optString("avatar_large");
            this.userLink = object.optString("url");
            this.userInfo = json;
            return this;
        }
        return null;
    }

    public UserInfoForThird initForQQ(JSONObject object, String openid, String aToken,
                                      long expires_time, int expires_in) throws Exception {
        if (object.optInt("ret", -1) == 0) {
            this.fromId = "2";                                                          // 来自QQ
            this.nickname = object.optString("nickname");                        // 用户昵称
            //            this.userId = openid;                                                       // 用户 id
            this.openid = openid;
            this.aToken = aToken;
            this.expireIn = String.valueOf(expires_in);
            this.expireTime = String.valueOf(expires_time);
            this.portrait = object.optString("figureurl_2");
            this.userInfo = object.toString();
            return this;
        }
        return null;
    }

    public UserInfoForThird initForWX(String json, String aToken, String rToken, String openid,
                                      long expires_in) throws Exception {
        JSONObject object = new JSONObject(json);
        if (object.optInt("errcode", -1) == -1 && !object.optString("unionid", "").equals("")) {
            this.fromId = "3";                                                 // 来自微信
            this.nickname = object.optString("nickname");               // 用户昵称
            this.userId = object.optString("unionid");                  // 用户 id
            this.openid = openid;
            this.aToken = aToken;
            this.rToken = rToken;
            this.expireIn = String.valueOf(expires_in);
            this.portrait = object.optString("headimgurl");
            this.userInfo = json;
            return this;
        }
        return null;
    }
}