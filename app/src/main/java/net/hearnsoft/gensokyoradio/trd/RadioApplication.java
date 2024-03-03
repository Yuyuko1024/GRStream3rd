package net.hearnsoft.gensokyoradio.trd;

import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.util.Log;

import net.hearnsoft.gensokyoradio.trd.utils.AudioSessionManager;

public class RadioApplication extends Application {

    private static final String TAG = RadioApplication.class.getSimpleName();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Init application.");
        AudioSessionManager.getInstance().generateAudioSessionId(this.getApplicationContext());
        if (BuildConfig.DEBUG) Log.d(TAG, "generated audio session id="+
                AudioSessionManager.getInstance().getAudioSessionId());
    }
}
