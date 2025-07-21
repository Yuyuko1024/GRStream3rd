package net.hearnsoft.gr3rd.compose

import android.app.Application
import android.content.Intent
import net.hearnsoft.gr3rd.compose.service.WebSocketService
import net.hearnsoft.gr3rd.compose.utils.AudioSessionManager
import net.hearnsoft.gr3rd.compose.utils.Logger

class RadioApplication : Application() {
    private val webSocket : Intent by lazy {
        Intent(this, WebSocketService::class.java)
    }

    override fun onCreate() {
        super.onCreate()

        Logger.info(this, "Application started")
        AudioSessionManager.generateAudioSessionId(this)
        Logger.info(this, "Audio session ID generated: ${AudioSessionManager.getAudioSessionIdOrDefault()}")

        startService(webSocket)
    }

    override fun onTerminate() {
        super.onTerminate()
        stopService(webSocket)
    }
}