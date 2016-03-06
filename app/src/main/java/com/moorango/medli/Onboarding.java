package com.moorango.medli;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.moorango.medli.lib.eventbus.EventBus;
import com.moorango.medli.lib.eventbus.events.FinishOnboardingEvent;
import com.moorango.medli.utils.AuthUtil;
import com.moorango.medli.utils.LogUtil;
import com.moorango.medli.utils.PreferencesUtil;

/**
 * Created by cmac147 on 3/4/16.
 */
public class Onboarding extends Activity {

    private static final String TAG = "Onboarding.java";
    LinearLayout welcomeBox, signinBox, signupBox;
    Button signinBtn, signupBtn;
    EditText emailEntry, passwordEntry;
    EditText suEmailEntry, suPasswordEntry, suPasswordConfirm, suFirstName, suLastName;
    int SIGN_IN = 0;
    int SIGN_UP = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.onboarding);

        EventBus.getInstance().register(this);

        setupViews();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void setupViews() {

        welcomeBox = (LinearLayout) findViewById(R.id.welcome_box);
        signinBox = (LinearLayout) findViewById(R.id.signin_box);
        signupBox = (LinearLayout) findViewById(R.id.signup_box);

        signinBtn = (Button) findViewById(R.id.sign_in_button);
        signupBtn = (Button) findViewById(R.id.sign_up_button);

        emailEntry = (EditText) findViewById(R.id.email_login);
        passwordEntry = (EditText) findViewById(R.id.password_login);

        suEmailEntry = (EditText) findViewById(R.id.email_signup);
        suPasswordEntry = (EditText) findViewById(R.id.password_signup);
        suPasswordConfirm = (EditText) findViewById(R.id.password_signup_confirm);
        suFirstName = (EditText) findViewById(R.id.first_name);
        suLastName = (EditText) findViewById(R.id.last_name);

        setupListeners();
    }

    private void setupListeners() {

        signinBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (signinBtn.getCurrentTextColor() == Color.BLUE) {
                    signIn();
                } else {
                    toggleViews(SIGN_IN);
                    signinBtn.setTextColor(Color.BLUE);
                    signupBtn.setTextColor(Color.BLACK);
                }
            }
        });

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (signupBtn.getCurrentTextColor() == Color.BLUE) {
                    signUp();
                } else {
                    toggleViews(SIGN_UP);
                    signupBtn.setTextColor(Color.BLUE);
                    signinBtn.setTextColor(Color.BLACK);
                }
            }
        });
    }

    private void toggleViews(int action) {
        if (welcomeBox.getVisibility() == View.VISIBLE)
            welcomeBox.setVisibility(View.GONE);

        if (action == SIGN_IN) {
            signupBox.setVisibility(View.GONE);
            signinBox.setVisibility(View.VISIBLE);
        } else {
            signinBox.setVisibility(View.GONE);
            signupBox.setVisibility(View.VISIBLE);
        }

    }

    private void signIn() {
        if (validateEntries(SIGN_IN)) {
            PreferencesUtil.setEmail(emailEntry.getText().toString());
            PreferencesUtil.setPassword(passwordEntry.getText().toString());
            AuthUtil.getInstance().LoginTask();
        } else {
            Toast.makeText(this, "Missing information", Toast.LENGTH_LONG).show();
        }
    }

    private void signUp() {
        LogUtil.log(TAG, "signUPPressed.");
        if (validateEntries(SIGN_UP)) {
            PreferencesUtil.setEmail(suEmailEntry.getText().toString());
            PreferencesUtil.setPassword(suPasswordEntry.getText().toString());
            PreferencesUtil.setFirstName(suFirstName.getText().toString());
            PreferencesUtil.setLastName(suLastName.getText().toString());
            AuthUtil.getInstance().SignUpTask();
        } else {
            Toast.makeText(this, "Missing informtion", Toast.LENGTH_LONG).show();
        }
    }

    private boolean validateEntries(int type) {

        if (type == SIGN_IN) {
            // TODO require stronger password. Upper/Lower case, Numbers and Letters.
            return android.util.Patterns.EMAIL_ADDRESS.matcher(emailEntry.getText()).matches() && passwordEntry.length() > 8;
        } else {
            if (suLastName.getText().toString().length() >= 1
                    && suFirstName.getText().toString().length() >= 2
                    && Patterns.EMAIL_ADDRESS.matcher(suEmailEntry.getText()).matches()
                    && suPasswordEntry.getText().toString().equals(suPasswordConfirm.getText().toString())
                    && suPasswordEntry.length() > 8)
                return true;
            else
                markErrors(SIGN_UP);
            return false;


        }
    }

    public void onEvent(FinishOnboardingEvent event) {
        if (event.success()) {
            Toast.makeText(Application.getContext(), event.getMessage(), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, Activity_MedLi_light.class);
            startActivity(intent);
            this.finish();
        }
    }

    private void markErrors(int type) {
        if (type == SIGN_IN) {

        } else {
            suFirstName.setTextColor(suFirstName.getText().toString().length() >= 2 ? Color.BLACK : Color.RED);
            suLastName.setTextColor(suLastName.getText().toString().length() >= 1 ? Color.BLACK : Color.RED);
            suEmailEntry.setTextColor(Patterns.EMAIL_ADDRESS.matcher(suEmailEntry.getText()).matches() ? Color.BLACK : Color.RED);
            suPasswordConfirm.setTextColor(suPasswordEntry.getText().toString().equals(suPasswordConfirm.getText().toString()) ? Color.BLACK : Color.RED);
            suPasswordEntry.setTextColor(suPasswordEntry.getText().toString().length() >= 8 ? Color.BLACK : Color.RED);
        }
    }
}
