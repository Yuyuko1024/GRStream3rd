package net.hearnsoft.gr3rd.compose.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.YesNoDialog
import com.moriafly.salt.ui.ext.safeMainPadding
import net.hearnsoft.gr3rd.compose.domain.viewmodel.UserViewModel
import net.hearnsoft.gr3rd.compose.ui.theme.GRStream3rdComposeTheme
import net.hearnsoft.gr3rd.compose.ui.widgets.LoginCard
import net.hearnsoft.gr3rd.compose.ui.widgets.UserCard

@Composable
@UnstableSaltUiApi
fun AccountScreen(
    modifier: Modifier = Modifier,
    userViewModel: UserViewModel = viewModel()
) {

    val context = LocalContext.current

    // 收集 ViewModel 状态
    val isLoggedIn by userViewModel.isLoggedIn.collectAsState()
    val username by userViewModel.username.collectAsState()
    val userId by userViewModel.userId.collectAsState()
    val isLoading by userViewModel.isLoading.collectAsState()
    val isResponse by userViewModel.isResponse.collectAsState()
    val isLoginSuccess by userViewModel.isLoginSuccess.collectAsState()
    val errorMessage by userViewModel.errorMessage.collectAsState()

    var toggleLogoutConfirm by remember { mutableStateOf(false) }

    if (toggleLogoutConfirm) {
        YesNoDialog(
            title = "确认登出",
            content = "确定要登出当前账户吗？",
            confirmText = "确定",
            cancelText = "取消",
            onConfirm = {
                userViewModel.logout()
                toggleLogoutConfirm = false
            },
            onDismissRequest = {
                toggleLogoutConfirm = false
            }
        )
    }

    Column(
        modifier = modifier
            .safeMainPadding()
    ) {
        TitleBar(
            onBack = {
            },
            showBackBtn = false,
            text = "账户"
        )
        Column(
            modifier = modifier
        ) {
            AnimatedVisibility(
                visible = !isLoggedIn,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                LoginCard(
                    isLoading = isLoading,
                    isResponse = isResponse,
                    isLoginSuccess = isLoginSuccess,
                    errorMessage = errorMessage,
                    onLogin = { username, password ->
                        userViewModel.login(username, password)
                    }
                )
            }

            AnimatedVisibility(
                visible = isLoggedIn,
                enter = fadeIn(animationSpec = tween(500)),
                exit = fadeOut(animationSpec = tween(500))
            ) {
                Column {
                    UserCard(
                        username = username,
                        userId = userId,
                        onLogout = {
                            toggleLogoutConfirm = true
                        }
                    )
                }
            }
        }
    }
}

@Preview
@UnstableSaltUiApi
@Composable
fun AccountScreenPreview() {
    GRStream3rdComposeTheme {
        AccountScreen()
    }
}