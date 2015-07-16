package com.moorango.medli;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.moorango.medli.Fragments.WelcomeFragment;
import com.moorango.medli.utils.AuthUtil;

/**
 * Created by cmac147 on 7/16/15.
 */
public class OnboardingActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        if (AuthUtil.getInstance().isLoggedIn()) {
            Intent intent = new Intent(this, Activity_MedLi_light.class);
            startActivity(intent);
            this.finish();
        } else {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .add(R.id.container, new WelcomeFragment(), WelcomeFragment.class.getSimpleName())
                    .addToBackStack(WelcomeFragment.class.getSimpleName())
                    .commit();

        }


    }
}
