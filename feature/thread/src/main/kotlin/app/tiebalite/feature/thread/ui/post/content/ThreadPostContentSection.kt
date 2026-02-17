package app.tiebalite.feature.thread.ui.post.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.tiebalite.core.model.thread.ThreadPostBody
import app.tiebalite.core.ui.emoticon.DefaultEmoticonResolver
import app.tiebalite.core.ui.emoticon.EmoticonAsset
import app.tiebalite.core.ui.emoticon.EmoticonResolver
import coil3.compose.AsyncImage

@Composable
internal fun ThreadPostContentSection(
    body: ThreadPostBody,
    modifier: Modifier = Modifier,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val emoticonResolver = DefaultEmoticonResolver
    val blocks =
        remember(body, linkColor, emoticonResolver) {
            buildThreadPostContentBlocks(
                body = body,
                linkColor = linkColor,
                emoticonResolver = emoticonResolver,
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
                    ThreadInlineText(
                        content = block.content,
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
    linkColor: Color,
    emoticonResolver: EmoticonResolver,
): List<ThreadPostContentBlock> {
    val blocks = mutableListOf<ThreadPostContentBlock>()

    val inlineContent =
        buildInlineAnnotatedText(
            inline = body.inline,
            linkColor = linkColor,
            emoticonResolver = emoticonResolver,
        )
    if (inlineContent.text.isNotEmpty()) {
        blocks += ThreadPostContentBlock.Text(content = inlineContent)
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

@Composable
private fun ThreadInlineText(
    content: ThreadInlineContent,
    modifier: Modifier = Modifier,
) {
    val text = content.text
    val uriHandler = LocalUriHandler.current
    var textLayoutResult by remember(text) { mutableStateOf<TextLayoutResult?>(null) }
    val hasUrlAnnotation =
        remember(text) {
            text
                .getStringAnnotations(
                    tag = UrlAnnotationTag,
                    start = 0,
                    end = text.length,
                ).isNotEmpty()
        }
    val clickableModifier =
        if (hasUrlAnnotation) {
            Modifier.pointerInput(text, uriHandler) {
                detectTapGestures { tapPosition ->
                    val layout = textLayoutResult ?: return@detectTapGestures
                    val offset = layout.getOffsetForPosition(tapPosition)
                    val url =
                        text
                            .getStringAnnotations(
                                tag = UrlAnnotationTag,
                                start = offset,
                                end = offset,
                            ).firstOrNull()
                            ?.item
                    if (!url.isNullOrBlank()) {
                        runCatching { uriHandler.openUri(url) }
                    }
                }
            }
        } else {
            Modifier
        }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurface,
        inlineContent = content.inlineContent,
        onTextLayout = { result ->
            textLayoutResult = result
        },
        modifier = modifier.then(clickableModifier),
    )
}

private fun buildInlineAnnotatedText(
    inline: List<ThreadPostBody.InlinePart>,
    linkColor: Color,
    emoticonResolver: EmoticonResolver,
): ThreadInlineContent {
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    val text =
        buildAnnotatedString {
            inline.forEachIndexed { index, part ->
                when (part) {
                    is ThreadPostBody.InlinePart.Text -> append(part.text)
                    is ThreadPostBody.InlinePart.Link -> {
                        val displayText = part.text.ifBlank { part.url }
                        if (displayText.isNotEmpty()) {
                            append(LinkPrefix)
                            pushStringAnnotation(
                                tag = UrlAnnotationTag,
                                annotation = part.url,
                            )
                            withStyle(SpanStyle(color = linkColor)) {
                                append(displayText)
                            }
                            pop()
                        }
                    }

                    is ThreadPostBody.InlinePart.Mention -> {
                        if (part.text.isNotEmpty()) {
                            withStyle(SpanStyle(color = linkColor)) {
                                append(part.text)
                            }
                        }
                    }

                    is ThreadPostBody.InlinePart.Emoticon -> {
                        val fallbackText = part.fallbackText()
                        when (
                            val asset =
                                emoticonResolver.resolve(
                                    id = part.id,
                                    name = part.name,
                                )
                        ) {
                            is EmoticonAsset.FallbackText -> append(asset.text)
                            is EmoticonAsset.LocalRes, is EmoticonAsset.Remote -> {
                                val key = "emoticon:$index:${part.id.orEmpty()}:${part.name}"
                                appendInlineContent(
                                    id = key,
                                    alternateText = fallbackText,
                                )
                                inlineContent[key] =
                                    InlineTextContent(
                                        placeholder =
                                            Placeholder(
                                                width = EmoticonSize,
                                                height = EmoticonSize,
                                                placeholderVerticalAlign = PlaceholderVerticalAlign.TextCenter,
                                            ),
                                    ) {
                                        ThreadEmoticonInline(
                                            asset = asset,
                                            contentDescription = part.name.takeIf { it.isNotBlank() },
                                        )
                                    }
                            }
                        }
                    }

                    is ThreadPostBody.InlinePart.Unknown -> append(part.text.ifEmpty { part.link })
                }
            }
        }
    return ThreadInlineContent(
        text = text,
        inlineContent = inlineContent.toMap(),
    )
}

private data class ThreadInlineContent(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>,
)

@Composable
private fun ThreadEmoticonInline(
    asset: EmoticonAsset,
    contentDescription: String?,
) {
    when (asset) {
        is EmoticonAsset.LocalRes -> {
            Image(
                painter = painterResource(id = asset.resId),
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }

        is EmoticonAsset.Remote -> {
            AsyncImage(
                model = asset.url,
                contentDescription = contentDescription,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit,
            )
        }

        is EmoticonAsset.FallbackText -> {
            Text(
                text = asset.text,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun ThreadPostBody.InlinePart.Emoticon.fallbackText(): String = "#(${name.ifBlank { "表情" }})"

private sealed interface ThreadPostContentBlock {
    data class Text(
        val content: ThreadInlineContent,
    ) : ThreadPostContentBlock

    data class ImageGroup(
        val images: List<ThreadPostBody.MediaPart.Image>,
    ) : ThreadPostContentBlock

    data class MediaHint(
        val text: String,
    ) : ThreadPostContentBlock
}

private const val UrlAnnotationTag = "url"
private const val LinkPrefix = "🔗"
private val EmoticonSize = 18.sp
