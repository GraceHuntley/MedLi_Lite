package com.moorango.medli;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MedLi_light extends ActionBarActivity implements Home.OnFragmentInteractionListener, MedSettings.OnFragmentInteractionListener,
        LogFragment.OnFragmentInteractionListener, MedList.OnFragmentInteractionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_li_light);


        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

            Home home = new Home();
            clearBackStack();
            fragmentTransaction.replace(R.id.fragment_holder, home, "home");
            fragmentTransaction.addToBackStack("home");
            fragmentTransaction.commit();
        }
    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.med_li_light, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;

        switch (item.getItemId()) {

            case R.id.action_edit_med:

                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                MedList medList = new MedList();

                fragmentTransaction.replace(R.id.fragment_holder, medList, "medList");
                fragmentTransaction.addToBackStack("medList");
                fragmentTransaction.commit();
                break;

            case R.id.action_add_med:

                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                MedSettings medSettings = new MedSettings();

                fragmentTransaction.replace(R.id.fragment_holder, medSettings, "medSettings");
                fragmentTransaction.addToBackStack("medSettings");
                fragmentTransaction.commit();
                break;

            case R.id.action_med_logs:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();
                LogFragment logFragment = new LogFragment();

                fragmentTransaction.replace(R.id.fragment_holder, logFragment, "logFragment");
                fragmentTransaction.addToBackStack("logFragment");
                fragmentTransaction.commit();
                break;

            case R.id.action_home:

                if (!getSupportFragmentManager().findFragmentByTag("home").isVisible()) {
                    if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                        getSupportFragmentManager().popBackStack();
                    }
                }



            default:
               /* Home home = new Home();

                fragmentTransaction.add(R.id.fragment_holder, home, "home");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit(); */
                return super.onOptionsItemSelected(item);


        }


        return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(int tag) {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        switch(tag) {
            case 0:
                fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack();
                break;
            case 1:
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();

                if (fragmentManager.getBackStackEntryCount() > 1 && fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 2) == fragmentManager.findFragmentByTag("home")) {
                    fragmentManager.popBackStack();
                } else {
                    Home home = new Home();
                    clearBackStack();
                    fragmentTransaction.replace(R.id.fragment_holder, home, "home");
                    fragmentTransaction.addToBackStack("home");
                    fragmentTransaction.commit();
                }

                break;
            case 2:

                break;

        }

    }

    public void onFragmentInteraction(int tag, String name) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        MedSettings medSettings = MedSettings.newInstance(name, true);

        fragmentTransaction.replace(R.id.fragment_holder, medSettings, "medSettings");
        fragmentTransaction.addToBackStack("medSettings");
        fragmentTransaction.commit();

    }

    private void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();

        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }
    }
}