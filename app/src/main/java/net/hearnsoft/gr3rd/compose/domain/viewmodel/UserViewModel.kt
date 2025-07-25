package net.hearnsoft.gr3rd.compose.domain.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blankj.utilcode.util.SPStaticUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.hearnsoft.gr3rd.compose.domain.beans.LoginData
import net.hearnsoft.gr3rd.compose.infrastructure.repository.GRApiRepository
import net.hearnsoft.gr3rd.compose.utils.Constants
import net.hearnsoft.gr3rd.compose.utils.Logger

class UserViewModel : ViewModel() {
    private val repository = GRApiRepository()

    // 用户信息状态
    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _userId = MutableStateFlow("")
    val userId: StateFlow<String> = _userId.asStateFlow()

    private val _isLoggedIn = MutableStateFlow(false)
    val isLoggedIn: StateFlow<Boolean> = _isLoggedIn.asStateFlow()

    // 登录状态
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isResponse = MutableStateFlow(false)
    val isResponse: StateFlow<Boolean> = _isResponse.asStateFlow()

    private val _isLoginSuccess = MutableStateFlow(false)
    val isLoginSuccess: StateFlow<Boolean> = _isLoginSuccess.asStateFlow()

    // 错误信息
    private val _errorMessage = MutableStateFlow<String>("")
    val errorMessage: StateFlow<String> = _errorMessage.asStateFlow()

    init {
        // 初始化时检查本地存储的用户信息
        checkLoginStatus()
    }

    // 检查本地登录状态
    private fun checkLoginStatus() {
        val savedUsername = SPStaticUtils.getString(Constants.PREF_USERNAME_KEY, "")
        val savedUserId = SPStaticUtils.getString(Constants.PREF_USERID_KEY, "")

        if (savedUsername.isNotEmpty() && savedUserId.isNotEmpty()) {
            _username.value = savedUsername
            _userId.value = savedUserId
            _isLoggedIn.value = true
            Logger.info("UserViewModel", "Found saved login: $savedUsername")
        } else {
            _isLoggedIn.value = false
            Logger.info("UserViewModel", "No saved login found")
        }
    }

    // 登录方法
    fun login(username: String, password: String) {
        viewModelScope.launch {
            try {
                // 重置状态
                _isLoading.value = true
                _isResponse.value = false
                _isLoginSuccess.value = false
                _errorMessage.value = ""

                Logger.info("UserViewModel", "Attempting login for: $username")

                val result = repository.login(username, password)

                // 停止加载，显示响应
                _isLoading.value = false
                _isResponse.value = true

                result.fold(
                    onSuccess = { loginData ->
                        // 登录成功
                        _isLoginSuccess.value = true
                        updateUserInfo(loginData)
                        Logger.info("UserViewModel", "Login successful: ${loginData.USERNAME}")

                        // 2秒后切换到 UserCard
                        delay(2000)
                        _isLoggedIn.value = true
                        resetLoginState()
                    },
                    onFailure = { error ->
                        // 登录失败
                        _isLoginSuccess.value = false
                        val errorMessage = when (error.message) {
                            "LOGIN_ERROR_INVALID" -> "用户名或密码错误"
                            "LOGIN_ERROR_SERVER" -> "服务器错误"
                            "LOGIN_ERROR_NETWORK" -> "网络连接失败"
                            else -> "登录失败"
                        }
                        _errorMessage.value = errorMessage
                        Logger.err("UserViewModel", "Login failed: $errorMessage", error)

                        // 3秒后重置登录状态，允许重新尝试
                        delay(3000)
                        resetLoginState()
                    }
                )
            } catch (e: Exception) {
                _isLoading.value = false
                _isResponse.value = true
                _isLoginSuccess.value = false
                _errorMessage.value = "登录出错"
                Logger.err("UserViewModel", "Login exception", e)

                delay(3000)
                resetLoginState()
            }
        }
    }

    // 更新用户信息
    private fun updateUserInfo(loginData: LoginData) {
        _username.value = loginData.USERNAME
        _userId.value = loginData.USERID
    }

    // 重置登录状态
    private fun resetLoginState() {
        _isResponse.value = false
        _isLoginSuccess.value = false
        _errorMessage.value = ""
    }

    // 登出方法
    fun logout() {
        viewModelScope.launch {
            // 清除本地存储
            SPStaticUtils.remove(Constants.PREF_USERNAME_KEY)
            SPStaticUtils.remove(Constants.PREF_USERID_KEY)
            SPStaticUtils.remove(Constants.PREF_APPSESSIONID_KEY)
            SPStaticUtils.remove(Constants.PREF_API_KEY)
            SPStaticUtils.remove(Constants.PREF_COOKIE_KEY)

            // 重置状态
            _username.value = ""
            _userId.value = ""
            _isLoggedIn.value = false
            resetLoginState()

            Logger.info("UserViewModel", "User logged out")
        }
    }
}