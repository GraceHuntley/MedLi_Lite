package com.moorango.medli;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

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
                                // .addToBackStack("home")
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_EmptyMedList(), "emptyList")
                                //.addToBackStack("emptyList")
                        .commit();
            }

        }

        final MedLiDataSource dataSource = MedLiDataSource.getHelper(this);
        if (dataSource.getPreferenceBool("agree_to_disclaimer")) {
            AlertDialog.Builder adB = new AlertDialog.Builder(this);

            View view = this.getLayoutInflater().inflate(R.layout.info_dialog, null);
            final CheckBox checkBox = (CheckBox) view.findViewById(R.id.no_show_checkbox);
            TextView tv1 = (TextView) view.findViewById(R.id.main_text);
            tv1.setText(getResources().getString(R.string.disclaimer));
            ((TextView) view.findViewById(R.id.dont_show_message)).setText("I Agree");
            adB.setView(view)
                    .setIcon(android.R.drawable.ic_dialog_alert)

                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (checkBox.isChecked()) {
                                dataSource.addOrUpdatePreference("agree_to_disclaimer", true);
                                dialogInterface.dismiss();

                            } else {
                                finish();
                            }
                        }
                    });
            adB.show();
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
                        .replace(android.R.id.content, new Fragment_MedSettings(), "medSettings")
                        .addToBackStack("medSettings")
                        .commit();

                break;

            case R.id.action_med_logs:

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_History(), "logFragment")
                        .addToBackStack("logFragment")
                        .commit();

                break;

            case R.id.action_settings:
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Preferences(), "preferences")
                        .addToBackStack("preferences")
                        .commit();
                break;

            case android.R.id.home:

                if (MedLiDataSource.getHelper(this).medListHasEntries()) {


                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new Fragment_Home(), "home").addToBackStack("home")
                            .commit();


                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new Fragment_EmptyMedList(), "emptyList")
                            .commit();
                }


                return super.onOptionsItemSelected(item);


        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {

        Fragment_Home fh = (Fragment_Home) getSupportFragmentManager().findFragmentByTag("home");
        Fragment_EmptyMedList feM = (Fragment_EmptyMedList) getSupportFragmentManager().findFragmentByTag("emptyList");

        if ((feM != null && feM.isVisible()) || (fh != null && fh.isVisible())) {
            finish();
        } else {
            if (MedLiDataSource.getHelper(this).medListHasEntries()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_Home(), "home")
                                // .addToBackStack("home")
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_EmptyMedList(), "emptyList")
                                //.addToBackStack("emptyList")
                        .commit();
            }

        }
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
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                            .commit();
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new Fragment_EmptyMedList(), "emptyList")
                            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
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