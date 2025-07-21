package net.hearnsoft.gr3rd.compose.domain.beans

import com.google.gson.annotations.SerializedName
import net.hearnsoft.gr3rd.compose.utils.Constants

data class NowPlayingData(
    val songid: Int,
    val title: String?,
    val artist: String?,
    val album: String?,
    val circle: String?,
    val duration: Int,
    @SerializedName("albumart")
    private val _albumart: String?,
    val year: String?,
    val played: Int,
    val remaining: Int,
    val albumid: Int
) {
    // 公开的 albumart 属性，自动处理空值
    val albumart: String
        get() = _albumart?.takeIf { it.isNotBlank() } ?: Constants.DEFAULT_COVER_URL
}