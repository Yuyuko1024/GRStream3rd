package net.hearnsoft.gensokyoradio.trd.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.OptIn;
import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.media3.common.AudioAttributes;
import androidx.media3.common.MediaItem;
import androidx.media3.common.MediaMetadata;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;
import androidx.media3.session.CommandButton;
import androidx.media3.session.MediaNotification;
import androidx.media3.session.MediaSession;
import androidx.media3.session.MediaSessionService;
import androidx.media3.session.MediaStyleNotificationHelper;

import com.google.common.collect.ImmutableList;

import net.hearnsoft.gensokyoradio.trd.MainActivity;
import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.model.SongDataModel;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;
import net.hearnsoft.gensokyoradio.trd.utils.ViewModelUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class GRStreamPlayerService extends MediaSessionService {

    private static final String TAG = GRStreamPlayerService.class.getSimpleName();

    private static Player player;
    private MediaSession session;
    private MediaItem streamItem;
    private SongDataModel dataModel;
    private Bitmap cover;
    private NotificationCompat.Builder notiBuilder;
    private NotificationCompat notification;
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
        setMediaNotificationProvider(new MediaNotification.Provider() {
            @Override
            public MediaNotification createNotification(MediaSession mediaSession, ImmutableList<CommandButton> customLayout, MediaNotification.ActionFactory actionFactory, Callback onNotificationChangedCallback) {
                createRadioNotification(mediaSession);
                return new MediaNotification(1, notiBuilder.build());
            }

            @Override
            public boolean handleCustomCommand(MediaSession session, String action, Bundle extras) {
                return false;
            }
        });
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
    public void onUpdateNotification(MediaSession session, boolean startInForegroundRequired) {
        super.onUpdateNotification(session, startInForegroundRequired);
    }

    @OptIn(markerClass = UnstableApi.class)
    private void createRadioNotification(MediaSession session) {
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        manager.createNotificationChannel(new NotificationChannel(Constants.NOTIFICATION_CHANNEL_ID, "RadioChannel", NotificationManager.IMPORTANCE_DEFAULT));
        notiBuilder = new NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
                .setShowWhen(false)
                .setStyle(new MediaStyleNotificationHelper.MediaStyle(session))
                .setColorized(true)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentIntent(PendingIntent.getActivity(
                        this,
                        0,
                        new Intent(this, MainActivity.class),
                        PendingIntent.FLAG_IMMUTABLE
                ));
    }

    private boolean downloadImage(String url) {
        boolean success = false;
        Bitmap bitmap = null;
        try {
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(10000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            //cover = IOUtils.toByteArray(is);
            bitmap = BitmapFactory.decodeStream(is);
            success = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (bitmap != null) {
                // 将Bitmap数据存入变量中
                cover = bitmap;
            }
        }
        return success;
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
