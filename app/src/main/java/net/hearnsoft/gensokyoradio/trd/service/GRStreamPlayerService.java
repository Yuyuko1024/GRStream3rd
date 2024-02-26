package net.hearnsoft.gensokyoradio.trd.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import net.hearnsoft.gensokyoradio.trd.MainActivity;
import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.model.SongDataModel;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;
import net.hearnsoft.gensokyoradio.trd.utils.ViewModelUtils;

public class GRStreamPlayerService extends MediaSessionService {

    private static final String TAG = GRStreamPlayerService.class.getSimpleName();

    private static Player player;
    private MediaSession session;
    private MediaItem streamItem;
    private SongDataModel dataModel;
    private ServiceBinder binder = new ServiceBinder();

    private Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            Player.Listener.super.onIsPlayingChanged(isPlaying);
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Player.Listener.super.onPlaybackStateChanged(playbackState);
            switch (playbackState) {
                case Player.STATE_IDLE:
                    dataModel. getBufferingState().postValue(0);
                    break;
                case Player.STATE_BUFFERING:
                    dataModel. getBufferingState().postValue(1);
                    break;
                case Player.STATE_READY:
                    dataModel. getBufferingState().postValue(2);
                default:
                    break;
            }
        }
    };

    public class ServiceBinder extends Binder {
        public GRStreamPlayerService getService() {
            return GRStreamPlayerService.this;
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @UnstableApi
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("net.hearnsoft.gensokyoradio.trd.UPDATE_NOTIFICATION".equals(action)) {
                updateMetadataInfo();
            }
        }
    };

    @UnstableApi
    @Override
    public void onCreate() {
        super.onCreate();
        // 获取全局ViewModel
        dataModel = ViewModelUtils.getViewModel(getApplication(), SongDataModel.class);
        registerBroadcasrReceiver();
        if (player == null) {
            initExoPlayer();
            session = new MediaSession.Builder(this, player)
                    .setSessionActivity(getSingleTopActivity())
                    .build();
        }
    }

    private PendingIntent getSingleTopActivity() {
        return PendingIntent.getActivity(
                this,
                0,
                new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE
        );
    }

    @Nullable
    @Override
    public MediaSession onGetSession(MediaSession.ControllerInfo controllerInfo) {
        return session;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        if (player.isPlaying()) {
            player.stop();
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    private void registerBroadcasrReceiver() {
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter("net.hearnsoft.gensokyoradio.trd.UPDATE_NOTIFICATION"));
    }

    @UnstableApi
    private void updateMetadataInfo() {
        Log.d(TAG, "replace new data");
        if (player != null && player.isPlaying()) {
            MediaItem newMetadataItem = new MediaItem.Builder()
                    .setMediaId("stream-1")
                    .setUri(Constants.GR_STREAM_URL)
                    .setMediaMetadata(new MediaMetadata.Builder()
                            .setTitle(dataModel.getTitle().getValue())
                            .setArtist(dataModel.getArtist().getValue())
                            .build())
                    .build();
            player.replaceMediaItem(player.getCurrentMediaItemIndex(), newMetadataItem);
        }
    }

    private void initExoPlayer() {
        player = new ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .build();
        streamItem = new MediaItem.Builder()
                .setMediaId("stream-1")
                .setUri(Constants.GR_STREAM_URL)
                .build();
        player.setMediaItem(streamItem);
        player.setPlayWhenReady(false);
        player.prepare();
        player.addListener(playerListener);
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
        super.onBind(intent);
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releaseExoPlayer();
        session.release();
        session = null;
    }

    private void releaseExoPlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }


}
