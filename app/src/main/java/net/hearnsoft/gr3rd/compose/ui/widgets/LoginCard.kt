package net.hearnsoft.gr3rd.compose.ui.widgets

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ItemDivider
import com.moriafly.salt.ui.ItemEdit
import com.moriafly.salt.ui.ItemEditPassword
import com.moriafly.salt.ui.ItemInfo
import com.moriafly.salt.ui.ItemInfoType
import com.moriafly.salt.ui.ItemOuterLargeTitle
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi

@Composable
@UnstableSaltUiApi
fun LoginCard(
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    isResponse: Boolean = false,
    isLoginSuccess: Boolean = false,
    errorMessage: String = "",
    onLogin: (String, String) -> Unit = { _, _ -> }
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    RoundedColumn(
        modifier = modifier
            .padding(8.dp)
    ) {
        ItemOuterLargeTitle(
            text = "登录",
            sub = "登录以使用 Gensokyo Radio 的全部功能",
        )
        RoundedColumn {
            // 用户名
            ItemEdit(
                text = username,
                onChange = {
                    username = it
                },
                hint = "用户名",
                readOnly = isLoading
            )
            // 分割线
            ItemDivider()
            // 密码
            ItemEditPassword(
                text = password,
                onChange = {
                    password = it
                },
                hint = "密码",
                readOnly = isLoading
            )
        }
        if (isLoading) {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(start = 24.dp, end = 16.dp, top = 4.dp, bottom = 4.dp),
            ) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .size(16.dp)
                        .align(Alignment.CenterVertically),
                    color = SaltTheme.colors.subText,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "登录中...",
                    color = SaltTheme.colors.subText
                )
            }
        }
        if (isResponse) {
            RoundedColumn {
                ItemInfo(
                    text = if (isLoginSuccess) "登录成功" else "登录失败: $errorMessage",
                    infoType = if (isLoginSuccess) ItemInfoType.Success else ItemInfoType.Error,
                )
            }
        }
        Button(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            text = "登录",
            enabled = !isLoading && username.isNotBlank() && password.isNotBlank(),
            onClick = {
                if (username.isNotBlank() && password.isNotBlank()) {
                    onLogin(username, password)
                }
            }
        )
    }
}

@Composable
@Preview
@UnstableSaltUiApi
fun LoginCardPreview() {
    LoginCard()
}

@Composable
@Preview(name = "Loading State")
@UnstableSaltUiApi
fun LoginCardLoadingPreview() {
    LoginCard(isLoading = true)
}

@Composable
@Preview(name = "Login Success State")
@UnstableSaltUiApi
fun LoginCardLoginYesPreview() {
    LoginCard(isResponse = true, isLoginSuccess = true)
}

@Composable
@Preview(name = "Login Failure State")
@UnstableSaltUiApi
fun LoginCardLoginErrPreview() {
    LoginCard(isResponse = true, isLoginSuccess = false, errorMessage = "用户名或密码错误")
}