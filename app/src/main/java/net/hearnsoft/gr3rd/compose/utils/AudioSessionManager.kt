package net.hearnsoft.gr3rd.compose.utils

import android.content.Context
import android.media.AudioManager

object AudioSessionManager {
    private var audioSessionId: Int? = null

    fun generateAudioSessionId(context: Context): Int {
        if (audioSessionId == null) {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
            audioSessionId = audioManager.generateAudioSessionId()
        }
        return audioSessionId!!
    }

    fun getAudioSessionId(): Int? = audioSessionId

    fun getAudioSessionIdOrDefault(): Int = audioSessionId ?: AudioManager.AUDIO_SESSION_ID_GENERATE

    fun reset() {
        audioSessionId = null
    }
}