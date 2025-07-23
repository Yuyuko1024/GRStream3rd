package net.hearnsoft.gr3rd.compose.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi

@Composable
@UnstableSaltUiApi
fun LibraryScreen(
    modifier: Modifier = Modifier,
) {
    TitleBar(
        onBack = {
        },
        showBackBtn = false,
        text = "媒体库"
    )
    Column(
        modifier = modifier
    ) {

    }
}

@Preview
@Composable
@UnstableSaltUiApi
fun LibraryScreenPreview() {
    LibraryScreen()
}