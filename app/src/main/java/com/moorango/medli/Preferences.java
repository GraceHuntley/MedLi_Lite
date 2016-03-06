package com.moorango.medli;


import android.os.Bundle;
import android.preference.Preference;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.moorango.medli.utils.AuthUtil;

/**
 * Created by Colin on 9/12/2014.
 * Copyright 2014
 */
public class Preferences extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        Preference pref = getPreferenceManager().findPreference("log_out");
        pref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            public boolean onPreferenceClick(Preference preference) {
                if (preference.getKey().equals("log_out")) {
                    AuthUtil.getInstance().LogoutTask();
                    return true;
                }
                return false;
            }
        });
    }

}
