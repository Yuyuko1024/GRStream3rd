package net.hearnsoft.gr3rd.compose.infrastructure.api

import net.hearnsoft.gr3rd.compose.domain.beans.NowPlayingData
import net.hearnsoft.gr3rd.compose.domain.beans.OnlineSongHistoryData
import net.hearnsoft.gr3rd.compose.utils.Constants
import retrofit2.Response
import retrofit2.http.GET

interface GRStationApiService {
    @GET(Constants.STATION_NOW_PLAYING_API)
    suspend fun getNowPlaying(): Response<NowPlayingData>

    @GET(Constants.STATION_SONG_HISTORY_API)
    suspend fun getSongHistory(): Response<List<OnlineSongHistoryData>>
}