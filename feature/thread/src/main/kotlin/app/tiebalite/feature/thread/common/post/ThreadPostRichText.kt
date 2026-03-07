package app.tiebalite.feature.thread.common.post

import androidx.compose.foundation.Image
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.tiebalite.core.model.thread.ThreadPostBody
import app.tiebalite.core.ui.emoticon.DefaultEmoticonResolver
import app.tiebalite.core.ui.emoticon.EmoticonAsset
import app.tiebalite.core.ui.emoticon.EmoticonResolver
import coil3.compose.AsyncImage

@Composable
internal fun ThreadPostRichText(
    inline: List<ThreadPostBody.InlinePart>,
    modifier: Modifier = Modifier,
    prefix: AnnotatedString = AnnotatedString(""),
    suffix: AnnotatedString = AnnotatedString(""),
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    onClick: (() -> Unit)? = null,
) {
    val linkColor = MaterialTheme.colorScheme.primary
    val emoticonResolver = DefaultEmoticonResolver
    val content =
        remember(inline, prefix, suffix, linkColor, emoticonResolver) {
            buildThreadInlineContent(
                inline = inline,
                linkColor = linkColor,
                emoticonResolver = emoticonResolver,
                prefix = prefix,
                suffix = suffix,
            )
        }
    if (content.text.isEmpty()) {
        return
    }
    ThreadInlineText(
        content = content,
        style = style,
        color = color,
        maxLines = maxLines,
        overflow = overflow,
        onClick = onClick,
        modifier = modifier,
    )
}

@Composable
private fun ThreadInlineText(
    content: ThreadInlineContent,
    style: TextStyle,
    color: Color,
    maxLines: Int,
    overflow: TextOverflow,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val text = content.text
    val uriHandler = LocalUriHandler.current
    var textLayoutResult by remember(text) { mutableStateOf<TextLayoutResult?>(null) }
    val hasUrlAnnotation = remember(text) { text.hasUrlAnnotation() }
    val clickableModifier =
        if (hasUrlAnnotation || onClick != null) {
            Modifier.pointerInput(text, uriHandler, onClick) {
                detectTapGestures { tapPosition ->
                    val url =
                        textLayoutResult
                            ?.getOffsetForPosition(tapPosition)
                            ?.let { offset -> text.findUrlAnnotation(offset) }
                    if (!url.isNullOrBlank()) {
                        runCatching { uriHandler.openUri(url) }
                    } else {
                        onClick?.invoke()
                    }
                }
            }
        } else {
            Modifier
        }
    Text(
        text = text,
        style = style,
        color = color,
        inlineContent = content.inlineContent,
        onTextLayout = { result ->
            textLayoutResult = result
        },
        modifier = modifier.then(clickableModifier),
        maxLines = maxLines,
        overflow = overflow,
    )
}

internal fun buildThreadInlineContent(
    inline: List<ThreadPostBody.InlinePart>,
    linkColor: Color,
    emoticonResolver: EmoticonResolver,
    prefix: AnnotatedString = AnnotatedString(""),
    suffix: AnnotatedString = AnnotatedString(""),
): ThreadInlineContent {
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    val text =
        buildAnnotatedString {
            append(prefix)
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
            append(suffix)
        }
    return ThreadInlineContent(
        text = text,
        inlineContent = inlineContent.toMap(),
    )
}

internal data class ThreadInlineContent(
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

private fun AnnotatedString.hasUrlAnnotation(): Boolean =
    getStringAnnotations(
        tag = UrlAnnotationTag,
        start = 0,
        end = length,
    ).isNotEmpty()

private fun AnnotatedString.findUrlAnnotation(offset: Int): String? =
    getStringAnnotations(
        tag = UrlAnnotationTag,
        start = offset,
        end = offset,
    ).firstOrNull()?.item

private const val UrlAnnotationTag = "url"
private const val LinkPrefix = "🔗"
private val EmoticonSize: TextUnit = 18.sp
