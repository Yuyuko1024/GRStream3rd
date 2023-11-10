package net.hearnsoft.gensokyoradio.trd;

import android.app.Application;
import android.util.Log;

public class RadioApplication extends Application {

    private static final String TAG = RadioApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Init application.");
    }
}
