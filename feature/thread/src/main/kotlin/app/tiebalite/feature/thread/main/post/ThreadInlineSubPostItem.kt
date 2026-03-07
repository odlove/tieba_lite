package app.tiebalite.feature.thread.main.post

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tiebalite.core.model.thread.ThreadPostBody
import app.tiebalite.core.model.thread.ThreadSubPost
import app.tiebalite.feature.thread.common.post.ThreadPostRichText

@Composable
internal fun ThreadInlineSubPostItem(
    item: ThreadSubPost,
    threadAuthorId: Long?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isThreadAuthor = threadAuthorId != null && threadAuthorId > 0L && item.authorId == threadAuthorId
    val prefix =
        buildAnnotatedString {
            pushStyle(
                SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                ),
            )
            append(item.authorName ?: "贴吧用户")
            pop()
            if (isThreadAuthor) {
                append(" ")
                pushStyle(
                    SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                    ),
                )
                append("楼主")
                pop()
            }
            append(": ")
        }
    ThreadPostRichText(
        inline = item.body.inline,
        prefix = prefix,
        suffix = AnnotatedString(item.body.inlineTrailingText()),
        style =
            MaterialTheme.typography.bodySmall.copy(
                fontSize = 13.sp,
                lineHeight = 18.sp,
            ),
        color = MaterialTheme.colorScheme.onSurface,
        modifier =
            modifier
                .padding(vertical = 2.dp),
        maxLines = 4,
        overflow = TextOverflow.Ellipsis,
        onClick = onClick,
    )
}

private fun ThreadPostBody.inlineTrailingText(): String {
    val mediaText =
        media.joinToString(separator = " ") { part ->
            when (part) {
                is ThreadPostBody.MediaPart.Image -> "[图片]"
                is ThreadPostBody.MediaPart.Video -> "[视频]"
                is ThreadPostBody.MediaPart.Voice ->
                    if (part.durationSeconds > 0) {
                        "[语音 ${part.durationSeconds}s]"
                    } else {
                        "[语音]"
                    }
            }
        }

    return when {
        mediaText.isNotBlank() && hasVisibleInlineContent() -> " $mediaText"
        mediaText.isNotBlank() -> mediaText
        hasVisibleInlineContent().not() -> "回复内容"
        else -> ""
    }
}

private fun ThreadPostBody.hasVisibleInlineContent(): Boolean =
    inline.any { part ->
        when (part) {
            is ThreadPostBody.InlinePart.Text -> part.text.isNotBlank()
            is ThreadPostBody.InlinePart.Link -> part.text.isNotBlank() || part.url.isNotBlank()
            is ThreadPostBody.InlinePart.Mention -> part.text.isNotBlank()
            is ThreadPostBody.InlinePart.Emoticon -> true
            is ThreadPostBody.InlinePart.Unknown -> part.text.isNotBlank() || part.link.isNotBlank()
        }
    }
