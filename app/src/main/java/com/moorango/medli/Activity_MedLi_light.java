package com.moorango.medli;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Fragments.Fragment_EmptyMedList;
import com.moorango.medli.Fragments.Fragment_History;
import com.moorango.medli.Fragments.Fragment_Home;
import com.moorango.medli.Fragments.Fragment_MedSettings;

public class Activity_MedLi_light extends ActionBarActivity implements Fragment_Home.OnFragmentInteractionListener, Fragment_MedSettings.OnFragmentInteractionListener,
        Fragment_History.OnFragmentInteractionListener, Fragment_EmptyMedList.OnFragmentInteractionListener {

    private final String TAG = "Activity_MedLi_Light";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_li_light);

        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {

            if (MedLiDataSource.getHelper(this).medListHasEntries()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_Home(), "home")
                                //.addToBackStack("home")
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_EmptyMedList(), "emptyList")
                                //.addToBackStack("emptyList")
                        .commit();
            }

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

            case R.id.action_add_med:

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_MedSettings(), "medSettings").addToBackStack("medSettings")
                        .commit();

                break;

            case R.id.action_med_logs:

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_History(), "logFragment").addToBackStack("logFragment")
                        .commit();

                break;

            case android.R.id.home:

                if (MedLiDataSource.getHelper(this).medListHasEntries()) {


                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new Fragment_Home(), "home").addToBackStack("home")
                            .commit();


                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new Fragment_EmptyMedList(), "emptyList").addToBackStack("emptyList")
                            .commit();
                }

                return super.onOptionsItemSelected(item);

        }

        return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(int tag, String name, int id) {
        FragmentManager fragmentManager;

        switch (tag) {
            case 0:

                fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack();

                break;
            case 1:

                if (MedLiDataSource.getHelper(this).medListHasEntries()) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new Fragment_Home(), "home").addToBackStack("home")
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new Fragment_EmptyMedList(), "emptyList").addToBackStack("emptyList")
                            .commit();
                }
                break;

            case 2:

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_MedSettings(), "medSettings")
                        .addToBackStack("medSettings")
                        .commit();

                break;

            case 3:
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, Fragment_MedSettings.newInstance(name, true, id), "medSettings")
                        .addToBackStack("medSettings")
                        .commit();

                break;

            case 4:
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, Fragment_History.newInstance(), "logFragment")
                        .addToBackStack("logFragment")
                        .commit();
                break;

            case 5:

        }

    }

}