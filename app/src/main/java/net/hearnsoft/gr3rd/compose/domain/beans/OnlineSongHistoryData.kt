package net.hearnsoft.gr3rd.compose.domain.beans

import com.google.gson.annotations.SerializedName

data class OnlineSongHistoryData(
    @SerializedName("PLAYED")
    val played: String,
    @SerializedName("TITLE")
    val title: String,
    @SerializedName("ARTIST")
    val artist: String,
    @SerializedName("ALBUMID")
    val albumId: String,
    @SerializedName("ALBUM")
    val album: String,
    @SerializedName("ALBUMART")
    val albumArt: String,
    @SerializedName("CIRCLE")
    val circle: String,
    @SerializedName("TRACK")
    val track: String
)