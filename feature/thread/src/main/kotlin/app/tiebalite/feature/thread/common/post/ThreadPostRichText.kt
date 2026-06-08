package app.tiebalite.feature.thread.common.post

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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import app.tiebalite.core.model.text.RichTextPart
import app.tiebalite.core.ui.components.text.RichInlineContent
import app.tiebalite.core.ui.components.text.buildRichInlineContent
import app.tiebalite.core.ui.emoticon.DefaultEmoticonResolver

@Composable
internal fun ThreadPostRichText(
    inline: List<RichTextPart>,
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
            buildRichInlineContent(
                parts = inline,
                linkColor = linkColor,
                emoticonResolver = emoticonResolver,
                prefix = prefix,
                suffix = suffix,
                urlAnnotationTag = UrlAnnotationTag,
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
    content: RichInlineContent,
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
