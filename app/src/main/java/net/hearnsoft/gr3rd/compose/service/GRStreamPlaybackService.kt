package net.hearnsoft.gr3rd.compose.service

import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.DefaultMediaNotificationProvider
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.blankj.utilcode.util.SPStaticUtils
import com.moriafly.salt.ui.UnstableSaltUiApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import net.hearnsoft.gr3rd.compose.MainActivity
import net.hearnsoft.gr3rd.compose.R
import net.hearnsoft.gr3rd.compose.ui.viewmodel.MediaUpdateData
import net.hearnsoft.gr3rd.compose.ui.viewmodel.SongViewModel
import net.hearnsoft.gr3rd.compose.utils.AudioSessionManager
import net.hearnsoft.gr3rd.compose.utils.Constants
import net.hearnsoft.gr3rd.compose.utils.Logger
import androidx.core.net.toUri

@UnstableApi
@UnstableSaltUiApi
class GRStreamPlaybackService : MediaSessionService() {
    companion object {
        const val TAG = "GRStreamPlaybackService"
    }

    private val serviceScope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    // 使用共享的ViewModel实例
    private val songViewModel by lazy {
        SongViewModel.getInstance()
    }

    private lateinit var session: MediaSession
    private lateinit var player: ExoPlayer
    private lateinit var intent: Intent

    override fun onCreate() {
        super.onCreate()

        initializePlayer()
        initializeMediaSession()
        prepareMediaItem()
        observeMediaUpdates()
        observePlayerState()
    }

    private fun initializePlayer() {
        // 创建播放器
        player = ExoPlayer.Builder(this)
            .setAudioAttributes(AudioAttributes.DEFAULT, true)
            .build()
            .apply {
                audioSessionId = AudioSessionManager.getAudioSessionIdOrDefault()
                setHandleAudioBecomingNoisy(true)
            }

        // 设置播放器监听器
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                songViewModel.updatePlayerStatus(isPlaying)
                Logger.debug(TAG, "Player state changed: isPlaying=$isPlaying")
            }

            override fun onPlaybackStateChanged(playbackState: Int) {
                val bufferingState = when (playbackState) {
                    Player.STATE_IDLE -> 0
                    Player.STATE_BUFFERING -> 1
                    Player.STATE_READY -> 2
                    Player.STATE_ENDED -> -1
                    else -> 0
                }
                songViewModel.updateBufferingState(bufferingState)
                songViewModel.updatePlayButtonStatus(playbackState != Player.STATE_IDLE)

                Logger.debug(TAG, "Playback state changed: $playbackState, buffering state: $bufferingState")
            }
        })
    }


    private fun initializeMediaSession() {
        // 设置媒体通知提供程序
        val notificationProvider: DefaultMediaNotificationProvider =
            DefaultMediaNotificationProvider.Builder(this)
                .build()
                .apply {
                    setSmallIcon(R.drawable.ic_icon_foreground)
                }
        setMediaNotificationProvider(notificationProvider)
        // 创建媒体唤起界面
        intent = Intent(this, MainActivity::class.java).apply {
            flags = (Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP or
                    Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT or Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY or
                    Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
        // 创建PendingIntent
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                PendingIntent.FLAG_IMMUTABLE
            } else {
                PendingIntent.FLAG_UPDATE_CURRENT
            }
        )

        // 创建媒体会话
        session = MediaSession.Builder(this, player)
            .setSessionActivity(pendingIntent)
            .build()

    }

    private fun prepareMediaItem() {
        val mediaItem = updateMetadataInfo()
        player.setMediaItem(mediaItem)
        player.playWhenReady = false
        player.prepare()

        Logger.debug(TAG, "Media item prepared with URI: ${mediaItem.localConfiguration?.uri}")
    }

    private fun observeMediaUpdates() {
        // 观察媒体更新事件
        serviceScope.launch {
            songViewModel.mediaUpdateEvent.collect { updateData ->
                Logger.debug(TAG, "Received media update: ${updateData.title}")
                updateMediaMetadata(updateData)
            }
        }
    }

    private fun observePlayerState() {
        // 观察网络状态
        serviceScope.launch {
            songViewModel.networkStatus.collect { isConnected ->
                if (!isConnected && player.isPlaying) {
                    player.pause()
                    Logger.info(TAG, "Network lost, pausing playback")
                }
            }
        }

        // 观察 WebSocket 连接状态
        serviceScope.launch {
            songViewModel.webSocketConnected.collect { isConnected ->
                songViewModel.updatePlayButtonStatus(isConnected)
                Logger.debug(TAG, "WebSocket status changed: $isConnected")
            }
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return session
    }

    @UnstableApi
    private fun updateMetadataInfo(): MediaItem {
        val title = songViewModel.title.value
        val artist = songViewModel.artist.value
        val coverUrl = songViewModel.coverUrl.value

        val mediaUri = getSelectedStreamUrl()

        return MediaItem.Builder()
            .setMediaId("gensokyo-radio-stream")
            .setUri(mediaUri)
            .setMediaMetadata(buildMediaMetadata(title, artist, coverUrl))
            .build()
    }

    private fun getSelectedStreamUrl(): String {
        return when (SPStaticUtils.getInt(Constants.PREF_SERVER, 0)) {
            1 -> Constants.GR_STREAM_URL_MOBILE
            2 -> Constants.GR_STREAM_URL_ENHANCED
            3 -> SPStaticUtils.getString(Constants.PREF_CUSTOM_SERVER, Constants.GR_STREAM_URL_DEFAULT)
            else -> Constants.GR_STREAM_URL_DEFAULT
        }
    }

    private fun buildMediaMetadata(title: String, artist: String, coverUrl: String): MediaMetadata {
        return MediaMetadata.Builder()
            .setTitle(title.ifEmpty { "Gensokyo Radio" })
            .setArtist(artist.ifEmpty { "Loading..." })
            .setAlbumTitle("Live Stream")
            .apply {
                coverUrl.takeIf { it.isNotEmpty() }?.let { url ->
                    try {
                        setArtworkUri(url.toUri())
                    } catch (e: Exception) {
                        Logger.err(TAG, "Error parsing artwork URI: $url", e)
                    }
                }
            }
            .build()
    }

    private fun updateMediaMetadata(updateData: MediaUpdateData) {
        try {
            val currentMediaItem = player.currentMediaItem ?: return

            val newMetadata = MediaMetadata.Builder()
                .setTitle(updateData.title)
                .setArtist(updateData.artist)
                .setAlbumTitle(updateData.album)
                .apply {
                    if (updateData.coverUrl.isNotEmpty()) {
                        try {
                            setArtworkUri(updateData.coverUrl.toUri())
                        } catch (e: Exception) {
                            Logger.err(TAG, "Error parsing artwork URI in update: ${updateData.coverUrl}", e)
                        }
                    }
                }
                .build()

            val newMediaItem = currentMediaItem.buildUpon()
                .setMediaMetadata(newMetadata)
                .build()

            // 替换当前媒体项的元数据
            player.replaceMediaItem(player.currentMediaItemIndex, newMediaItem)

            Logger.info(TAG, "Updated media metadata: ${updateData.title} by ${updateData.artist}")
        } catch (e: Exception) {
            Logger.err(TAG, "Error updating media metadata", e)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        val player = session.player
        if (!player.playWhenReady || player.mediaItemCount == 0) {
            // 如果没有播放或没有媒体项，则停止服务
            stopSelf()
        }
        super.onTaskRemoved(rootIntent)
    }

    override fun onDestroy() {
        Logger.info(TAG, "Destroying GRStreamPlaybackService")

        serviceScope.cancel()

        // 清理播放器和会话
        player.release()
        session.release()

        super.onDestroy()
    }

}