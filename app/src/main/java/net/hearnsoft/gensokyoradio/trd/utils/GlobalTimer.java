package net.hearnsoft.gensokyoradio.trd.utils;

import android.util.Log;

import net.hearnsoft.gensokyoradio.trd.BuildConfig;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

public class GlobalTimer {
    private static final String TAG = GlobalTimer.class.getSimpleName();
    private static GlobalTimer instance;
    private static Timer timer;
    private final ArrayList<TimerUpdateListener> listeners = new ArrayList<>();
    private boolean isUpdateProgress = false;
    private int duration;
    private int played;
    private int remaining;
    private boolean isDebug = BuildConfig.DEBUG;


    public GlobalTimer() {
    }

    public static synchronized GlobalTimer getInstance() {
        if (instance == null) {
            instance = new GlobalTimer();
        }
        return instance;
    }

    public void startTimer(int duration, int played, int remaining) {
        if (isUpdateProgress && timer != null) {
            stopTimer();
        }
        isUpdateProgress = true;

        this.duration = duration;
        this.played = played;
        this.remaining = remaining;

        timer = new Timer();
        timer.schedule(new TimerTask() {
            int playedSec = played;
            @Override
            public void run() {
                if (playedSec == duration) {
                    stopTimer();
                } else {
                    playedSec++;
                    if (isDebug) {
                        Log.d(TAG, "GlobalTimer: onTimeUpdate: " +
                                playedSec + " " + duration + " " + remaining);
                    }
                    notifyListeners(duration, playedSec, remaining);
                }
            }
        },0, 1000);
    }

    public void stopTimer() {
        isUpdateProgress = false;
        timer.cancel();
        timer.purge();
        timer = null;
    }

    public void addListener(TimerUpdateListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(TimerUpdateListener listener) {
        listeners.remove(listener);
    }

    private void notifyListeners(int duration, int played, int remaining) {
        for (TimerUpdateListener listener : listeners) {
            listener.onTimeUpdate(duration, played, remaining);
        }
    }

}
