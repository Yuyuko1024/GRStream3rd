package net.hearnsoft.gr3rd.compose.domain.beans

import com.google.gson.annotations.SerializedName

data class LoginData(
    @SerializedName("RESULT")
    val result: String,
    @SerializedName("USERID")
    val userId: String,
    @SerializedName("USERNAME")
    val username: String,
    @SerializedName("APPSESSIONID")
    val appSessionId: String,
    @SerializedName("API")
    val api: String
)
