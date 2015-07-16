package com.moorango.medli;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;

import com.moorango.medli.utils.LogUtil;

import java.lang.ref.WeakReference;

public class Application extends android.app.Application {

    private static Application instance;

    private static WeakReference<Activity> topMostActivity;
    private static final String TAG = "Application";

    public Application() {
        instance = this;
    }

    public static Application getInstance() {
        return instance;
    }

    public static synchronized Context getContext() {
        return instance;
    }


    public static Context getActivityContext() {
        if (topMostActivity != null) {
            return topMostActivity.get();
        }
        return null;
    }


    public void onCreate() {
        super.onCreate();

        this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

                LogUtil.log(TAG, activity.getClass().getSimpleName() + " created");
            }

            @Override
            public void onActivityStarted(Activity activity) {
            }

            @Override
            public void onActivityResumed(Activity activity) {

                topMostActivity = new WeakReference<>(activity);


                LogUtil.log(TAG, activity.getClass().getSimpleName() + " resumed");

            }

            @Override
            public void onActivityPaused(Activity activity) {
                topMostActivity = null;

            }

            @Override
            public void onActivityStopped(Activity activity) {

            }

            @Override
            public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

            }

            @Override
            public void onActivityDestroyed(Activity activity) {
                LogUtil.log(TAG, activity.getClass().getSimpleName() + " destroyed");


            }
        });
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
        topMostActivity = null;


    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();

    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        //LogUtil.log(TAG, "onTrimMemory");

        switch (level) {
            case TRIM_MEMORY_RUNNING_CRITICAL:
                //JsonCache.getInstance().clearCache();

                break;
            case TRIM_MEMORY_RUNNING_LOW:

                //LogUtil.log(TAG, "MEMORY_LOW");
                break;
            case TRIM_MEMORY_RUNNING_MODERATE:

                //LogUtil.log(TAG, "MEMORY_MODERATE");
                break;

            case TRIM_MEMORY_UI_HIDDEN:

                //VideoCastManager.getInstance().decrementUiCounter();
                System.gc();
                LogUtil.log(TAG, "UI_HIDDEN");
                //Picasso.with(getContext()).shutdown();
                break;
        }
    }
}
