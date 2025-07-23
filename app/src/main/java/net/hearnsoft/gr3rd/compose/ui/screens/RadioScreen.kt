package net.hearnsoft.gr3rd.compose.ui.screens

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.ext.safeMainPadding
import net.hearnsoft.gr3rd.compose.R
import net.hearnsoft.gr3rd.compose.domain.viewmodel.SongViewModel
import net.hearnsoft.gr3rd.compose.ui.theme.GRStream3rdComposeTheme
import net.hearnsoft.gr3rd.compose.ui.theme.Theme

@UnstableSaltUiApi
@Composable
fun RadioScreen(
    modifier: Modifier = Modifier,
    songViewModel: SongViewModel,
    onPlayPauseClick: () -> Unit = {},
    onRateClick: () -> Unit = {},
    onMoreInfoClick: () -> Unit = {}
) {
    // 收集 ViewModel 状态
    val title by songViewModel.title.collectAsState()
    val artist by songViewModel.artist.collectAsState()
    val coverUrl by songViewModel.coverUrl.collectAsState()
    val isPlaying by songViewModel.playerStatus.collectAsState()
    val bufferingState by songViewModel.bufferingState.collectAsState()
    val playButtonEnabled by songViewModel.playBtnStatus.collectAsState()
    val networkConnected by songViewModel.networkStatus.collectAsState()
    val webSocketConnected by songViewModel.webSocketConnected.collectAsState()

    // 计时器状态
    val playedTime by songViewModel.playedTime.collectAsState()
    val durationTime by songViewModel.durationTime.collectAsState()
    val progress by songViewModel.playProgress.collectAsState()

    // 添加调试日志
    LaunchedEffect(coverUrl) {
        println("DEBUG: coverUrl = $coverUrl")
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
                    onClick = onRateClick,
                    enabled = playButtonEnabled,
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
                    enabled = playButtonEnabled,
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
                    Text(
                        text = when (bufferingState) {
                            0 -> stringResource(R.string.status_idle)
                            1 -> stringResource(R.string.status_buffering)
                            2 -> stringResource(R.string.status_ready)
                            else -> stringResource(R.string.status_idle)
                        },
                        color = SaltTheme.colors.subText
                    )

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
            onPlayPauseClick = {},
            onRateClick = {},
            onMoreInfoClick = {}
        )
    }
}
