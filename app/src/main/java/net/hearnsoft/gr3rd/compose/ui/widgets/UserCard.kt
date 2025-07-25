package net.hearnsoft.gr3rd.compose.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.innerPadding

@Composable
@UnstableSaltUiApi
fun UserCard(
    modifier: Modifier = Modifier,
    username: String = "",
    userId: String = "",
    onLogout: () -> Unit = {}
) {
    RoundedColumn {
        ItemValue(
            text = "用户名",
            sub = username.ifEmpty { "未知用户" },
        )
        ItemValue(
            text = "用户 ID",
            sub = userId.ifEmpty { "未知ID" },
        )
        Button(
            modifier = modifier.fillMaxWidth()
                .semantics(true) { }
                .innerPadding(),
            text = "登出",
            onClick = onLogout
        )
    }

}

@Preview
@Composable
@UnstableSaltUiApi
fun UserCardPreview() {
    UserCard(
        username = "TestUser",
        userId = "123456",
    )
}