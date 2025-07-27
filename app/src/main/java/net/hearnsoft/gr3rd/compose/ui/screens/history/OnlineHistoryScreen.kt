package net.hearnsoft.gr3rd.compose.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.blankj.utilcode.util.SPStaticUtils
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.Text
import com.moriafly.salt.ui.UnstableSaltUiApi
import net.hearnsoft.gr3rd.compose.R
import net.hearnsoft.gr3rd.compose.domain.beans.OnlineSongHistoryData
import net.hearnsoft.gr3rd.compose.ui.theme.Theme
import net.hearnsoft.gr3rd.compose.ui.viewmodel.SongHistoryViewModel
import net.hearnsoft.gr3rd.compose.utils.Constants

@Composable
@UnstableSaltUiApi
fun OnlineHistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: SongHistoryViewModel = viewModel()
) {

    val songList by viewModel.songList.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    // 图片质量
    val coverQuality = if (SPStaticUtils.getInt(Constants.PREF_COVER_QUALITY, 0) == 0) "500" else "200"

    val isActive = rememberUpdatedState(true) // 用于触发加载

    // 每次进入该Tab都加载
    LaunchedEffect(isActive.value) {
        viewModel.loadOnlineHistory()
    }

    Box(modifier = modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            songList.isEmpty() -> {
                Text(
                    text = "暂无在线历史记录",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(songList.size) { index ->
                        OnlineSongHistoryItem(
                            modifier = Modifier
                                .fillMaxWidth(),
                            songHistoryData = songList[index],
                            coverQuality = coverQuality
                        )
                    }
                }
            }
        }
    }
}

@Composable
@UnstableSaltUiApi
fun OnlineSongHistoryItem(
    modifier: Modifier = Modifier,
    songHistoryData: OnlineSongHistoryData,
    coverQuality: String = "500"
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable {}
            .fillMaxWidth()
            .height(80.dp)
            .padding(8.dp)
    ) {
        val albumArtUrl =
            if (songHistoryData.albumArt.isEmpty()) {
                Constants.DEFAULT_COVER_URL
            } else {
                Constants.COVER_URL +coverQuality + "/" + songHistoryData.albumArt
            }

        val playedTimeText: @Composable (Int) -> String = { timeSec ->
            when {
                timeSec < 60 -> stringResource(R.string.online_history_time_less_min, timeSec.toString())
                timeSec < 120 -> stringResource(R.string.online_history_time_one_min)
                else -> stringResource(R.string.online_history_time_mins, (timeSec / 60).toString())
            }
        }

        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(albumArtUrl)
                .crossfade(true)
                .crossfade(1000)
                .build(),
            contentDescription = "Album cover",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .size(64.dp, 64.dp)
                .background(Theme.colors.background)
                .padding(4.dp)
                .align(Alignment.CenterVertically),
        )

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
                .align(Alignment.CenterVertically)
        ) {
            Text(
                text = songHistoryData.title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = SaltTheme.textStyles.main
            )
            Text(
                text = "${songHistoryData.artist} - ${songHistoryData.album}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = SaltTheme.textStyles.sub
            )
            Text(
                text = "Track ${songHistoryData.track} - ${playedTimeText(songHistoryData.played.toInt())}",
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = SaltTheme.textStyles.sub
            )
        }

    }
}

@Preview
@Composable
@UnstableSaltUiApi
fun OnlineHistoryScreenPreview() {
    OnlineHistoryScreen()
}

@Preview
@Composable
@UnstableSaltUiApi
fun OnlineSongHistoryItemPreview() {
    OnlineSongHistoryItem(
        modifier = Modifier.background(Theme.colors.background),
        songHistoryData = OnlineSongHistoryData(
            played =  "236",
            title =  "わたしはこねこ",
            artist =   "yana",
            albumId =  "10974",
            album =  "キセキ☆インパルス Emotional Feedback",
            albumArt =  "STAL-0902_642c951742.jpg",
            circle =  "Shibayan Records",
            track =  "5"
        )
    )
}