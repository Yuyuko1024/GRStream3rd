package net.hearnsoft.gensokyoradio.trd.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import androidx.media3.common.AudioAttributes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;
import androidx.media3.exoplayer.ExoPlayer;

import net.hearnsoft.gensokyoradio.trd.MainActivity;
import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;

public class GRStreamPlayerService extends Service{

    private static Player player;
    private ServiceBinder binder = new ServiceBinder();

    public class ServiceBinder extends Binder {
        public GRStreamPlayerService getService() {
            return GRStreamPlayerService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (player == null) {
            initExoPlayer();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void initExoPlayer() {
        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .build();
        MediaItem item = MediaItem.fromUri(Constants.GR_STREAM_URL);
        player.setMediaItem(item);
        player.setPlayWhenReady(false);
        player.prepare();
    }

    public void playAndPauseStream() {
        if (player != null && player.isPlaying()) {
            player.pause();
            releaseExoPlayer();
            Toast.makeText(this, R.string.stream_stop_toast, Toast.LENGTH_SHORT).show();
        } else {
            initExoPlayer();
            player.play();
            Toast.makeText(this, R.string.stream_resume_toast, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseExoPlayer();
    }

    private void releaseExoPlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }


}
