package app.tiebalite.core.ui.components.text

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.sp
import app.tiebalite.core.model.text.RichTextPart
import app.tiebalite.core.ui.emoticon.EmoticonAsset
import app.tiebalite.core.ui.emoticon.EmoticonResolver
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class RichTextTest {
    @Test
    fun replyKeepsBothNamesColoredAndBadgeAlongsideEmoticons() {
        val badgeId = "thread:original-poster"
        val badge = InlineTextContent(Placeholder(32.sp, 14.sp, PlaceholderVerticalAlign.TextCenter)) {}
        val content = buildRichInlineContent(
            parts = listOf(
                RichTextPart.Text("reply "),
                RichTextPart.Mention("target", uid = 42L),
                RichTextPart.Text(": body"),
                RichTextPart.Emoticon(id = "image_emoticon9", name = "cry"),
                RichTextPart.Link("link", "https://example.com"),
            ),
            linkColor = Color.Blue,
            emoticonResolver = EmoticonResolver { _, _ -> EmoticonAsset.Remote("https://example.com/emoticon.png") },
            prefix = buildAnnotatedString {
                withStyle(SpanStyle(color = Color.Blue)) { append("author") }
                append(" ")
                appendInlineContent(badgeId, "OP")
                append(": ")
            },
            urlAnnotationTag = "url",
            additionalInlineContent = mapOf(badgeId to badge),
        )

        for (name in listOf("author", "target")) {
            val start = content.text.text.indexOf(name)
            assertTrue(content.text.spanStyles.any {
                it.start == start && it.end == start + name.length && it.item.color == Color.Blue
            })
        }
        assertSame(badge, content.inlineContent[badgeId])
        assertEquals(2, content.inlineContent.size)
        assertTrue(content.text.getStringAnnotations(0, content.text.length).any { it.item == badgeId })
        assertEquals(
            "https://example.com",
            content.text.getStringAnnotations("url", 0, content.text.length).single().item,
        )
    }
}
