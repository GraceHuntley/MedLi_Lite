package com.moorango.medli;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.moorango.medli.lib.eventbus.EventBus;
import com.moorango.medli.lib.eventbus.events.FinishOnboardingEvent;
import com.moorango.medli.utils.AuthUtil;
import com.moorango.medli.utils.PreferencesUtil;

/**
 * Created by cmac147 on 3/2/16.
 */
public class MedliSplash extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getInstance().register(this);

        setContentView(R.layout.splash);

        if (AuthUtil.getInstance().isLoggedIn())
            launchMain();
        else if (hasSavedCredentials())
            startLogin();
        else
            startOnboarding();

    }

    private void launchMain() {
        Intent intent = new Intent(this, Activity_MedLi_light.class);
        startActivity(intent);
        this.finish();
    }

    private boolean hasSavedCredentials() {
        return (PreferencesUtil.getEmail() != null && PreferencesUtil.getPassword() != null);
    }

    private void startLogin() {
        AuthUtil.getInstance().LoginTask();
    }

    private void startOnboarding() {
        Intent intent = new Intent(this, Onboarding.class);
        startActivity(intent);
        this.finish();
    }

    public void onEvent(FinishOnboardingEvent event) {
        if (event.success()) {
            Toast.makeText(Application.getContext(), event.getMessage(), Toast.LENGTH_LONG).show();
            Intent intent = new Intent(this, Activity_MedLi_light.class);
            startActivity(intent);
            this.finish();
        }
    }
}
