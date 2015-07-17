package com.moorango.medli.lib;

import android.support.v4.app.Fragment;

import com.moorango.medli.Fragments.LoginFragment;
import com.moorango.medli.Fragments.SignupFragment;

/**
 * Created by cmac147 on 7/16/15.
 */
public class NavConstants {

    public static enum FRAGMENT_DATA {

        FragmentLogin(LoginFragment.class),
        FragmentSignup(SignupFragment.class);

        Class<? extends Fragment> fragment;

        private FRAGMENT_DATA(Class<? extends Fragment> fragment) {
            this.fragment = fragment;
        }

        public Class<? extends Fragment> getFragmentClass() {
            return fragment;
        }
    }
}
