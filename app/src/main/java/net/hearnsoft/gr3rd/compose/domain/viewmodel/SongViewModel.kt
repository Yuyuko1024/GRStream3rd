package net.hearnsoft.gr3rd.compose.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.hearnsoft.gr3rd.compose.domain.beans.NowPlayingData
import net.hearnsoft.gr3rd.compose.utils.Constants
import net.hearnsoft.gr3rd.compose.utils.GlobalTimer

class SongViewModel : ViewModel() {
    // 基本歌曲信息 - 用于UI显示
    private val _title = MutableStateFlow("Loading...")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _artist = MutableStateFlow("Loading...")
    val artist: StateFlow<String> = _artist.asStateFlow()

    private val _album = MutableStateFlow("Loading...")
    val album: StateFlow<String> = _album.asStateFlow()

    private val _coverUrl = MutableStateFlow("https://gensokyoradio.net/images/assets/gr-logo-placeholder.png")
    val coverUrl: StateFlow<String> = _coverUrl.asStateFlow()

    // 播放状态
    private val _playerStatus = MutableStateFlow(false)
    val playerStatus: StateFlow<Boolean> = _playerStatus.asStateFlow()

    private val _playBtnStatus = MutableStateFlow(false)
    val playBtnStatus: StateFlow<Boolean> = _playBtnStatus.asStateFlow()

    // 缓冲状态
    private val _bufferingState = MutableStateFlow(0)
    val bufferingState: StateFlow<Int> = _bufferingState.asStateFlow()

    // 网络状态
    private val _networkStatus = MutableStateFlow(true)
    val networkStatus: StateFlow<Boolean> = _networkStatus.asStateFlow()

    // 可视化器状态
    private val _showVisualizer = MutableStateFlow(false)
    val showVisualizer: StateFlow<Boolean> = _showVisualizer.asStateFlow()

    private val _visualizerUsable = MutableStateFlow(false)
    val visualizerUsable: StateFlow<Boolean> = _visualizerUsable.asStateFlow()

    // 当前播放详细信息
    private val _nowPlayingTitle = MutableStateFlow("")
    val nowPlayingTitle: StateFlow<String> = _nowPlayingTitle.asStateFlow()

    private val _nowPlayingArtist = MutableStateFlow("")
    val nowPlayingArtist: StateFlow<String> = _nowPlayingArtist.asStateFlow()

    private val _nowPlayingAlbum = MutableStateFlow("")
    val nowPlayingAlbum: StateFlow<String> = _nowPlayingAlbum.asStateFlow()

    private val _nowPlayingYears = MutableStateFlow("")
    val nowPlayingYears: StateFlow<String> = _nowPlayingYears.asStateFlow()

    private val _nowPlayingCircle = MutableStateFlow("")
    val nowPlayingCircle: StateFlow<String> = _nowPlayingCircle.asStateFlow()

    // 更新标志
    private val _isUpdatedInfo = MutableStateFlow(false)
    val isUpdatedInfo: StateFlow<Boolean> = _isUpdatedInfo.asStateFlow()

    // 媒体更新事件 - 用于通知 MediaSessionService
    private val _mediaUpdateEvent = MutableSharedFlow<MediaUpdateData>(
        replay = 0,
        extraBufferCapacity = 1
    )
    val mediaUpdateEvent: SharedFlow<MediaUpdateData> = _mediaUpdateEvent.asSharedFlow()

    // WebSocket 状态
    private val _webSocketConnected = MutableStateFlow(false)
    val webSocketConnected: StateFlow<Boolean> = _webSocketConnected.asStateFlow()

    // 计时器相关
    private val globalTimer = GlobalTimer.getInstance()

    // 暴露计时器状态
    val playedTime: StateFlow<String> = globalTimer.playedTimeString
    val remainingTime: StateFlow<String> = globalTimer.remainingTimeString
    val durationTime: StateFlow<String> = globalTimer.durationTimeString
    val playProgress: StateFlow<Float> = globalTimer.played.combine(globalTimer.duration) { played, duration ->
        if (duration > 0) played.toFloat() / duration.toFloat() else 0f
    }.stateIn(viewModelScope, SharingStarted.Eagerly, 0f)

    // === 数据更新方法 ===

    // 从 WebSocket 更新完整歌曲信息
    fun updateFromWebSocket(nowPlayingData: NowPlayingData) {
        // 更新基本信息（用于UI显示）
        _title.value = nowPlayingData.title ?: "Unknown Title"
        _artist.value = nowPlayingData.artist ?: "Unknown Artist"
        _album.value = nowPlayingData.album ?: "Unknown Album"
        _coverUrl.value = nowPlayingData.albumart

        // 更新详细信息
        _nowPlayingTitle.value = nowPlayingData.title ?: ""
        _nowPlayingArtist.value = nowPlayingData.artist ?: ""
        _nowPlayingAlbum.value = nowPlayingData.album ?: ""
        _nowPlayingYears.value = nowPlayingData.year ?: ""
        _nowPlayingCircle.value = nowPlayingData.circle ?: ""

        // 标记为已更新
        _isUpdatedInfo.value = true

        // 启动计时器
        if (nowPlayingData.duration >= 0) {
            globalTimer.startTimer(
                duration = nowPlayingData.duration,
                played = nowPlayingData.played,
                remaining = nowPlayingData.remaining
            )
        }

        // 发送媒体更新事件给 MediaSessionService
        viewModelScope.launch {
            _mediaUpdateEvent.emit(
                MediaUpdateData(
                    title = nowPlayingData.title ?: "Unknown Title",
                    artist = nowPlayingData.artist ?: "Unknown Artist",
                    album = nowPlayingData.album ?: "Unknown Album",
                    coverUrl = nowPlayingData.albumart,
                    years = nowPlayingData.year ?: "",
                    circle = nowPlayingData.circle ?: ""
                )
            )
        }
    }

    // 播放状态相关方法
    fun updatePlayerStatus(isPlaying: Boolean) {
        _playerStatus.value = isPlaying
    }

    fun updatePlayButtonStatus(isEnabled: Boolean) {
        _playBtnStatus.value = isEnabled
    }

    fun updateBufferingState(state: Int) {
        _bufferingState.value = state
    }

    // 网络状态更新
    fun updateNetworkStatus(isConnected: Boolean) {
        _networkStatus.value = isConnected
    }

    // WebSocket 连接状态
    fun updateWebSocketStatus(isConnected: Boolean) {
        _webSocketConnected.value = isConnected
    }

    // 可视化器状态
    fun updateVisualizerSettings(show: Boolean, usable: Boolean) {
        _showVisualizer.value = show
        _visualizerUsable.value = usable
    }

    // 重置信息更新标志
    fun resetUpdateFlag() {
        _isUpdatedInfo.value = false
    }

    // 停止计时器
    fun stopTimer() {
        globalTimer.stopTimer()
    }

    // 重置计时器
    fun resetTimer() {
        globalTimer.resetTimer()
    }

    override fun onCleared() {
        super.onCleared()
        globalTimer.destroy()
    }

    companion object {
        // 全局单例访问点
        @Volatile
        private var INSTANCE: SongViewModel? = null

        fun getInstance(): SongViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SongViewModel().also { INSTANCE = it }
            }
        }
    }
}

// 媒体更新数据类
data class MediaUpdateData(
    val title: String,
    val artist: String,
    val album: String,
    val coverUrl: String,
    val years: String = "",
    val circle: String = "",
    val timestamp: Long = System.currentTimeMillis()
)