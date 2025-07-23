package net.hearnsoft.gr3rd.compose.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainPadding
import net.hearnsoft.gr3rd.compose.ui.theme.GRStream3rdComposeTheme

@Composable
@UnstableSaltUiApi
fun AccountScreen(
    modifier: Modifier = Modifier,
) {
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