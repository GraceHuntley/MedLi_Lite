package com.moorango.medli;


import android.os.Bundle;

import com.github.machinarius.preferencefragment.PreferenceFragment;

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
    }

}
