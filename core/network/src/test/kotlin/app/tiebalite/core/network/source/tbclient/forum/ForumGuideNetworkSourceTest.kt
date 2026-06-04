package app.tiebalite.core.network.source.tbclient.forum

import org.junit.Assert.assertArrayEquals
import org.junit.Test

class ForumGuideNetworkSourceTest {
    @Test
    fun buildForumGuideRequestBodyMatchesApiLabFields() {
        val bytes =
            buildForumGuideRequestBody(
                sortType = 3,
                callFrom = 0,
            )

        assertArrayEquals(
            byteArrayOf(
                0x0a,
                0x04,
                0x10,
                0x03,
                0x18,
                0x00,
            ),
            bytes,
        )
    }
}
