package com.moorango.medli.Fragments;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moorango.medli.R;
import com.moorango.medli.lib.network.API;
import com.moorango.medli.lib.network.RequestParams;
import com.moorango.medli.lib.network.URL;
import com.moorango.medli.utils.LogUtil;

/**
 * Created by cmac147 on 7/16/15.
 */
public class SignupFragment extends Fragment {


    private final String TAG = "SignupFragment";

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_signup, null);

        return setupViews(rootView);
    }

    private View setupViews(View rootView) {

        new SignUpTask().execute();

        return rootView;
    }

    public class SignUpTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... param) {
            RequestParams params = new RequestParams();
            params.put("email", "me@me.com");
            params.put("password", "12345678");
            params.put("password_confirmation", "12345678");
            //params.put("password", passwordEntry.getText().toString().trim());

            String registerResult = API.getInstance().post(URL.generateUnsecureURL("users"), params);

            LogUtil.log(TAG, registerResult);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            super.onPostExecute(aVoid);
        }
    }
}
