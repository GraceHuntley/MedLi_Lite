package com.moorango.medli.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.moorango.medli.R;
import com.moorango.medli.lib.NavConstants;
import com.moorango.medli.lib.eventbus.EventBus;
import com.moorango.medli.lib.eventbus.events.FragmentSwitcher;

/**
 * Created by cmac147 on 7/16/15.
 */
public class WelcomeFragment extends Fragment {

    private Button loginButton, signupButton;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_welcome, null);

        return setupViews(rootView);
    }

    private View setupViews(View rootView) {

        loginButton = (Button) rootView.findViewById(R.id.login_button);
        signupButton = (Button) rootView.findViewById(R.id.signup_button);

        setupListeners();

        return rootView;
    }

    private void setupListeners() {
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new FragmentSwitcher(NavConstants.FRAGMENT_DATA.FragmentLogin));
            }
        });

        signupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new FragmentSwitcher(NavConstants.FRAGMENT_DATA.FragmentSignup));
            }
        });
    }
}
