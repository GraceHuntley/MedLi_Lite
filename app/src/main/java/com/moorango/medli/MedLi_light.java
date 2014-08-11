package com.moorango.medli;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

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

        switch (tag) {
            case 0:

                fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack();

                break;
            case 1:


                getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new Home(), "home")
                        .addToBackStack("home")
                        .commit();

                break;

        }

    }

    public void onFragmentInteraction(int tag, String name) {

        getSupportFragmentManager().beginTransaction().replace(android.R.id.content, MedSettings.newInstance(name, true), "medSettings")
                .addToBackStack("medSettings").commit();

    }

}