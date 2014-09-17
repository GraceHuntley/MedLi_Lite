package com.moorango.medli;


import android.test.ActivityInstrumentationTestCase2;

import com.robotium.solo.Solo;


/**
 * <a href="http://d.android.com/tools/testing/testing_android.html">Testing Fundamentals</a>
 */
public class Activity_MedLi_lightTest extends ActivityInstrumentationTestCase2<Activity_MedLi_light> {


    public Activity_MedLi_lightTest() {
        super(Activity_MedLi_light.class);
    }


    private Solo solo;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        solo = new Solo(getInstrumentation(), getActivity());


    }

    public void testSomething() {


    }
}