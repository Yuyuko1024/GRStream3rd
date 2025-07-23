package net.hearnsoft.gr3rd.compose

import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blankj.utilcode.util.SPStaticUtils
import com.hjq.permissions.OnPermissionCallback
import com.hjq.permissions.XXPermissions
import com.hjq.permissions.permission.PermissionLists
import com.hjq.permissions.permission.base.IPermission
import com.moriafly.salt.ui.BottomBar
import com.moriafly.salt.ui.BottomBarItem
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainPadding
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import net.hearnsoft.gr3rd.compose.domain.viewmodel.SongViewModel
import net.hearnsoft.gr3rd.compose.service.GRStreamPlaybackService
import net.hearnsoft.gr3rd.compose.ui.screens.ScreenRoute
import net.hearnsoft.gr3rd.compose.ui.theme.GRStream3rdComposeTheme
import net.hearnsoft.gr3rd.compose.ui.view.MainView
import net.hearnsoft.gr3rd.compose.utils.Constants
import net.hearnsoft.gr3rd.compose.utils.Logger

@UnstableSaltUiApi
@UnstableApi
class MainActivity : ComponentActivity() {
    companion object {
        const val TAG = "MainActivity"
        const val RC_PERM_DEFAULT = 1
    }

    // 使用共享的 ViewModel 实例
    private val songViewModel by lazy { SongViewModel.getInstance() }
    private var mediaController: MediaController? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        initPermissions()
        startPlaybackService()

        setContent {
            GRStream3rdComposeTheme {
                MainView(
                    songViewModel = songViewModel,
                    onPlayPauseClick = { handlePlayPause() },
                    onRateClick = { showRateDialog() },
                    onMoreInfoClick = { showSongInfoDialog() }
                )
            }
        }
    }

    private fun startPlaybackService() {
        lifecycleScope.launch {
            try {
                val sessionToken = SessionToken(
                    this@MainActivity,
                    ComponentName(this@MainActivity, GRStreamPlaybackService::class.java)
                )

                mediaController = MediaController.Builder(this@MainActivity, sessionToken)
                    .buildAsync().await()

                Logger.info(TAG, "Media controller initialized")
            } catch (e: Exception) {
                Logger.err(TAG, "Failed to initialize media controller", e)
            }
        }
    }

    private fun initPermissions() {
        // 初始化权限请求逻辑
        var permissions = mutableListOf(
            PermissionLists.getReadPhoneStatePermission()
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(
                PermissionLists.getPostNotificationsPermission()
            )
        }

        if (!XXPermissions.isGrantedPermissions(this, permissions)) {
            XXPermissions.with(this)
                .permissions(permissions)
                .request(object : OnPermissionCallback {
                    override fun onGranted(
                        permissions: List<IPermission?>,
                        allGranted: Boolean
                    ) {
                        Logger.debug(TAG, "Permissions granted: $permissions")
                    }

                    override fun onDenied(
                        permissions: List<IPermission?>,
                        never: Boolean
                    ) {
                        Logger.warn(TAG, "Permissions denied: $permissions")
                        if (never) {
                            // 如果用户选择了不再询问，提示用户手动开启权限
                            XXPermissions.startPermissionActivity(this@MainActivity, permissions)
                        }
                    }
                })
        }
    }

    private fun handlePlayPause() {
        mediaController?.let { controller ->
            if (controller.isPlaying) {
                controller.pause()
            } else {
                controller.play()
            }
        } ?: run {
            Logger.warn(TAG, "Media controller not available")
        }
    }

    private fun showRateDialog() {
        // TODO: 实现评分对话框
        Logger.info(TAG, "Show rate dialog requested")
    }

    private fun showSongInfoDialog() {
        // TODO: 实现歌曲信息对话框
        Logger.info(TAG, "Show song info dialog requested")
    }

    private fun showNoticeDialogIfNeeded() {
        if (!SPStaticUtils.getBoolean(Constants.PREF_SHOWED_NOTICE_DIALOG, false)) {
            // TODO: 实现首次启动通知对话框
            Logger.info(TAG, "Should show notice dialog")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaController?.release()
        Logger.info(TAG, "MainActivity destroyed")
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
