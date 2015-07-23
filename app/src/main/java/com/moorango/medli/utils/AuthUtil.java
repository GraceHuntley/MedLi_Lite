package com.moorango.medli.utils;

import com.moorango.medli.lib.network.API;
import com.moorango.medli.lib.network.RequestParams;
import com.moorango.medli.lib.network.URL;

import org.json.JSONObject;

import java.security.MessageDigest;

/**
 * Created by cmac147 on 7/14/15.
 */
public class AuthUtil {

    private static AuthUtil instance;
    private static final String TAG = "AuthUtil";
    private boolean isLoggedIn = false;

    private String token = null;
    private String userId = null;
    private String statusCode = null;
    private String responseMessage = null;

    public static AuthUtil getInstance() {
        if (instance == null) {
            instance = new AuthUtil();
        }

        return instance;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    public static String encryptPassword(String password) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");
            messageDigest.update(password.getBytes("iso-8859-1"), 0, password.length());
            byte[] sha1hash = messageDigest.digest();
            return convertToHex(sha1hash);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static String convertToHex(byte[] data) {
        StringBuilder result = new StringBuilder();
        for (byte aData : data) {
            result.append(Integer.toString((aData & 0xff) + 0x100, 16).substring(1));
        }
        return result.toString();
    }

    public boolean canLogin() {
        return PreferenceUtil.getEmail() != null && PreferenceUtil.getPassword() != null;
    }

    public void login() {
        if (canLogin()) {

            this.token = null;

            RequestParams params = new RequestParams();

            if (PreferenceUtil.getEmail() != null) {
                params.put("email", PreferenceUtil.getEmail());

            }

            if (PreferenceUtil.getPassword() != null) {
                params.put("password", PreferenceUtil.getPassword());
            }

            API api = API.getInstance();
            api.setAcceptUnauthorizedServerResponse(true);

            String response = api.post(URL.generateUnsecureURL("sessions"), params);

            LogUtil.log(TAG, "test:" + response);

            if (api.getSuccess() && response != null) {

                JSONObject data = JsonUtil.parseString(response);
                this.token = JsonUtil.getString("authentication_token", data);
                this.userId = JsonUtil.getString("user_id", data);

                this.isLoggedIn = true;

                LogUtil.log(TAG, "loggedIn");

                PreferenceUtil.setLoggedIn(true);


            } else {

                LogUtil.log(TAG, "failed");

                PreferenceUtil.setLoggedIn(false);

                try {
                    if (response != null) {
                        JSONObject data = JsonUtil.parseString(response);
                        this.statusCode = JsonUtil.getString("code", data);
                        this.responseMessage = JsonUtil.getString("message", data);
                        PreferenceUtil.setEmail(null);
                        //PreferencesUtil.setCountryCode(null);

                        LogUtil.log(TAG, JsonUtil.getString("code", data));
                        LogUtil.log(TAG, JsonUtil.getString("message", data));


                    } else {
                        this.responseMessage = "Check your network connection.";
                    }
                } catch (NullPointerException npE) {
                    npE.printStackTrace();
                }
            }
        }

    }
}
