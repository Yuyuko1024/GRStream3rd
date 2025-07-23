package net.hearnsoft.gr3rd.compose.infrastructure.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.hearnsoft.gr3rd.compose.domain.beans.NowPlayingData
import net.hearnsoft.gr3rd.compose.infrastructure.adapter.GRStationApiClient
import net.hearnsoft.gr3rd.compose.utils.Logger

class GRStationNowPlayingRepository {
    private val apiService = GRStationApiClient.apiService

    suspend fun fetchNowPlaying(): Result<NowPlayingData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getNowPlaying()
            if (response.isSuccessful && response.body() != null) {
                Result.success(response.body()!!)
            } else {
                Result.failure(Exception("API call failed: ${response.code()}"))
            }
        } catch (e: Exception) {
            Logger.err("SongRepository", "Fetch data error", e)
            Result.failure(e)
        }
    }
}