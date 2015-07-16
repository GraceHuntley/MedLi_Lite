package com.moorango.medli;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.moorango.medli.Data.MedLiDataSource;
import com.moorango.medli.Fragments.Fragment_EmptyMedList;
import com.moorango.medli.Fragments.Fragment_History;
import com.moorango.medli.Fragments.Fragment_Home;
import com.moorango.medli.Fragments.Fragment_MedSettings;
import com.moorango.medli.lib.network.API;
import com.moorango.medli.lib.network.RequestParams;
import com.moorango.medli.lib.network.URL;
import com.moorango.medli.utils.LogUtil;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

public class Activity_MedLi_light extends ActionBarActivity implements Fragment_Home.OnFragmentInteractionListener, Fragment_MedSettings.OnFragmentInteractionListener,
        Fragment_History.OnFragmentInteractionListener, Fragment_EmptyMedList.OnFragmentInteractionListener {

    private final String TAG = "Activity_MedLi_Light";

    Tracker ga;

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        // --Commented out by Inspection (9/18/2014 1:20 PM):GLOBAL_TRACKER, // Tracker used by all the apps from a company. eg: roll-up tracking.
        // --Commented out by Inspection (9/18/2014 1:20 PM):ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a company.
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();


    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            Tracker t = analytics.newTracker("UA-54927508-1");
            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_med_li_light);

        //ga = GoogleAnalytics.getInstance(this).newTracker("UA-54927508-1");

        ga = getTracker(TrackerName.APP_TRACKER);

        ga.send(new HitBuilders.EventBuilder().setAction("ActivityLoaded").build());

        new FetchDataTask().execute();


        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {

            if (MedLiDataSource.getHelper(this).medListHasEntries()) {

                ComponentName receiver = new ComponentName(this, StartAlarms.class);
                PackageManager pm = this.getPackageManager();

                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP);

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_Home(), "home")
                                // .addToBackStack("home")
                        .commit();
            } else {

                ComponentName receiver = new ComponentName(this, StartAlarms.class);
                PackageManager pm = this.getPackageManager();

                pm.setComponentEnabledSetting(receiver,
                        PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                        PackageManager.DONT_KILL_APP);

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_EmptyMedList(), "emptyList")
                                //.addToBackStack("emptyList")
                        .commit();
            }

        }

        final MedLiDataSource dataSource = MedLiDataSource.getHelper(this);
        if (dataSource.getPreferenceBool("agree_to_disclaimer")) {
            AlertDialog.Builder adB = new AlertDialog.Builder(this);
            adB.setInverseBackgroundForced(true); // fixed bug in older versions of android.
            View view = this.getLayoutInflater().inflate(R.layout.info_dialog, null);
            (view.findViewById(R.id.no_show_checkbox)).setVisibility(View.GONE);
            (view.findViewById(R.id.dont_show_message)).setVisibility(View.GONE);
            TextView tv1 = (TextView) view.findViewById(R.id.main_text);
            tv1.setText(getResources().getString(R.string.disclaimer));
            ((TextView) view.findViewById(R.id.dont_show_message)).setText("I Agree");
            adB.setView(view)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .setNegativeButton("I Disagree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            finish();
                        }
                    })
                    .setPositiveButton("I Agree", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {

                            dataSource.addOrUpdatePreference("agree_to_disclaimer", false);
                            dialogInterface.dismiss();
                            ga.send(new HitBuilders.EventBuilder().setAction("AgreedToDisclaimer").build());

                        }
                    });
            adB.show();
        }

    }

    private class FetchDataTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... param) {

            RequestParams params = new RequestParams();
            params.put("email", "test@test.com");
            params.put("password", "test1");
            params.put("password_confirmation", "test1");
            //params.put("encrypted_password", AuthUtil.encryptPassword("test"));

            String registerResult = API.getInstance().post(URL.generateUnsecureURL("users"), params);
            //String result = API.getInstance().get(URL.generateUnsecureURL("users/index"), params);
            //String result = API.getInstance().post(URL.generateUnsecureURL("users/index"), params);
            LogUtil.log(TAG, registerResult);
            //LogUtil.log(TAG, result);

            return null;
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
                ga.send(new HitBuilders.EventBuilder().setAction(name).build());
                break;
        }
    }
}