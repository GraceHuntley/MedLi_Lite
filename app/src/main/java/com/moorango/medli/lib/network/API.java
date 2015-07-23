package com.moorango.medli.lib.network;

import android.os.Handler;
import android.os.Looper;

import com.moorango.medli.Application;
import com.moorango.medli.utils.CommonUtil;
import com.moorango.medli.utils.LogUtil;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;
import com.squareup.okhttp.ResponseBody;

import org.apache.http.HttpResponse;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.List;
import java.util.concurrent.TimeUnit;


public class API {

    //public static final String CONSUMER_KEY = "82987d6543e97a3113839eb5ccc3b7790506e626d";
    //public static final String CONSUMER_SECRET = "09cde00ffefe8724b981327ea4c21b2c";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType FORM_ENCODED = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    //private static final int TIMEOUT_READ = 60000, TIMEOUT_CONNECT = 10000;
    private static final int TIMEOUT_READ = 30000, TIMEOUT_CONNECT = 5000;
    private static OkHttpClient client;

    private final String TAG = getClass().getSimpleName();
    private Boolean successfulRequest = false;
    private Boolean acceptUnauthorizedServerResponse = false;

    private Request httpRequest;

    private API() {
        /*
        Only need one instance of the actual client that is shared amongst all API instances
         */
        if (client == null) {
            client = new OkHttpClient();

            client.setConnectTimeout(TIMEOUT_CONNECT, TimeUnit.MILLISECONDS);
            client.setReadTimeout(TIMEOUT_READ, TimeUnit.MILLISECONDS);

        }
    }

    public static API getInstance() {
        return new API();
    }

    // ALL AVAILABLE NETWORK CALLS

    public String get(URL url) {

        CommonUtil.checkNetworkStateAndWait(Application.getActivityContext());
        if (CommonUtil.checkNetworkState()) {
            httpRequest = new Request.Builder().url(url.getURL()).get().build();

            return cleanReturn(getResponse(httpRequest));
        }
        return null;

    }

    public String get(URL url, RequestParams params) {
        CommonUtil.checkNetworkStateAndWait(Application.getActivityContext());
        if (CommonUtil.checkNetworkState()) {
            String parameterizedUrl = url.getURL() + "?" + getParamString(params);
            httpRequest = new Request.Builder().url(parameterizedUrl).get().build();

            return cleanReturn(getResponse(httpRequest));
        }
        return null;

    }

    public String post(URL url, RequestParams params) {

        try {

            OkHttpClient okClient = new OkHttpClient();
            MediaType JSON = MediaType.parse("application/json; charset=utf-8");

            RequestBody body = RequestBody.create(JSON, params.getBody());
            LogUtil.log(TAG, params.getBody());

            Request request = new Request.Builder()
                    .url(url.getURL())
                    .post(body)
                    .build();

            Response response = okClient.newCall(request).execute();

            //LogUtil.log(TAG, response.toString());
            //LogUtil.log(TAG, response.message().toString());
            //return response.body().string();

            return cleanReturn(response);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //}
        return null;
    }

    public String put(URL url, RequestParams params) {
        CommonUtil.checkNetworkStateAndWait(Application.getActivityContext());
        if (CommonUtil.checkNetworkState()) {
            try {
                //OAuthConsumer consumer = new CommonsHttpOAuthConsumer(API.CONSUMER_KEY,
                //        API.CONSUMER_SECRET);
                HttpPut request = new HttpPut(url.getURL());
                //String token = AuthUtil.getInstance().getToken();
                //String tokenSecret = AuthUtil.getInstance().getTokenSecret();
                //if (token != null && tokenSecret != null) {
                //    consumer.setTokenWithSecret(token, tokenSecret);
                //}
                //consumer.sign(request);
                request.setEntity(new UrlEncodedFormEntity(getParamsList(params)));  //not authorized

                //LogUtil.log(TAG, params.getParamString());
                HttpResponse response = new DefaultHttpClient().execute(request);
                return cleanReturn(response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    public String delete(URL url) {
        CommonUtil.checkNetworkStateAndWait(Application.getActivityContext());
        if (CommonUtil.checkNetworkState()) {

            httpRequest = new Request.Builder().url(url.getURL()).delete().build();

            return cleanReturn(getResponse(httpRequest));
        }
        return null;
    }

    public String head(URL url) {
        CommonUtil.checkNetworkStateAndWait(Application.getActivityContext());
        if (CommonUtil.checkNetworkState()) {
            httpRequest = new Request.Builder().url(url.getURL()).head().build();
            return cleanReturn(getResponse(httpRequest));
        }
        return null;
    }


    // Abstracted logic - mostly convenience stuff

    private String getParamString(RequestParams params) {
        //AuthUtil.getInstance().addCountryCode(params);
        //LogUtil.log(TAG, params.getParamString());
        return params.getParamString();
    }

    public void setAcceptUnauthorizedServerResponse(boolean newValue) {
        acceptUnauthorizedServerResponse = newValue;
    }

    public List<BasicNameValuePair> getParamsList(RequestParams params) {
        //AuthUtil.getInstance().addCountryCode(params);
        return params.getParamsList();
    }

    private Response getResponse(Request originalRequest) {
        try {


            LogUtil.log(TAG, originalRequest.url().toString());
            return client.newCall(httpRequest).execute();
        } catch (Exception e) {

            e.printStackTrace();

            return null;
        }

    }

    private void checkSuccess(Response response) {
        if (response != null) {
            checkSuccess(response.code());
            LogUtil.log(TAG, "response_code: " + response.code());
        }
    }

    private void checkSuccess(HttpResponse response) {
        if (response != null) {
            checkSuccess(response.getStatusLine().getStatusCode());

        }
    }

    private void checkSuccess(final int responseCode) {
        this.successfulRequest = ((responseCode == 200) || (responseCode == 201));
        LogUtil.log(TAG, "success: " + successfulRequest);
        if (!successfulRequest) {
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
            *  */
            if ((responseCode == 401) && !acceptUnauthorizedServerResponse) {
                try {
                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            //CommonUtil.styledToast(Application.getContext(), Constants.NETWORK_ERROR + responseCode);
                            /*if (responseCode == 401) {
                                AuthUtil.getInstance().loginTask();
                            }*/
                            //AuthUtil.getInstance().loginTask();
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
    }

    public Boolean getSuccess() {
        return successfulRequest;
    }

    private String cleanReturn(Response response) {
        ResponseBody body = null;
        if (response != null) {
            body = response.body();

        }
        checkSuccess(response);
        return getBody(body);

    }

    private String cleanReturn(HttpResponse response) {
        String body = null;


        try {
            if (response != null) {
                body = readFully(response.getEntity().getContent(), "utf-8");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        checkSuccess(response);
        return body;
    }

    public String readFully(InputStream inputStream, String encoding)
            throws IOException {
        return new String(readFully(inputStream), encoding);
    }

    private byte[] readFully(InputStream inputStream)
            throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }
        return baos.toByteArray();
    }

    private String getBody(ResponseBody body) {

        try {
            if (body != null) {

                try {
                    return body.string();
                } catch (IllegalCharsetNameException icne) {
                    icne.printStackTrace();
                    return null;
                }
            } else {
                LogUtil.log(TAG, "itsNull");
                return null;
            }
        } catch (IOException e) {
            /*
            If request failed you will get back null, so make sure to check getSuccess()
             */
            return null;
        } catch (UnsupportedCharsetException uce) {

            uce.printStackTrace();
            return null;
        } catch (NullPointerException npE) {
            npE.printStackTrace();
            return null;
        } catch (ArrayIndexOutOfBoundsException aio) {

            aio.printStackTrace();
            return null;
        }
    }
}