package net.hearnsoft.gr3rd.compose.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import net.hearnsoft.gr3rd.compose.BuildConfig

class GlobalTimer private constructor() {
    companion object {
        const val TAG = "GlobalTimer"

        @Volatile
        private var INSTANCE: GlobalTimer? = null

        fun getInstance(): GlobalTimer {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: GlobalTimer().also { INSTANCE = it }
            }
        }
    }

    private val timerScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private var timerJob: Job? = null

    // 时间状态
    private val _duration = MutableStateFlow(0)
    val duration: StateFlow<Int> = _duration.asStateFlow()

    private val _played = MutableStateFlow(0)
    val played: StateFlow<Int> = _played.asStateFlow()

    private val _remaining = MutableStateFlow(0)
    val remaining: StateFlow<Int> = _remaining.asStateFlow()

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> = _isRunning.asStateFlow()

    // 格式化的时间字符串
    private val _playedTimeString = MutableStateFlow("00:00")
    val playedTimeString: StateFlow<String> = _playedTimeString.asStateFlow()

    private val _remainingTimeString = MutableStateFlow("00:00")
    val remainingTimeString: StateFlow<String> = _remainingTimeString.asStateFlow()

    private val _durationTimeString = MutableStateFlow("00:00")
    val durationTimeString: StateFlow<String> = _durationTimeString.asStateFlow()

    fun startTimer(duration: Int, played: Int, remaining: Int) {
        if (_isRunning.value) {
            stopTimer()
        }

        _duration.value = duration
        _played.value = played
        _remaining.value = remaining
        _isRunning.value = true

        // 更新格式化时间字符串
        updateTimeStrings()

        timerJob = timerScope.launch {
            var currentPlayed = played

            while (isActive && currentPlayed < duration) {
                delay(1000) // 每秒更新一次

                if (isActive) {
                    currentPlayed++
                    val currentRemaining = duration - currentPlayed

                    // 更新状态
                    _played.value = currentPlayed
                    _remaining.value = currentRemaining

                    // 更新格式化时间字符串
                    updateTimeStrings()

                    if (BuildConfig.DEBUG) {
                        Logger.debug(TAG, "GlobalTimer: onTimeUpdate: $currentPlayed $duration $currentRemaining")
                    }
                }
            }

            // 计时结束
            if (currentPlayed >= duration) {
                stopTimer()
            }
        }
    }

    fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
        _isRunning.value = false

        Logger.debug(TAG, "Timer stopped")
    }

    fun resetTimer() {
        stopTimer()
        _duration.value = 0
        _played.value = 0
        _remaining.value = 0
        updateTimeStrings()
    }

    private fun updateTimeStrings() {
        _playedTimeString.value = formatTime(_played.value)
        _remainingTimeString.value = formatTime(_remaining.value)
        _durationTimeString.value = formatTime(_duration.value)
    }

    private fun formatTime(seconds: Int): String {
        val minutes = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    // 获取进度百分比（0.0 - 1.0）
    fun getProgress(): Float {
        val durationValue = _duration.value
        return if (durationValue > 0) {
            _played.value.toFloat() / durationValue.toFloat()
        } else {
            0f
        }
    }

    fun destroy() {
        stopTimer()
        timerScope.cancel()
    }
}