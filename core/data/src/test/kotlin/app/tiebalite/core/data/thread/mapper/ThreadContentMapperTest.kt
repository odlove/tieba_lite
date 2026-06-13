package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.text.RichTextPart
import app.tiebalite.core.model.thread.ThreadPostBody
import app.tiebalite.core.network.proto.thread.ThreadPbContentLite
import org.junit.Assert.assertEquals
import org.junit.Test

class ThreadContentMapperTest {
    private val mapper = ThreadContentMapper()

    @Test
    fun mapKeepsEmoticonIdFromTextAndNameFromDescription() {
        val body =
            mapper.map(
                listOf(
                    ThreadPbContentLite
                        .newBuilder()
                        .setType(2)
                        .setText("image_emoticon9")
                        .setC("#(泪)")
                        .build(),
                ),
            )

        assertEquals(
            RichTextPart.Emoticon(id = "image_emoticon9", name = "泪"),
            body.inline.single(),
        )
    }

    @Test
    fun mapKeepsVideoSizeFromWidthAndHeight() {
        val body =
            mapper.map(
                listOf(
                    ThreadPbContentLite
                        .newBuilder()
                        .setType(5)
                        .setLink("https://example.com/video.mp4")
                        .setSrc("https://example.com/cover.jpg")
                        .setWidth(576)
                        .setHeight(1024)
                        .build(),
                ),
            )

        val video = body.media.single() as ThreadPostBody.MediaPart.Video
        assertEquals(576, video.width)
        assertEquals(1024, video.height)
    }
}
