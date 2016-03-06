package com.moorango.medli.lib.network;

import android.text.TextUtils;

import com.moorango.medli.Constants;
import com.moorango.medli.utils.CommonUtil;
import com.moorango.medli.utils.LogUtil;
import com.moorango.medli.utils.PreferencesUtil;
import com.moorango.medli.utils.TimeUtils;

import org.apache.http.HttpResponse;

import java.io.IOException;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.concurrent.TimeUnit;

import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by cmac147 on 7/17/15.
 */
public class API {

    private static final String TAG = "API.java";
    private static API instance;
    private static OkHttpClient client;
    private static final int TIMEOUT_READ = 30000, TIMEOUT_CONNECT = 5000;
    private Boolean successfulRequest = false;

    private API() {
        /*
        Only need one instance of the actual client that is shared amongst all API instances
         */
        if (client == null) {
            OkHttpClient.Builder builder = new OkHttpClient().newBuilder();

            builder.connectTimeout(TIMEOUT_CONNECT, TimeUnit.MILLISECONDS);
            builder.readTimeout(TIMEOUT_READ, TimeUnit.MILLISECONDS);
            if (!Constants.PRODUCTION) {
                HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                logging.setLevel(HttpLoggingInterceptor.Level.BODY);
                builder.addInterceptor(logging);
            }

            builder.addNetworkInterceptor(new Interceptor() {
                @Override
                public Response intercept(Chain chain) throws IOException {
                    String token = PreferencesUtil.getAccessToken();
                    Request.Builder requestBuilder = chain.request().newBuilder();
                    if (!TextUtils.isEmpty(token)) {
                        requestBuilder.addHeader("Access-Token", token);
                    }
                    return chain.proceed(requestBuilder.build());
                }
            });

            client = builder.build();
        }
    }

    public static API getInstance() {
        if (instance == null) instance = new API();


        return instance;
    }

    public String post(URL url, RequestParams params) {
        return post(url, params, null, null);
    }

    public String post(URL url, RequestParams params, String headerName, String headerValue) {
        try {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            // params.put("current_time_zone", TimeUtils.getTimeZone());
            RequestBody body = RequestBody.create(JSON, params.getBody());

            LogUtil.log(TAG, params.toString());
            Request request;
            if (headerName != null) {
                request = new Request.Builder()
                        .url(url.getURL()).addHeader(headerName, headerValue)
                        .post(body)
                        .build();
            } else {
                request = new Request.Builder()
                        .url(url.getURL())
                        .post(body)
                        .build();
            }


            Response response = client.newCall(request).execute();
            successfulRequest = ((response.code() == 200) || (response.code() == 201));
            return response.body().string();
        } catch (IOException ioe) {
            ioe.printStackTrace();

            return "";
        }

    }

    /*
        May or may not be used by saveProfilePic in AuthUtil.java.
     */
    public String post(URL url, Request request) {
        if (CommonUtil.checkNetworkState()) {
            try {
                Response response = client.newCall(request).execute();
                successfulRequest = ((response.code() == 200) || (response.code() == 201));
                return response.body().string();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return "";
            }
        }
        return null;
    }

    public String post(URL url, String params) {
        if (CommonUtil.checkNetworkState()) {
            try {
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, params);
                Request request = new Request.Builder()
                        .url(url.getURL())
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                successfulRequest = ((response.code() == 200) || (response.code() == 201));
                return response.body().string();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return "";
            }
        }

        return null;

    }


    public String postWithGetParams(URL url, String getParams, String headerParams) {
        if (CommonUtil.checkNetworkState()) {
            try {
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, headerParams);
                String parameterizedUrl = url.getURL() + (getParams != null && getParams.length() > 0 ? "?" + getParams : "");

                Request request = new Request.Builder()
                        .url(parameterizedUrl)
                        .post(body)
                        .build();

                Response response = client.newCall(request).execute();
                successfulRequest = ((response.code() == 200) || (response.code() == 201));
                return response.body().string();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return "";
            }
        }

        return null;

    }

    public String put(URL url, String params) {
        if (CommonUtil.checkNetworkState()) {
            try {
                MediaType JSON = MediaType.parse("application/json; charset=utf-8");
                RequestBody body = RequestBody.create(JSON, params);
                Request request = new Request.Builder()
                        .url(url.getURL())
                        .put(body)
                        .build();

                Response response = client.newCall(request).execute();
                successfulRequest = ((response.code() == 200) || (response.code() == 201));
                return response.body().string();
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return "";
            }
        }

        return null;

    }


    public String get(URL url, RequestParams params) {
        if (CommonUtil.checkNetworkState()) {
            params.put("current_time_zone", TimeUtils.getTimeZone());
            String parameterizedUrl = url.getURL() + "?" + getParamString(params);

            Request request = new Request.Builder()
                    .url(parameterizedUrl)
                    .get().build();

            return cleanReturn(getResponse(request));
        }
        return null;

    }


    private String getParamString(RequestParams params) {
        return params.getParamString();
    }

    private Response getResponse(Request originalRequest) {
        try {
            return client.newCall(originalRequest).execute();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String cleanReturn(Response response) {
        ResponseBody body = null;
        if (response != null) {
            body = response.body();

        }

        checkSuccess(response);
        return getBody(body);
    }


    public Boolean getSuccess() {
        return successfulRequest;
    }

    private String getBody(ResponseBody body) {
        System.gc();
        try {
            if (body != null) {
                try {
                    return body.string();
                } catch (IllegalCharsetNameException icne) {
                    icne.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        } catch (IOException e) {
            /*
            If request failed you will get back null, so make sure to check getSuccess()
             */
            return null;
        } catch (UnsupportedCharsetException uce) {
            /* Patch for Crashlytics (Pivotal:97317986) */
            uce.printStackTrace();
            return null;
        } catch (NullPointerException npE) {
            npE.printStackTrace();
            return null;
        } catch (ArrayIndexOutOfBoundsException aio) {
            /* Patch for Crashlytics (Pivotal:98891762) */
            aio.printStackTrace();
            return null;
        }
    }

    private void checkSuccess(Response response) {
        if (response != null) {
            checkSuccess(response.code());
        }
    }

    private void checkSuccess(HttpResponse response) {
        if (response != null) {
            checkSuccess(response.getStatusLine().getStatusCode());
        }
    }

    private void checkSuccess(final int responseCode) {
        this.successfulRequest = ((responseCode == 200) || (responseCode == 201));
/*
        if (!successfulRequest) {
           */
/*  The following check for an acceptable 401 (Unauthorized) server response
            *  was implemented because:
            *  If the user enters an incorrect password when trying to log in,
            *  this was attempting to re-log them in.  If they had just tried to
            *  re-link their facebook account, they'll get logged in even though
            *  they have not entered the correct password. (if they close the app
            *  and launch the app again, they'll be logged in.)
            *  In addition, if the login response continues to respond with a 401,
            *  this may still go on indefinitely. A counter may be needed here to prevent this.
            *  If the 401 (unauthorized) response is indeed needed to try to log
            *  the user in, then perhaps the API needs another response code returned
            *  when the user enters an incorrect password.
            *  *//*

            if ((responseCode == 401) && !acceptUnauthorizedServerResponse) {
                try {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //CommonUtil.styledToast(Application.getContext(), Constants.NETWORK_ERROR + responseCode);
                            */
/*if (responseCode == 401) {
                                AuthUtil.getInstance().loginTask();
                            }*//*

                            AuthUtil.getInstance().loginTask(null);
                            if (httpRequest != null) {
                                LogUtil.log("SERVER_ERROR", httpRequest.toString() + " code: " + responseCode);
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
*/
    }

    public String delete(URL url, RequestParams params) {
        try {
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");
            params.put("current_time_zone", TimeUtils.getTimeZone());
            RequestBody body = RequestBody.create(JSON, params.getBody());
            Request request;

            request = new Request.Builder()
                    .url(url.getURL())
                    .delete(body)
                    .build();

            Response response = client.newCall(request).execute();
            successfulRequest = ((response.code() == 200) || (response.code() == 201));
            return response.body().string();
        } catch (IOException ioe) {
            ioe.printStackTrace();

            return "";
        }

    }
}