package net.hearnsoft.gr3rd.compose.ui.view

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.TitleBar
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.YesDialog
import com.moriafly.salt.ui.ext.safeMainPadding
import net.hearnsoft.gr3rd.compose.MainBottomBar
import net.hearnsoft.gr3rd.compose.R
import net.hearnsoft.gr3rd.compose.domain.viewmodel.SongViewModel
import net.hearnsoft.gr3rd.compose.ui.screens.AccountScreen
import net.hearnsoft.gr3rd.compose.ui.screens.HistoryScreen
import net.hearnsoft.gr3rd.compose.ui.screens.HomeScreen
import net.hearnsoft.gr3rd.compose.ui.screens.LibraryScreen
import net.hearnsoft.gr3rd.compose.ui.screens.RadioScreen
import net.hearnsoft.gr3rd.compose.ui.screens.ScreenRoute
import net.hearnsoft.gr3rd.compose.ui.theme.GRStream3rdComposeTheme
import net.hearnsoft.gr3rd.compose.ui.theme.Theme

@UnstableSaltUiApi
@Composable
fun MainView(
    modifier: Modifier = Modifier,
    songViewModel: SongViewModel,
    onPlayPauseClick: () -> Unit = {},
    onRateClick: () -> Unit = {},
    onMoreInfoClick: () -> Unit = {}
) {
    val navController = rememberNavController()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // 收集对话框状态
    val showNowPlayingDialog = songViewModel.showSongInfoDialog.collectAsState()
    val currentSongInfo by songViewModel.currentSongInfo.collectAsState()

    // 根据当前路由设置标题
    val title = when (currentRoute) {
        ScreenRoute.Home.route -> "首页"
        ScreenRoute.Library.route -> "媒体库"
        ScreenRoute.History.route -> "历史记录"
        ScreenRoute.Account.route -> "个人"
        else -> stringResource(R.string.app_name)
    }

    if (showNowPlayingDialog.value && currentSongInfo != null) {
        val songData = currentSongInfo?.songInfo

        // 构建对话框内容
        val content = songData?.let {
            "标题：${it.title}\n艺术家：${it.artist}\n专辑：${it.album}\n" +
                    "发行年份：${it.year}\n社团：${it.circle}\n"
        } ?: "未知歌曲信息"


        RoundedColumn {
            YesDialog(
                onDismissRequest = { songViewModel.hideSongInfo() },
                properties = DialogProperties(
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                ),
                title = songData?.title ?: "未知歌曲",
                content = content,
                confirmText = "确定"
            )
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Theme.colors.background)
            .let {
                if (currentRoute != ScreenRoute.Radio.route) {
                    it.safeMainPadding() // 如果是 Radio 页面，则不显示标题栏
                } else {
                    it
                }
            }
    ) {
        if (currentRoute != ScreenRoute.Radio.route) {
            // 如果不是 Radio 页面，则显示标题栏
            TitleBar(
                onBack = {
                },
                showBackBtn = false,
                text = title
            )
        }
        MainNavHost(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize(),
            navController = navController,
            songViewModel = songViewModel,
            onPlayPauseClick = onPlayPauseClick,
            onRateClick = onRateClick,
            onMoreInfoClick = onMoreInfoClick
        )
        MainBottomBar(
            navController = navController,
            modifier = Modifier.safeMainPadding()
        )
    }
}

@Composable
@UnstableSaltUiApi
fun MainNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    songViewModel: SongViewModel,
    onPlayPauseClick: () -> Unit,
    onRateClick: () -> Unit,
    onMoreInfoClick: () -> Unit
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        // 默认route的页面
        startDestination = ScreenRoute.Radio.route,
        // 动画过渡效果配置
        enterTransition = {
            slideIntoContainer(
                animationSpec = tween(200),
                towards = SlideDirection.Start,
            ) + fadeIn(animationSpec = tween(200))
        },
        exitTransition = {
            slideOutOfContainer(
                animationSpec = tween(200),
                towards = SlideDirection.End,
            ) + fadeOut(animationSpec = tween(200))
        },
        popEnterTransition = {
            slideIntoContainer(
                animationSpec = tween(200),
                towards = SlideDirection.Start,
            ) + fadeIn(animationSpec = tween(200))
        },
        popExitTransition = {
            slideOutOfContainer(
                animationSpec = tween(200),
                towards = SlideDirection.End,
            ) + fadeOut(animationSpec = tween(200))
        }
    ) {
        composable(ScreenRoute.Home.route) {
            HomeScreen(modifier)
        }
        composable(ScreenRoute.Radio.route) {
            RadioScreen(
                modifier = Modifier
                    .fillMaxSize(),
                songViewModel = songViewModel,
                onPlayPauseClick = onPlayPauseClick,
                onRateClick = onRateClick,
                onMoreInfoClick = onMoreInfoClick
            )
        }
        composable(ScreenRoute.Library.route) {
            LibraryScreen(modifier)
        }
        composable(ScreenRoute.History.route) {
            HistoryScreen(modifier)
        }
        composable(ScreenRoute.Account.route) {
            AccountScreen(modifier)
        }
    }
}

@Preview(showBackground = true)
@UnstableSaltUiApi
@Composable
fun MainPreview() {
    GRStream3rdComposeTheme {
        // 创建一个模拟的 ViewModel 用于预览
        MainView(
            songViewModel = SongViewModel.getInstance(),
            onPlayPauseClick = {},
            onRateClick = {},
            onMoreInfoClick = {}
        )
    }
}
