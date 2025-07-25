package net.hearnsoft.gr3rd.compose.infrastructure.api

import net.hearnsoft.gr3rd.compose.domain.beans.LoginData
import net.hearnsoft.gr3rd.compose.utils.Constants
import retrofit2.Response
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface GRApiService {
    @FormUrlEncoded
    @POST(Constants.GR_LOGIN_API)
    suspend fun login(
        @Field("user") username: String,
        @Field("pass") password: String
    ): Response<List<LoginData>>
}