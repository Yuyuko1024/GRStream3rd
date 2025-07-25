package net.hearnsoft.gr3rd.compose.infrastructure.adapter

import net.hearnsoft.gr3rd.compose.infrastructure.api.GRApiService
import net.hearnsoft.gr3rd.compose.utils.Constants
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object GRApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.GR_API_BASE)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: GRApiService = retrofit.create(GRApiService::class.java)
}