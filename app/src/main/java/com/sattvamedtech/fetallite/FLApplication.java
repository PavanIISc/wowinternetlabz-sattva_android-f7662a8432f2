package com.sattvamedtech.fetallite;

import android.app.Application;

public class FLApplication extends Application {

    private static FLApplication mInstance;
    public static boolean isFetalEnabled = true;
    public static String mPatientId = "";
    public static String mTestId = "";
    public static String mFileTimeStamp = "";

    @Override
    public void onCreate() {
        super.onCreate();
        mInstance = this;
    }

    public static FLApplication getInstance() {
        return mInstance;
    }
}
