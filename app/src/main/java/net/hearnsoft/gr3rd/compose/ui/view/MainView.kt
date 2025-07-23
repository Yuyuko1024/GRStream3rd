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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.moriafly.salt.ui.BottomBar
import com.moriafly.salt.ui.BottomBarItem
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.YesDialog
import com.moriafly.salt.ui.ext.safeMainPadding
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
import net.hearnsoft.gr3rd.compose.ui.widgets.NowPlayingDialog

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

    // 收集对话框状态
    val showNowPlayingDialog = songViewModel.showSongInfoDialog.collectAsState()
    val currentSongInfo by songViewModel.currentSongInfo.collectAsState()

    if (showNowPlayingDialog.value && currentSongInfo != null) {
        val songData = currentSongInfo!!.songInfo
        // 显示当前播放歌曲信息对话框
        NowPlayingDialog(
            onDismiss = { songViewModel.hideSongInfo() },
            properties = DialogProperties(),
            nowPlayingSongInfo = songData,
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Theme.colors.background)
    ) {
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

@Composable
@UnstableSaltUiApi
fun MainBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    BottomBar(
        backgroundColor = SaltTheme.colors.background,
        modifier = modifier
    ) {
        BottomBarItem(
            text = "首页",
            onClick = {
                if (currentRoute != ScreenRoute.Home.route) {
                    navController.navigate(ScreenRoute.Home.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            state = currentRoute == ScreenRoute.Home.route,
            painter = painterResource(id = R.drawable.ic_home_24px),
        )
        BottomBarItem(
            text = "媒体库",
            onClick = {
                if (currentRoute != ScreenRoute.Library.route) {
                    navController.navigate(ScreenRoute.Library.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            state = currentRoute == ScreenRoute.Library.route,
            painter = painterResource(id = R.drawable.ic_art_track_24px),
        )
        BottomBarItem(
            text = "电台",
            onClick = {
                if (currentRoute != ScreenRoute.Radio.route) {
                    navController.navigate(ScreenRoute.Radio.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            state = currentRoute == ScreenRoute.Radio.route,
            painter = painterResource(id = R.drawable.ic_podcasts_24px),
        )
        BottomBarItem(
            text = "记录",
            onClick = {
                if (currentRoute != ScreenRoute.History.route) {
                    navController.navigate(ScreenRoute.History.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            state = currentRoute == ScreenRoute.History.route,
            painter = painterResource(id = R.drawable.ic_music_history_24px),
        )
        BottomBarItem(
            text = "账户",
            onClick = {
                if (currentRoute != ScreenRoute.Account.route) {
                    navController.navigate(ScreenRoute.Account.route) {
                        popUpTo(navController.graph.startDestinationId)
                        launchSingleTop = true
                    }
                }
            },
            state = currentRoute == ScreenRoute.Account.route,
            painter = painterResource(id = R.drawable.ic_account_circle_24px),
        )
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
