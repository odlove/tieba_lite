package app.tiebalite.core.data.recommend.mapper

import app.tiebalite.core.network.proto.recommend.MediaLite
import app.tiebalite.core.network.proto.recommend.PersonalizedResponseDataLite
import app.tiebalite.core.network.proto.recommend.PersonalizedResponseLite
import app.tiebalite.core.network.proto.recommend.ThreadInfoLite
import app.tiebalite.core.network.source.tbclient.recommend.PersonalizedFeedRaw
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalizedFeedMapperTest {
    private val mapper = PersonalizedFeedMapper()

    @Test
    fun mapImagesFallsBackToBigAndSrcPic() {
        val raw =
            PersonalizedFeedRaw(
                body = ByteArray(0),
                response =
                    PersonalizedResponseLite
                        .newBuilder()
                        .setData(
                            PersonalizedResponseDataLite
                                .newBuilder()
                                .addThreadList(
                                    ThreadInfoLite
                                        .newBuilder()
                                        .setId(1)
                                        .setTitle("title")
                                        .addMedia(
                                            MediaLite
                                                .newBuilder()
                                                .setBigPic("//example.com/big.jpg")
                                                .setWidth(100)
                                                .setHeight(200),
                                        ).addMedia(
                                            MediaLite
                                                .newBuilder()
                                                .setSrcPic("https://example.com/src.jpg"),
                                        ),
                                ),
                        ).build(),
            )

        val item = mapper.map(raw).single()

        assertEquals(
            listOf(
                "https://example.com/big.jpg",
                "https://example.com/src.jpg",
            ),
            item.images.map { image -> image.url },
        )
        assertEquals(100, item.images[0].width)
        assertEquals(200, item.images[0].height)
    }
}
