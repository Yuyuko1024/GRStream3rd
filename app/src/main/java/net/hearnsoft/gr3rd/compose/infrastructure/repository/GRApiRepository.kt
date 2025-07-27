package net.hearnsoft.gr3rd.compose.infrastructure.repository

import com.blankj.utilcode.util.SPStaticUtils
import net.hearnsoft.gr3rd.compose.domain.beans.LoginData
import net.hearnsoft.gr3rd.compose.infrastructure.adapter.GRApiClient
import net.hearnsoft.gr3rd.compose.utils.Constants
import net.hearnsoft.gr3rd.compose.utils.Logger

class GRApiRepository {
    private val apiService = GRApiClient.apiService

    suspend fun login(username: String, password: String) : Result<LoginData> {
        val TAG = "GRApiRepository.login"
        return try {
            val response = apiService.login(username, password)
            Logger.info(TAG, response.body().toString())
            if (response.isSuccessful && response.body() != null) {
                val responseBody = response.body()!!
                Logger.info(TAG, "Get login response body: $responseBody")

                val loginData = responseBody[0]

                if (loginData.result == "SUCCESS") {
                    saveLoginData(loginData)

                    val cookie = response.headers()["Set-Cookie"]
                    if (cookie != null) {
                        SPStaticUtils.put(Constants.PREF_COOKIE_KEY, cookie)
                    }

                    Result.success(loginData)
                } else {
                    Result.failure(Exception("LOGIN_ERROR_INVALID"))
                }
            } else {
                Result.failure(Exception("LOGIN_ERROR_SERVER"))
            }
        } catch (e: Exception) {
            Logger.err(TAG, "Error logging in", e)
            Result.failure(Exception("LOGIN_ERROR_NETWORK", e))
        }
    }

    private fun saveLoginData(loginData: LoginData) {
        SPStaticUtils.put(Constants.PREF_USERNAME_KEY, loginData.username)
        SPStaticUtils.put(Constants.PREF_USERID_KEY, loginData.userId)
        SPStaticUtils.put(Constants.PREF_APPSESSIONID_KEY, loginData.appSessionId)
        SPStaticUtils.put(Constants.PREF_API_KEY, loginData.api)
    }
}