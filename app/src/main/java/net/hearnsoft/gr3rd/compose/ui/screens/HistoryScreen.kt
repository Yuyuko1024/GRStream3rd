package net.hearnsoft.gr3rd.compose.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.gr3rd.compose.ui.theme.GRStream3rdComposeTheme

@Composable
@UnstableSaltUiApi
fun HistoryScreen(
    modifier: Modifier = Modifier,
) {
    TitleBar(
        onBack = {
        },
        showBackBtn = false,
        text = "记录"
    )
    Column(
        modifier = modifier
    ) {

    }
}

@Preview
@UnstableSaltUiApi
@Composable
fun HistoryScreenPreview() {
    GRStream3rdComposeTheme {
        HistoryScreen()
    }
}