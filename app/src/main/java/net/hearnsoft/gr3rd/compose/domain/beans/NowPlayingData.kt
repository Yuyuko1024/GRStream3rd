package net.hearnsoft.gr3rd.compose.domain.beans

import com.google.gson.annotations.SerializedName

data class NowPlayingData(
    @SerializedName("SERVERINFO")
    val serverInfo: ServerInfo,

    @SerializedName("SONGINFO")
    val songInfo: SongInfo,

    @SerializedName("SONGTIMES")
    val songTimes: SongTimes,

    @SerializedName("SONGDATA")
    val songData: SongData,

    @SerializedName("MISC")
    val misc: Misc
)

data class ServerInfo(
    @SerializedName("LASTUPDATE")
    val lastUpdate: Int,

    @SerializedName("SERVERS")
    val servers: Int,

    @SerializedName("STATUS")
    val status: String,

    @SerializedName("LISTENERS")
    val listeners: Int,

    @SerializedName("STREAMS")
    val streams: Streams,

    @SerializedName("MODE")
    val mode: String
)

data class SongInfo(
    @SerializedName("TITLE")
    val title: String,

    @SerializedName("ARTIST")
    val artist: String,

    @SerializedName("ALBUM")
    val album: String,

    @SerializedName("YEAR")
    val year: String,

    @SerializedName("CIRCLE")
    val circle: String
)

data class SongTimes(
    @SerializedName("DURATION")
    val duration: Int,

    @SerializedName("PLAYED")
    val played: Int,

    @SerializedName("REMAINING")
    val remaining: Int,

    @SerializedName("SONGSTART")
    val songStart: Int,

    @SerializedName("SONGEND")
    val songEnd: Int
)

data class SongData(
    @SerializedName("SONGID")
    val songID: Int,

    @SerializedName("ALBUMID")
    val albumID: Int,

    @SerializedName("RATING")
    val rating: String,

    @SerializedName("TIMESRATED")
    val timesRated: Int
)

data class Misc(
    @SerializedName("CIRCLELINK")
    val circleLink: String,

    @SerializedName("ALBUMART")
    val albumArt: String,

    @SerializedName("CIRCLEART")
    val circleArt: String,

    @SerializedName("OFFSET")
    val offset: String,

    @SerializedName("OFFSETTIME")
    val offsetTime: Int
)

data class Streams(
    @SerializedName("lv1")
    val lv1: StreamLevel,

    @SerializedName("lv2")
    val lv2: StreamLevel,

    @SerializedName("lv3")
    val lv3: StreamLevel,

    @SerializedName("lv4")
    val lv4: StreamLevel,

    @SerializedName("lv5")
    val lv5: StreamLevel
)

data class StreamLevel(
    @SerializedName("BITRATE")
    val bitRate: Int,

    @SerializedName("LISTENERS")
    val listeners: Int
)