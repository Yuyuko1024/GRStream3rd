package net.hearnsoft.gr3rd.compose.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainPadding
import net.hearnsoft.gr3rd.compose.ui.screens.history.LocalHistoryScreen
import net.hearnsoft.gr3rd.compose.ui.screens.history.OnlineHistoryScreen
import net.hearnsoft.gr3rd.compose.ui.theme.GRStream3rdComposeTheme
import net.hearnsoft.gr3rd.compose.ui.theme.Primary
import net.hearnsoft.gr3rd.compose.ui.theme.Theme

@Composable
@UnstableSaltUiApi
fun HistoryScreen(
    modifier: Modifier = Modifier,
) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf("在线记录", "本地记录")

    Column(
        modifier = modifier
            .safeMainPadding()
            .fillMaxSize()
    ) {
        TitleBar(
            onBack = {
            },
            showBackBtn = false,
            text = "记录"
        )
        Column(
            modifier = modifier
                .fillMaxSize()
        ) {
            TabRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp), // 推荐高度
                selectedTabIndex = selectedTabIndex,
                containerColor = SaltTheme.colors.background,
                indicator = { tabPositions ->
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        height = 2.dp,
                        color = Primary
                    )
                },
                divider = {},
            ) {
                tabs.forEachIndexed { index, tabName ->
                    Tab(
                        modifier = Modifier.fillMaxWidth(),
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = tabName,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        selectedContentColor = SaltTheme.colors.highlight,
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> OnlineHistoryScreen(
                    modifier = modifier.weight(1f).fillMaxSize()
                )
                1 -> LocalHistoryScreen(
                    modifier = modifier.weight(1f).fillMaxSize()
                )
            }
        }
    }
}

@Preview
@UnstableSaltUiApi
@Composable
fun HistoryScreenPreview() {
    GRStream3rdComposeTheme {
        HistoryScreen(
            modifier =
                Modifier.background(Theme.colors.background)
        )
    }
}