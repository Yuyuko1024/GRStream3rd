package net.hearnsoft.gr3rd.compose.utils

import android.net.Uri
import androidx.core.net.toUri
import com.google.gson.JsonObject


object Constants {
    const val WS_URL = "wss://gensokyoradio.net/wss"
    val WS_SESSION_MSG : JsonObject = JsonObject().apply {
        addProperty("message", "grInitialConnection")
    }
    val WS_PING_MSG : JsonObject = JsonObject().apply {
        addProperty("message", "ping")
    }

    const val NOW_PLAYING_JSON: String = "https://gensokyoradio.net/api/station/playing/"
    const val SONG_HISTORY_JSON: String = "https://gensokyoradio.net/api/station/history/"

    const val COVER_URL: String = "https://gensokyoradio.net/images/albums/"
    const val DEFAULT_COVER_URL: String = "https://gensokyoradio.net/images/assets/no-albumart.png"

    const val GR_STREAM_URL_DEFAULT: String = "https://stream.gensokyoradio.net/1/"
    const val GR_STREAM_URL_MOBILE: String = "https://stream.gensokyoradio.net/2"
    const val GR_STREAM_URL_ENHANCED: String = "https://stream.gensokyoradio.net/3"

    const val GR_LOGIN_API_URL: String = "https://gensokyoradio.net/api/login/"
    const val GR_REGISTER_URL: String = "https://gensokyoradio.net/register"
    const val GR_FORGOT_PASSWORD_URL: String = "https://gensokyoradio.net/account/recover"
    const val GR_SONG_RATE_URL: String = "https://gensokyoradio.net/api/station/rating/"

    val GR_PWA_APP_URL: Uri = "https://app.gensokyoradio.net/".toUri()


    const val PREF_USERNAME_KEY: String = "username"
    const val PREF_USERID_KEY: String = "userid"
    const val PREF_APPSESSIONID_KEY: String = "appsessionid"
    const val PREF_API_KEY: String = "api"
    const val PREF_COOKIE_KEY: String = "cookie"
    const val PREF_COVER_QUALITY: String = "cover_quality"

    const val PREF_CLIENT_ID: String = "clientId"
    const val PREF_VISUALIZER: String = "visualizer"
    const val PREF_SERVER: String = "server"
    const val PREF_CUSTOM_SERVER: String = "custom_server"
    const val PREF_SHOWED_NOTICE_DIALOG: String = "showed_notice_dialog"
}