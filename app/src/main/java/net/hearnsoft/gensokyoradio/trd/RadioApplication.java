package net.hearnsoft.gensokyoradio.trd;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

import net.hearnsoft.gensokyoradio.trd.db.SongHistoryDbHelper;
import net.hearnsoft.gensokyoradio.trd.service.WebSocketService;
import net.hearnsoft.gensokyoradio.trd.utils.AudioSessionManager;
import net.hearnsoft.gensokyoradio.trd.utils.GlobalTimer;

public class RadioApplication extends Application {

    private static final String TAG = RadioApplication.class.getSimpleName();
    private Intent websocketService;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Init application.");
        AudioSessionManager.getInstance().generateAudioSessionId(this.getApplicationContext());
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "generated audio session id="+
                AudioSessionManager.getInstance().getAudioSessionId());
        }
        SongHistoryDbHelper.getInstance(this);

        websocketService = new Intent(this, WebSocketService.class);
        startService(websocketService);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        if (websocketService != null) {
            stopService(websocketService);
            GlobalTimer.getInstance().stopTimer();
        }
    }
}
