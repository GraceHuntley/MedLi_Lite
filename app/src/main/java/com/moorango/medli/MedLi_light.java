package com.moorango.medli;

import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;


public class MedLi_light extends ActionBarActivity implements Home.OnFragmentInteractionListener, MedSettings.OnFragmentInteractionListener {

    FragmentManager fragmentManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_li_light);

        fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        Home home = new Home();
        fragmentTransaction.add(R.id.fragment_holder, home, "home");
        fragmentTransaction.commit();
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
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (item.getItemId()) {

            case R.id.action_edit_med:
                return true;

            case R.id.add_med:
                MedSettings medSettings = new MedSettings();

                fragmentTransaction.replace(R.id.fragment_holder, medSettings, "medSettings");
                fragmentTransaction.addToBackStack(null);

                fragmentTransaction.commit();
                return true;

            default:
                Home home = new Home();

                fragmentTransaction.add(R.id.fragment_holder, home, "home");
                fragmentTransaction.commit();
                return true;

        }

        //return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(Uri uri) {
        fragmentManager.popBackStack();
    }
}
