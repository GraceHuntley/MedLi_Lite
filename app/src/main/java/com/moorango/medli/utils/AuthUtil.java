package com.moorango.medli.utils;

import android.widget.Toast;

import com.moorango.medli.Application;
import com.moorango.medli.Constants;
import com.moorango.medli.lib.eventbus.EventBus;
import com.moorango.medli.lib.eventbus.events.FinishOnboardingEvent;
import com.moorango.medli.lib.eventbus.events.RevertLifeCycleForSignOut;
import com.moorango.medli.lib.network.API;
import com.moorango.medli.lib.network.RequestParams;
import com.moorango.medli.lib.network.URL;
import com.moorango.medli.runnables.VoidTask;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by cmac147 on 7/6/15.
 */
public class AuthUtil {
    private final String TAG = getClass().getSimpleName();
    private static AuthUtil instance;
    private boolean isLoggedIn = false;

    public static AuthUtil getInstance() {
        if (instance == null) {
            instance = new AuthUtil();
        }
        return instance;
    }

    public boolean isLoggedIn() {
        return this.isLoggedIn;
    }

    /*
        Clear preferences related to profile (usually in case of errors).
     */
    public static void clearCredentials() {
        PreferencesUtil.setFirstName(null);
        PreferencesUtil.setLastName(null);
        PreferencesUtil.setEmail(null);
        PreferencesUtil.setPassword(null);
        PreferencesUtil.setAccessToken(null);
        PreferencesUtil.setLoggedIn(false);
        PreferencesUtil.setPhoneNumber(null);
        PreferencesUtil.setProfilePicPath(null);
    }

    public void LoginTask() {


        new VoidTask() {

            String response;
            API api;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

            }

            @Override
            protected Void doInBackground(Void... voids) {

                api = API.getInstance();

                RequestParams params = new RequestParams();
                params.put("email", PreferencesUtil.getEmail());
                params.put("password", PreferencesUtil.getPassword());
                params.put("authentication_type", "login");

                LogUtil.log(TAG, "url = " + Constants.BASE_URL + "authentication.json");
                response = api.post(URL.generateUrlFromScratch(Constants.BASE_URL + "authentication.json"), params);

                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if ((response != null) && (response.length() > 0)) {
                    LogUtil.log(TAG, response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        isLoggedIn = success;
                        if (success) {
                            // Save token
                            JSONObject data = jsonObject.getJSONObject("data");
                            String accessToken = data.getString("access_token");
                            PreferencesUtil.setAccessToken(accessToken);
                            EventBus.getInstance().post(new FinishOnboardingEvent("test", FinishOnboardingEvent.SUCCESS));
                        } else {
                            String errorMsg = jsonObject.getString("message");
                            Toast.makeText(Application.getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                            LogUtil.log(TAG, "Login failed: " + errorMsg);
                            isLoggedIn = false;
                            clearCredentials();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        isLoggedIn = false;
                        clearCredentials();
                    }
                } else {
                    Toast.makeText(Application.getContext(), "Login failed", Toast.LENGTH_SHORT).show();
                    isLoggedIn = false;
                    clearCredentials();
                }

                // EventBus.getInstance().post(new OnBoardingEvent(OnBoardingActivity.OnBoardingEventType.LOGIN_DONE, isLoggedIn, false, false));

            }
        }.execute();


    }

    public void SignUpTask() {


        new VoidTask() {

            String response;
            API api;

            @Override
            protected Void doInBackground(Void... voids) {

                api = API.getInstance();

                RequestParams params = new RequestParams();
                params.put("email", PreferencesUtil.getEmail());
                params.put("password", PreferencesUtil.getPassword());
                params.put("name", PreferencesUtil.getFirstName() + " " + PreferencesUtil.getLastName());
                params.put("authentication_type", "signup");

                LogUtil.log(TAG, "url = " + Constants.BASE_URL + "authentication.json");
                response = api.post(URL.generateUrlFromScratch(Constants.BASE_URL + "authentication.json"), params);

                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);

                if ((response != null) && (response.length() > 0)) {
                    LogUtil.log(TAG, response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");

                        isLoggedIn = success;
                        if (success) {
                            // Save token
                            JSONObject data = jsonObject.getJSONObject("data");
                            String accessToken = data.getString("access_token");
                            PreferencesUtil.setAccessToken(accessToken);
                            EventBus.getInstance().post(new FinishOnboardingEvent("Signed up successfully!", FinishOnboardingEvent.SUCCESS));
                        } else {
                            String errorMsg = jsonObject.getString("message");
                            Toast.makeText(Application.getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                            LogUtil.log(TAG, "Login failed: " + errorMsg);
                            isLoggedIn = false;
                            clearCredentials();
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                        isLoggedIn = false;
                        clearCredentials();
                    }
                } else {
                    Toast.makeText(Application.getContext(), "Login failed", Toast.LENGTH_SHORT).show();
                    isLoggedIn = false;
                    clearCredentials();
                }

                // EventBus.getInstance().post(new OnBoardingEvent(OnBoardingActivity.OnBoardingEventType.LOGIN_DONE, isLoggedIn, false, false));

            }
        }.execute();
    }

    public void LogoutTask() {


        new VoidTask() {
            boolean success = false;
            String response;
            API api;

            @Override
            protected void onPreExecute() {
                super.onPreExecute();
//                if (spinner != null)
//                    spinner.show();
            }

            @Override
            protected Void doInBackground(Void... voids) {

                api = API.getInstance();

                RequestParams params = new RequestParams();
                params.put("access_token", PreferencesUtil.getAccessToken());

                LogUtil.log(TAG, "url = " + Constants.BASE_URL + "sign_out.json");
                response = api.post(URL.generateUrlFromScratch(Constants.BASE_URL + "sign_out.json"), params);


                return null;

            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
//                if (spinner != null)
//                    spinner.hide();

                if ((response != null) && (response.length() > 0)) {
                    LogUtil.log(TAG, response);
                    try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("status");

                        isLoggedIn = !success;
                        if (success) {

                            clearCredentials();


                            EventBus.getInstance().post(new RevertLifeCycleForSignOut());
                        } else {
                            String errorMsg = jsonObject.getString("message");
                            Toast.makeText(Application.getContext(), errorMsg, Toast.LENGTH_SHORT).show();
                            LogUtil.log(TAG, "Logout failed: " + errorMsg);
                            isLoggedIn = false;
                        }


                    } catch (JSONException e) {
                        e.printStackTrace();

                    }
                } else {
                    Toast.makeText(Application.getContext(), "Logout failed", Toast.LENGTH_SHORT).show();

                }

                // EventBus.getInstance().post(new OnBoardingEvent(OnBoardingActivity.OnBoardingEventType.LOGIN_DONE, isLoggedIn, false, false));

            }
        }.execute();


    }

}
