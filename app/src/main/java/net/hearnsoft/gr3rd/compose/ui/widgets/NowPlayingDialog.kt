package net.hearnsoft.gr3rd.compose.ui.widgets

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ItemOuterTip
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.SaltTheme
import com.moriafly.salt.ui.UnstableSaltUiApi
import com.moriafly.salt.ui.dialog.BasicDialog
import com.moriafly.salt.ui.dialog.DialogTitle
import com.moriafly.salt.ui.outerPadding
import net.hearnsoft.gr3rd.compose.domain.beans.SongInfo

@Composable
@UnstableSaltUiApi
fun NowPlayingDialog(
    onDismiss: () -> Unit,
    properties: DialogProperties = DialogProperties(),
    nowPlayingSongInfo: SongInfo?,
    isLoading: Boolean = false,
) {
    BasicDialog(
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        if (isLoading) {
            // 显示加载状态
            DialogTitle(text = "正在获取歌曲信息...")

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    color = SaltTheme.colors.highlight,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
            }
        } else if (nowPlayingSongInfo != null) {
            val title = remember { nowPlayingSongInfo.title }
            val artist = remember { nowPlayingSongInfo.artist }
            val album = remember { nowPlayingSongInfo.album }
            val year = remember { nowPlayingSongInfo.year }
            val circle = remember { nowPlayingSongInfo.circle }

            val dialogTitle = title

            DialogTitle(text = "当前正在播放: $dialogTitle")

            // 内容
            RoundedColumn {
                ItemValue(text = "标题", sub = title)
                ItemValue(text = "艺术家", sub = artist)
                ItemValue(text = "专辑", sub = album)
                ItemValue(text = "发行年份", sub = year)
                ItemValue(text = "社团", sub = circle)
            }

        } else {
            // 数据为空的错误状态
            DialogTitle(text = "获取信息失败")
            // 内容
            ItemOuterTip(text = "无法获取歌曲信息，请稍后重试")
        }

        Button(
            onClick = {
                onDismiss()
            },
            text = "确定",
            modifier = Modifier
                .fillMaxWidth()
                .outerPadding(),
            enabled = true
        )
    }
}

@Preview
@Composable
@UnstableSaltUiApi
fun NowPlayingDialogPreview() {
    NowPlayingDialog(
        onDismiss = { },
        nowPlayingSongInfo = SongInfo(
            title = "Sample Song",
            artist = "Sample Artist",
            album = "Sample Album",
            year = "2023",
            circle = "test"
        )
    )
}

@Preview
@Composable
@UnstableSaltUiApi
fun NowPlayingDialogErrorPreview() {
    NowPlayingDialog(
        onDismiss = { },
        nowPlayingSongInfo = null
    )
}

@Preview
@Composable
@UnstableSaltUiApi
fun NowPlayingDialogLoadingPreview() {
    NowPlayingDialog(
        onDismiss = { },
        nowPlayingSongInfo = null,
        isLoading = true
    )
}