package net.hearnsoft.gensokyoradio.trd.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
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
import androidx.media3.session.DefaultMediaNotificationProvider;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;

import net.hearnsoft.gensokyoradio.trd.BuildConfig;
import net.hearnsoft.gensokyoradio.trd.MainActivity;
import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.model.SongDataModel;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;
import net.hearnsoft.gensokyoradio.trd.utils.ViewModelUtils;

public class GRStreamPlayerService extends MediaSessionService {

    private static final String TAG = GRStreamPlayerService.class.getSimpleName();

    private MediaSession session;
    private SongDataModel dataModel;

    private final Player.Listener playerListener = new Player.Listener() {
        @Override
        public void onPlaybackStateChanged(int playbackState) {
            switch (playbackState) {
                case Player.STATE_IDLE:
                    dataModel.getBufferingState().postValue(0);
                    break;
                case Player.STATE_BUFFERING:
                    dataModel.getBufferingState().postValue(1);
                    break;
                case Player.STATE_READY:
                    dataModel.getBufferingState().postValue(2);
                default:
                    break;
            }
        }

        @Override
        public void onIsPlayingChanged(boolean isPlaying) {
            dataModel.getPlayerStatus().postValue(isPlaying);
            if (BuildConfig.DEBUG) Log.d(TAG, "Player status:" + isPlaying);
        }
    };

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @UnstableApi
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("net.hearnsoft.gensokyoradio.trd.UPDATE_NOTIFICATION".equals(action)) {
                Log.d(TAG, "replace new data");
                if (session != null) {
                    session.getPlayer().replaceMediaItem(session.getPlayer().getCurrentMediaItemIndex(), updateMetadataInfo());
                }
            }
        }
    };

    @UnstableApi
    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("hi","create player service");
        // 获取全局ViewModel
        dataModel = ViewModelUtils.getViewModel(getApplication(), SongDataModel.class);
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(receiver, new IntentFilter("net.hearnsoft.gensokyoradio.trd.UPDATE_NOTIFICATION"));
        setMediaNotificationProvider(new DefaultMediaNotificationProvider.Builder(this).build());
        session = new MediaSession.Builder(this, new ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .build())
                .setSessionActivity(getSingleTopActivity())
                .build();
        session.getPlayer().setMediaItem(updateMetadataInfo());
        session.getPlayer().setPlayWhenReady(false);
        session.getPlayer().prepare();
        session.getPlayer().addListener(playerListener);
    }

    @Override
    public void onTaskRemoved(@Nullable Intent rootIntent) {
        Player player = session.getPlayer();
        if (!player.getPlayWhenReady() || player.getMediaItemCount() == 0) {
            // Stop the service if not playing, continue playing in the background
            // otherwise.
            stopSelf();
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

    @UnstableApi
    private MediaItem updateMetadataInfo() {
        String title = dataModel.getTitle().getValue();
        String artist = dataModel.getArtist().getValue();
        String uri = dataModel.getCoverUrl().getValue();
        return new MediaItem.Builder()
                .setMediaId("stream-1")
                .setUri(Constants.GR_STREAM_URL)
                .setMediaMetadata(new MediaMetadata.Builder()
                        .setTitle(title == null ? "null" : title)
                        .setArtist(artist == null ? "null" : artist)
                        .setArtworkUri(uri == null ? null : Uri.parse(uri))
                        .build())
                .build();
    }

    public void playAndPauseStream() {
        if (session.getPlayer().isPlaying()) {
            session.getPlayer().pause();
            Toast.makeText(this, R.string.stream_stop_toast, Toast.LENGTH_SHORT).show();
        } else {
            session.getPlayer().play();
            Toast.makeText(this, R.string.stream_resume_toast, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        session.getPlayer().release();
        session.release();
        session = null;
        super.onDestroy();
    }

}
