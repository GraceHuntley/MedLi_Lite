package com.moorango.medli;
/**
 * Created by cmac147 on 5/19/15.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.multidex.MultiDex;
import android.support.multidex.MultiDexApplication;

import net.danlew.android.joda.JodaTimeAndroid;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

//import com.newrelic.agent.android.NewRelic;

/**
 * Space to handle floating instances of application.
 * Keep context when needed, get context when hard to grasp etc.
 */
public class Application extends MultiDexApplication {

    // Location updates intervals in sec


    private static WeakReference<Activity> activityInstance;
    private static Application instance;

    private static final String TAG = "com.moorango.medli.Application";

    public static List<String> pushNotifications = new ArrayList<>();

    public Application() {

        instance = this;


    }

    public static Application getInstance() {
        return instance;
    }

    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        JodaTimeAndroid.init(this);

        //LeakCanary.install(this);

      /*  if (Constants.PRODUCTION) {
            Fabric.with(this, new Crashlytics(), new Twitter(authConfig));
            //NewRelic.withApplicationToken("AAbafb5f23fac4860e451dad8c4f55df34c837a05f").start(this);
        } else {
            Fabric.with(this, new Twitter(authConfig));
        } */


        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                                               @Override
                                               public void onActivityCreated(Activity activity, Bundle bundle) {


                                               }

                                               @Override
                                               public void onActivityStarted(Activity activity) {

                                               }

                                               @Override
                                               public void onActivityResumed(Activity activity) {

                                                   activityInstance = new WeakReference<>(activity);

                                               }

                                               @Override
                                               public void onActivityPaused(Activity activity) {

                                               }

                                               @Override
                                               public void onActivityStopped(Activity activity) {

                                               }

                                               @Override
                                               public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {

                                               }

                                               @Override
                                               public void onActivityDestroyed(Activity activity) {

                                               }
                                           }

        );
    }

    public static synchronized Context getContext() {
        return instance;
    }

}