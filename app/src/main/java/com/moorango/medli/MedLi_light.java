package com.moorango.medli;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class MedLi_light extends ActionBarActivity implements Home.OnFragmentInteractionListener, MedSettings.OnFragmentInteractionListener {


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
            fragmentTransaction.addToBackStack(null);
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

        switch (item.getItemId()) {

            case R.id.action_edit_med:
                break;

            case R.id.action_add_med:

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                MedSettings medSettings = new MedSettings();

                fragmentTransaction.replace(R.id.fragment_holder, medSettings, "medSettings");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

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
        switch(tag) {
            case 0:
                fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack();
                break;
            case 1:
                fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                Home home = new Home();
                clearBackStack();
                fragmentTransaction.replace(R.id.fragment_holder, home, "home");
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
                break;

        }

    }

    private void clearBackStack() {
        FragmentManager fm = getSupportFragmentManager();

        for (int i = 0; i < fm.getBackStackEntryCount(); i++) {
            fm.popBackStack();
        }
    }
}