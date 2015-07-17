package com.moorango.medli.Fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moorango.medli.R;

/**
 * Created by cmac147 on 7/16/15.
 */
public class SignupFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_signup, null);

        return setupViews(rootView);
    }

    private View setupViews(View rootView) {

        return rootView;
    }
}
