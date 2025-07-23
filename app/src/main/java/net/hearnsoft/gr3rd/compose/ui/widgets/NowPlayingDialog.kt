package net.hearnsoft.gr3rd.compose.ui.widgets

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.window.DialogProperties
import com.moriafly.salt.ui.Button
import com.moriafly.salt.ui.ItemValue
import com.moriafly.salt.ui.RoundedColumn
import com.moriafly.salt.ui.TextButton
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
    nowPlayingSongInfo: SongInfo
) {
    val title = remember { nowPlayingSongInfo.title }
    val artist = remember { nowPlayingSongInfo.artist }
    val album = remember { nowPlayingSongInfo.album }
    val year = remember { nowPlayingSongInfo.year }
    val circle = remember { nowPlayingSongInfo.circle }

    val dialogTitle = title

    BasicDialog(
        onDismissRequest = onDismiss,
        properties = properties
    ) {
        DialogTitle(text = "当前正在播放: $dialogTitle")

        // 内容
        RoundedColumn {
            ItemValue(text = "标题", sub = title)
            ItemValue(text = "艺术家", sub = artist)
            ItemValue(text = "专辑", sub = album)
            ItemValue(text = "发行年份", sub = year)
            ItemValue(text = "社团", sub = circle)
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