package com.moorango.medli;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;

import com.moorango.medli.Fragments.WelcomeFragment;
import com.moorango.medli.lib.eventbus.EventBus;
import com.moorango.medli.lib.eventbus.events.FragmentSwitcher;
import com.moorango.medli.utils.AuthUtil;

/**
 * Created by cmac147 on 7/16/15.
 */
public class OnboardingActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        EventBus.getInstance().register(this);
        if (!AuthUtil.getInstance().isLoggedIn() && AuthUtil.getInstance().canLogin()) {
            AuthUtil.getInstance().login();
        }

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

    public void onEvent(FragmentSwitcher destination) {
        Class<? extends Fragment> fragmentClass = destination.getFragment().getFragmentClass();
        try {
            Fragment fragment = fragmentClass.newInstance();

            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction()
                    .replace(R.id.container, fragment, fragment.getClass().getSimpleName())
                    .addToBackStack(fragment.getClass().getSimpleName())
                    .commit();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
