package app.tiebalite.feature.thread.common.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.thread.ThreadPostBody

@Composable
internal fun ThreadPostContentSection(
    body: ThreadPostBody,
    modifier: Modifier = Modifier,
) {
    val blocks =
        remember(body) {
            buildThreadPostContentBlocks(
                body = body,
            )
        }
    if (blocks.isEmpty()) {
        return
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        blocks.forEach { block ->
            when (block) {
                is ThreadPostContentBlock.Text -> {
                    ThreadPostRichText(
                        inline = block.inline,
                    )
                }

                is ThreadPostContentBlock.ImageGroup -> {
                    PostImageGrid(images = block.images)
                }

                is ThreadPostContentBlock.MediaHint -> {
                    Text(
                        text = block.text,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun buildThreadPostContentBlocks(
    body: ThreadPostBody,
): List<ThreadPostContentBlock> {
    val blocks = mutableListOf<ThreadPostContentBlock>()

    if (body.inline.isNotEmpty()) {
        blocks += ThreadPostContentBlock.Text(inline = body.inline)
    }

    val images =
        body.media
            .asSequence()
            .mapNotNull { part -> part as? ThreadPostBody.MediaPart.Image }
            .filter { image -> image.url.isNotBlank() }
            .distinctBy { image -> image.url }
            .toList()
    if (images.isNotEmpty()) {
        blocks += ThreadPostContentBlock.ImageGroup(images = images)
    }

    body.media.forEach { part ->
        when (part) {
            is ThreadPostBody.MediaPart.Image -> Unit
            is ThreadPostBody.MediaPart.Video -> {
                blocks += ThreadPostContentBlock.MediaHint(text = "视频")
            }

            is ThreadPostBody.MediaPart.Voice -> {
                blocks +=
                    ThreadPostContentBlock.MediaHint(
                        text = if (part.durationSeconds > 0) "语音 ${part.durationSeconds}s" else "语音",
                    )
            }
        }
    }

    return blocks
}

private sealed interface ThreadPostContentBlock {
    data class Text(
        val inline: List<ThreadPostBody.InlinePart>,
    ) : ThreadPostContentBlock

    data class ImageGroup(
        val images: List<ThreadPostBody.MediaPart.Image>,
    ) : ThreadPostContentBlock

    data class MediaHint(
        val text: String,
    ) : ThreadPostContentBlock
}
