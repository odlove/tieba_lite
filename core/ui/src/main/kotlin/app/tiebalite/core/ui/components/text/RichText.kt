package app.tiebalite.core.ui.components.text

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import app.tiebalite.core.model.text.RichText as ModelRichText
import app.tiebalite.core.model.text.RichTextPart
import app.tiebalite.core.ui.emoticon.DefaultEmoticonResolver
import app.tiebalite.core.ui.emoticon.EmoticonAsset
import app.tiebalite.core.ui.emoticon.EmoticonResolver
import coil3.compose.AsyncImage

@Composable
fun RichText(
    richText: ModelRichText,
    modifier: Modifier = Modifier,
    prefix: AnnotatedString = AnnotatedString(""),
    suffix: AnnotatedString = AnnotatedString(""),
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    color: Color = MaterialTheme.colorScheme.onSurface,
    linkColor: Color = MaterialTheme.colorScheme.primary,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    urlAnnotationTag: String? = null,
) {
    val emoticonResolver = DefaultEmoticonResolver
    val content =
        remember(richText, prefix, suffix, linkColor, emoticonResolver, urlAnnotationTag) {
            buildRichInlineContent(
                parts = richText.parts,
                linkColor = linkColor,
                emoticonResolver = emoticonResolver,
                prefix = prefix,
                suffix = suffix,
                urlAnnotationTag = urlAnnotationTag,
            )
        }
    if (content.text.isEmpty()) {
        return
    }
    Text(
        text = content.text,
        style = style,
        color = color,
        inlineContent = content.inlineContent,
        modifier = modifier,
        maxLines = maxLines,
        overflow = overflow,
    )
}

fun buildRichInlineContent(
    parts: List<RichTextPart>,
    linkColor: Color,
    emoticonResolver: EmoticonResolver,
    prefix: AnnotatedString = AnnotatedString(""),
    suffix: AnnotatedString = AnnotatedString(""),
    urlAnnotationTag: String? = null,
): RichInlineContent {
    val inlineContent = mutableMapOf<String, InlineTextContent>()
    val text =
        buildAnnotatedString {
            append(prefix)
            parts.forEachIndexed { index, part ->
                when (part) {
                    is RichTextPart.Text -> append(part.text)
                    is RichTextPart.Link -> {
                        val displayText = part.text.ifBlank { part.url }
                        if (displayText.isNotEmpty()) {
                            append(LinkPrefix)
                            if (urlAnnotationTag != null) {
                                pushStringAnnotation(
                                    tag = urlAnnotationTag,
                                    annotation = part.url,
                                )
                            }
                            withStyle(SpanStyle(color = linkColor)) {
                                append(displayText)
                            }
                            if (urlAnnotationTag != null) {
                                pop()
                            }
                        }
                    }

                    is RichTextPart.Mention -> {
                        if (part.text.isNotEmpty()) {
                            withStyle(SpanStyle(color = linkColor)) {
                                append(part.text)
                            }
                        }
                    }

                    is RichTextPart.Emoticon -> {
                        val fallbackText = part.plainText
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
                                        RichTextEmoticonInline(
                                            asset = asset,
                                            contentDescription = part.name.takeIf { it.isNotBlank() },
                                        )
                                    }
                            }
                        }
                    }

                    is RichTextPart.Unknown -> append(part.plainText)
                }
            }
            append(suffix)
        }
    return RichInlineContent(
        text = text,
        inlineContent = inlineContent.toMap(),
    )
}

data class RichInlineContent(
    val text: AnnotatedString,
    val inlineContent: Map<String, InlineTextContent>,
)

@Composable
private fun RichTextEmoticonInline(
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

private const val LinkPrefix = "🔗"
private val EmoticonSize: TextUnit = 18.sp
