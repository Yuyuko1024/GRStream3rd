package net.hearnsoft.gensokyoradio.trd.service;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
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

import com.blankj.utilcode.util.SPStaticUtils;

import net.hearnsoft.gensokyoradio.trd.BuildConfig;
import net.hearnsoft.gensokyoradio.trd.MainActivity;
import net.hearnsoft.gensokyoradio.trd.R;
import net.hearnsoft.gensokyoradio.trd.model.SongDataModel;
import net.hearnsoft.gensokyoradio.trd.utils.AudioSessionManager;
import net.hearnsoft.gensokyoradio.trd.utils.Constants;
import net.hearnsoft.gensokyoradio.trd.utils.ViewModelUtils;

public class GRStreamPlayerService extends MediaSessionService {

    private static final String TAG = GRStreamPlayerService.class.getSimpleName();

    private MediaSession session;
    private Intent intent;
    private int flag;
    private SongDataModel dataModel;
    private Observer<Boolean> networkObserver;

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
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Player status:" + isPlaying);
            }
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
                    session.getPlayer()
                            .replaceMediaItem(
                                    session.getPlayer().getCurrentMediaItemIndex(),
                                    updateMetadataInfo()
                            );
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
        DefaultMediaNotificationProvider provider = new DefaultMediaNotificationProvider.Builder(this)
                .build();
        provider.setSmallIcon(R.drawable.ic_icon_foreground);
        setMediaNotificationProvider(provider);

        intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP |
                Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT | Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY |
                Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            flag = PendingIntent.FLAG_IMMUTABLE;
        } else {
            flag = PendingIntent.FLAG_UPDATE_CURRENT;
        }

        ExoPlayer player = new ExoPlayer.Builder(this)
                .setAudioAttributes(AudioAttributes.DEFAULT, true)
                .build();
        player.setAudioSessionId(AudioSessionManager.getInstance().getAudioSessionId());

        session = new MediaSession.Builder(this, player)
                .setSessionActivity(getSingleTopActivity())
                .build();
        session.getPlayer().setMediaItem(updateMetadataInfo());
        session.getPlayer().setPlayWhenReady(false);
        session.getPlayer().prepare();
        session.getPlayer().addListener(playerListener);

        // 创建网络状态观察者
        networkObserver = isNetworkAvailable -> {
            if (!isNetworkAvailable && session != null && session.getPlayer() != null) {
                // 网络断开时暂停播放
                session.getPlayer().pause();
            }
        };

        // 使用 observeForever 观察网络状态
        dataModel.getNetworkStatus().observeForever(networkObserver);
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
                intent,
                flag
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
        String media_uri;
        switch (SPStaticUtils.getInt("server")) {
            case 1:
                media_uri = Constants.GR_STREAM_URL_MOBILE;
                break;
            case 2:
                media_uri = Constants.GR_STREAM_URL_ENHANCED;
                break;
            case 3:
                media_uri = SPStaticUtils.getString("custom_server",
                        Constants.GR_STREAM_URL_DEFAULT);
                break;
            case 0:
            default:
                media_uri = Constants.GR_STREAM_URL_DEFAULT;
                break;
        }
        return new MediaItem.Builder()
                .setMediaId("stream-1")
                .setUri(media_uri)
                .setMediaMetadata(new MediaMetadata.Builder()
                        .setTitle(title == null ? "null" : title)
                        .setArtist(artist == null ? "null" : artist)
                        .setArtworkUri(uri == null ? null : Uri.parse(uri))
                        .build())
                .build();
    }

    @Override
    public void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(receiver);
        session.getPlayer().release();
        session.release();
        session = null;
        // 移除观察者
        if (networkObserver != null) {
            dataModel.getNetworkStatus().removeObserver(networkObserver);
        }
        super.onDestroy();
    }

}
