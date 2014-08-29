package com.moorango.medli;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class Activity_MedLi_light extends ActionBarActivity implements Fragment_Home.OnFragmentInteractionListener, Fragment_MedSettings.OnFragmentInteractionListener,
        Fragment_History.OnFragmentInteractionListener, Fragment_EmptyMedList.OnFragmentInteractionListener {

    // Constants
    // The authority for the sync adapter's content provider
    public static final String AUTHORITY = "com.moorango.medli.provider";
    // An account type, in the form of a domain name
    public static final String ACCOUNT_TYPE = "medli.moorango.com";
    // The account name
    public static final String ACCOUNT = "dummyaccount";
    // Instance fields
    Account mAccount;

    // Global variables
    // A content resolver for accessing the provider
    ContentResolver mResolver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_med_li_light);

        getSupportActionBar().setHomeButtonEnabled(true);

        if (savedInstanceState == null) {

            if (MedLiDataSource.getHelper(this).medListHasEntries()) {
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_Home(), "home").addToBackStack("home")
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_EmptyMedList(), "emptyList").addToBackStack("emptyList")
                        .commit();
            }

        }

        // Create the dummy account
        mAccount = CreateSyncAccount(this);

        // Get the content resolver for your app
        mResolver = getContentResolver();
        // Turn on automatic syncing for the default account and authority
        mResolver.setSyncAutomatically(mAccount, AUTHORITY, true);

    }

    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(
                ACCOUNT, ACCOUNT_TYPE);
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(
                        ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
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
                    if (!getSupportFragmentManager().findFragmentByTag("home").isVisible()) {

                        getSupportFragmentManager().beginTransaction()
                                .replace(android.R.id.content, new Fragment_Home(), "home").addToBackStack("home")
                                .commit();

                    }
                } else {
                    getSupportFragmentManager().beginTransaction()
                            .replace(android.R.id.content, new Fragment_EmptyMedList(), "emptyList").addToBackStack("emptyList")
                            .commit();
                }

                return super.onOptionsItemSelected(item);

        }

        return super.onOptionsItemSelected(item);
    }

    public void onFragmentInteraction(int tag, String name) {
        FragmentManager fragmentManager;

        switch (tag) {
            case 0:

                fragmentManager = getSupportFragmentManager();
                fragmentManager.popBackStack();

                break;
            case 1:

                getSupportFragmentManager().beginTransaction().replace(android.R.id.content, new Fragment_Home(), "home")
                        .addToBackStack("home")
                        .commit();
                break;

            case 2:

                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, new Fragment_MedSettings(), "medSettings")
                        .addToBackStack("medSettings")
                        .commit();

                break;

            case 3:
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, Fragment_MedSettings.newInstance(name, true), "medSettings")
                        .addToBackStack("medSettings")
                        .commit();

                break;

            case 4:
                getSupportFragmentManager().beginTransaction()
                        .replace(android.R.id.content, Fragment_History.newInstance(null, null), "logFragment")
                        .addToBackStack("logFragment")
                        .commit();
        }

    }

}