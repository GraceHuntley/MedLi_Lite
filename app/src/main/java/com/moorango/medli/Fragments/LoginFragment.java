package com.moorango.medli.Fragments;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.moorango.medli.Activity_MedLi_light;
import com.moorango.medli.R;
import com.moorango.medli.lib.Constants;
import com.moorango.medli.utils.AuthUtil;
import com.moorango.medli.utils.LogUtil;
import com.moorango.medli.utils.PreferenceUtil;

/**
 * Created by cmac147 on 7/16/15.
 */
public class LoginFragment extends Fragment {

    public static final String TAG = "LoginFragment";

    private Button loginButton;
    private EditText emailEntry, passwordEntry;

    private boolean entriesValid = false;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_login, null);

        return setupViews(rootView);
    }

    private View setupViews(View rootView) {

        emailEntry = (EditText) rootView.findViewById(R.id.email_entry);
        passwordEntry = (EditText) rootView.findViewById(R.id.password_entry);

        loginButton = (Button) rootView.findViewById(R.id.login_button);

        setupListeners();
        return rootView;
    }

    private void setupListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtil.log(TAG, "click");
                if (entriesValid) {
                    (new LoginTask()).execute();
                } else {
                    LogUtil.log(TAG, "notValid");
                }


            }
        });

        emailEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (passwordEntry.getText().length() >= Constants.PASSWORD_MINIMUM && emailIsValid()) {
                    entriesValid = true;
                } else {
                    entriesValid = false;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        passwordEntry.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (count >= Constants.PASSWORD_MINIMUM && emailIsValid()) {
                    entriesValid = true;
                } else {
                    entriesValid = false;
                }

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean emailIsValid() {
        return Patterns.EMAIL_ADDRESS.matcher(emailEntry.getText()).matches();
    }

    public class LoginTask extends AsyncTask<Void, Void, Void> {

        boolean success = false;

        @Override
        protected Void doInBackground(Void... param) {


            PreferenceUtil.setEmail(emailEntry.getText().toString().trim());
            PreferenceUtil.setPassword(passwordEntry.getText().toString().trim());
            //RequestParams params = new RequestParams();
            //params.put("email", emailEntry.getText().toString().trim());
            //params.put("password", passwordEntry.getText().toString().trim());
            //params.put("password", passwordEntry.getText().toString().trim());

            AuthUtil.getInstance().login();

            //String registerResult = API.getInstance().post(URL.generateUnsecureURL("sessions"), params);

            //LogUtil.log(TAG, registerResult);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (AuthUtil.getInstance().isLoggedIn()) {
                LogUtil.log(TAG, "success");
                Intent intent = new Intent(getActivity(), Activity_MedLi_light.class);
                startActivity(intent);
                getActivity().finish();
            }

            super.onPostExecute(aVoid);
        }
    }
}
