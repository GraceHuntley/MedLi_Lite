package com.moorango.medli;

import android.app.Service;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;

public class MedLi_light extends ActionBarActivity implements Home.OnFragmentInteractionListener, MedSettings.OnFragmentInteractionListener,
        LogFragment.OnFragmentInteractionListener, MedList.OnFragmentInteractionListener {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_li_light);

        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {

            getSupportFragmentManager().beginTransaction()
                    .replace(android.R.id.content, new Home(), "home").addToBackStack("home")
                    .commit();

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

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new MedList(), "medList").addToBackStack("medList")
                        .commit();

                break;

            case R.id.action_add_med:

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new MedSettings(), "medSettings").addToBackStack("medSettings")
                        .commit();

                break;

            case R.id.action_med_logs:

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new LogFragment(), "logFragment").addToBackStack("logFragment")
                        .commit();

                break;

            case android.R.id.home:

                if (!getSupportFragmentManager().findFragmentByTag("home").isVisible()) {

                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new Home(), "home").addToBackStack("home")
                            .commit();

                }

                return super.onOptionsItemSelected(item);

        }

        return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(int tag) {
        FragmentManager fragmentManager;
        FragmentTransaction fragmentTransaction;
        switch(tag) {
            case 0:
                hideKeyboard();
                fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack();

                break;
            case 1:
                hideKeyboard();
                fragmentManager = getSupportFragmentManager();
                fragmentTransaction = fragmentManager.beginTransaction();

                if (fragmentManager.getBackStackEntryCount() > 1 && fragmentManager.getBackStackEntryAt(fragmentManager.getBackStackEntryCount() - 2) == fragmentManager.findFragmentByTag("home")) {
                    fragmentManager.popBackStack();
                } else {
                    Home home = new Home();
                    clearBackStack();
                    fragmentTransaction.replace(android.R.id.content, home, "home");
                    fragmentTransaction.addToBackStack("home");
                    fragmentTransaction.commit();
                }
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

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Service.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}