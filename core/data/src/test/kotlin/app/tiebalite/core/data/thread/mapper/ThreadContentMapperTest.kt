package app.tiebalite.core.data.thread.mapper

import app.tiebalite.core.model.text.RichTextPart
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
}
