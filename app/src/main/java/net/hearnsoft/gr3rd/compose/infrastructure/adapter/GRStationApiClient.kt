package net.hearnsoft.gr3rd.compose.infrastructure.adapter

import net.hearnsoft.gr3rd.compose.infrastructure.api.GRStationApiService
import net.hearnsoft.gr3rd.compose.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GRStationApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.GR_STATION_API_BASE)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: GRStationApiService = retrofit.create(GRStationApiService::class.java)
}