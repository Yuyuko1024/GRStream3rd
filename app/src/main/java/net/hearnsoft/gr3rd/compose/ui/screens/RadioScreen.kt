package net.hearnsoft.gr3rd.compose.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainPadding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.hearnsoft.gr3rd.compose.R
import net.hearnsoft.gr3rd.compose.domain.viewmodel.SongViewModel
import net.hearnsoft.gr3rd.compose.infrastructure.repository.GRStationNowPlayingRepository
import net.hearnsoft.gr3rd.compose.ui.theme.GRStream3rdComposeTheme
import net.hearnsoft.gr3rd.compose.ui.theme.Theme
import net.hearnsoft.gr3rd.compose.ui.widgets.NowPlayingDialog
import net.hearnsoft.gr3rd.compose.utils.Logger

@UnstableSaltUiApi
@Composable
fun RadioScreen(
    modifier: Modifier = Modifier,
    context: Context,
    songViewModel: SongViewModel,
    onPlayPauseClick: () -> Unit = {}
) {
    // 收集 ViewModel 状态
    val title by songViewModel.title.collectAsState()
    val artist by songViewModel.artist.collectAsState()
    val coverUrl by songViewModel.coverUrl.collectAsState()
    val isPlaying by songViewModel.playerStatus.collectAsState()
    val bufferingState by songViewModel.bufferingState.collectAsState()
    val networkConnected by songViewModel.networkStatus.collectAsState()
    val webSocketConnected by songViewModel.webSocketConnected.collectAsState()

    // 计时器状态
    val playedTime by songViewModel.playedTime.collectAsState()
    val durationTime by songViewModel.durationTime.collectAsState()
    val progress by songViewModel.playProgress.collectAsState()

    // 缓冲动画状态
    var showBufferingAnimation by remember { mutableStateOf(false) }
    var lastBufferingState by remember { mutableIntStateOf(bufferingState) }
    var animationJob by remember { mutableStateOf<Job?>(null) }

    // 监听缓冲状态变化
    LaunchedEffect(bufferingState) {
        if (bufferingState != lastBufferingState) {
            // 取消之前的动画任务
            animationJob?.cancel()

            // 根据不同状态设置不同的显示时长
            val displayDuration = when (bufferingState) {
                0 -> 0L // 空闲状态不显示
                1 -> 5000L // 缓冲中显示5秒
                2 -> 2000L // 准备就绪显示2秒
                else -> 3000L // 默认3秒
            }

            if (displayDuration > 0) {
                showBufferingAnimation = true

                // 启动新的动画任务
                animationJob = launch {
                    delay(displayDuration)
                    showBufferingAnimation = false
                }
            } else {
                showBufferingAnimation = false
            }
        }
        lastBufferingState = bufferingState
    }

    // 缓冲状态组件销毁时取消动画任务
    DisposableEffect(Unit) {
        onDispose {
            animationJob?.cancel()
        }
    }

    // 创建当前播放信息的Repository实例
    val nowPlayingRepository = remember { GRStationNowPlayingRepository() }
    // 当前播放信息对话框状态
    var nowPlayingDataIsLoading by remember { mutableStateOf(false) }

    // 收集对话框状态
    val showNowPlayingDialog = songViewModel.showSongInfoDialog.collectAsState()
    val currentSongInfo by songViewModel.currentSongInfo.collectAsState()

    // 当前播放信息对话框事件
    val onMoreInfoClick : () -> Unit = {
        // 显示对话框
        songViewModel.showSongInfoDialog()
        nowPlayingDataIsLoading = true

        // 异步加载当前播放信息
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val result = nowPlayingRepository.fetchNowPlaying()

                result.fold(
                    onSuccess = {
                        songViewModel.updateSongInfo(it)
                        nowPlayingDataIsLoading = false
                    },
                    onFailure = {
                        Logger.err("MainView", "Failed to fetch now playing data", it)
                        nowPlayingDataIsLoading = false
                        songViewModel.hideSongInfo()
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                context,
                                "无法获取当前播放信息，请稍后再试",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                )
            } catch (e: Exception) {
                Logger.err("MainView", "Failed to load now playing data", e)
                nowPlayingDataIsLoading = false
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context,
                        "无法获取当前播放信息，请稍后再试",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                songViewModel.hideSongInfo()
            }
        }
    }

    if (showNowPlayingDialog.value) {
        val songData = currentSongInfo?.songInfo
        // 显示当前播放歌曲信息对话框
        NowPlayingDialog(
            onDismiss = {
                songViewModel.hideSongInfo()
                nowPlayingDataIsLoading = false
            },
            properties = DialogProperties(),
            nowPlayingSongInfo = songData,
            isLoading = nowPlayingDataIsLoading
        )
    }

    // 主界面布局
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = Theme.colors.background)
    ) {
        // 封面图片区域
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
        ) {
            // 封面图片
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(coverUrl)
                    .crossfade(true)
                    .crossfade(1000)
                    .build(),
                contentDescription = "Album cover",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
            )

            // 网络状态指示器
            if (!networkConnected || !webSocketConnected) {
                Card(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .safeMainPadding(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.Black.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = when {
                            !networkConnected -> stringResource(R.string.network_lost)
                            !webSocketConnected -> stringResource(R.string.network_connecting)
                            else -> ""
                        },
                        color = Color.White,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }
        }

        // 下半部分UI容器
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            // 歌曲信息栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 歌曲信息
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                ) {
                    // 歌曲标题
                    Text(
                        text = title,
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = SaltTheme.colors.text
                    )

                    // 艺术家
                    Text(
                        text = artist,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = SaltTheme.colors.subText
                    )
                }

                // 评分按钮
                IconButton(
                    onClick = {
                        //TODO("Need to implement")
                    },
                    modifier = Modifier.padding(end = 6.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_rate_star_24px),
                        contentDescription = "Rate Song",
                        tint = SaltTheme.colors.subText
                    )
                }

                // 更多信息按钮
                IconButton(
                    onClick = onMoreInfoClick,
                    modifier = Modifier.padding(start = 16.dp, end = 4.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_more_info_alt),
                        contentDescription = "More Info",
                        tint = SaltTheme.colors.subText
                    )
                }
            }

            // 进度和状态区域
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
            ) {
                // 进度条
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { progress },
                    color = SaltTheme.colors.highlight,
                    trackColor = SaltTheme.colors.stroke
                )

                // 时间和状态信息
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 已播放时间
                    Text(
                        text = playedTime,
                        modifier = Modifier.padding(8.dp),
                        color = SaltTheme.colors.subText
                    )

                    // 缓冲状态
                    AnimatedVisibility(
                        visible = showBufferingAnimation,
                        enter = fadeIn(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = EaseInOut
                            )
                        ) + expandVertically(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = EaseInOut
                            )
                        ),
                        exit = fadeOut(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = EaseInOut
                            )
                        ) + shrinkVertically(
                            animationSpec = tween(
                                durationMillis = 300,
                                easing = EaseInOut
                            )
                        )
                    ) {
                        Text(
                            text = when (bufferingState) {
                                0 -> stringResource(R.string.status_idle)
                                1 -> stringResource(R.string.status_buffering)
                                2 -> stringResource(R.string.status_ready)
                                else -> stringResource(R.string.status_idle)
                            },
                            color = SaltTheme.colors.subText
                        )
                    }

                    // 总时间
                    Text(
                        text = durationTime,
                        modifier = Modifier.padding(8.dp),
                        color = SaltTheme.colors.subText
                    )
                }
            }

            // 播放按钮
            ExtendedFloatingActionButton(
                onClick = onPlayPauseClick,
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.CenterHorizontally),
                containerColor = SaltTheme.colors.highlight,
                contentColor = Color.White
            ) {
                when {
                    bufferingState == 1 -> {
                        // 显示加载指示器
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    }
                    isPlaying -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_pause),
                            contentDescription = "正在播放"
                        )
                    }
                    else -> {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_play),
                            contentDescription = "已暂停"
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@UnstableSaltUiApi
@Composable
fun RadioScreenPreview() {
    GRStream3rdComposeTheme {
        // 创建一个模拟的 ViewModel 用于预览
        RadioScreen(
            songViewModel = SongViewModel.getInstance(),
            context = LocalContext.current,
            onPlayPauseClick = {}
        )
    }
}
