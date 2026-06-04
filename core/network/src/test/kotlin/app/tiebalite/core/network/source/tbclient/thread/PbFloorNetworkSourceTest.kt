package app.tiebalite.core.network.source.tbclient.thread

import app.tiebalite.core.network.source.tbclient.TestTbClientDevice
import app.tiebalite.core.network.source.tbclient.TestTbClientIdentity
import app.tiebalite.core.network.source.tbclient.TestTbClientScreen
import app.tiebalite.core.network.source.tbclient.TestTbClientTimestamp
import app.tiebalite.core.network.source.tbclient.decodeProtoFields
import app.tiebalite.core.network.source.tbclient.fieldNumbers
import app.tiebalite.core.network.source.tbclient.firstField
import org.junit.Assert.assertEquals
import org.junit.Test

class PbFloorNetworkSourceTest {
    @Test
    fun buildPbFloorRequestBodyMatchesApiLabFields() {
        val request =
            decodeProtoFields(
                buildPbFloorRequestBody(
                    identity = TestTbClientIdentity,
                    device = TestTbClientDevice,
                    screen = TestTbClientScreen,
                    timestamp = TestTbClientTimestamp,
                    threadId = 123L,
                    postId = 456L,
                    page = 2,
                    subPostId = 789L,
                    forumId = 321L,
                    isCommReverse = 0,
                    requestTimes = 0,
                    bduss = "BDUSS",
                    stoken = "STOKEN",
                ),
            ).firstField(1).decodeMessage()

        assertEquals(listOf(9, 11, 10, 1, 12, 13, 15, 16, 2, 4, 18, 7, 6, 5, 3, 8, 17), request.fieldNumbers())
        assertEquals(0L, request.firstField(9).decodeMessage().firstField(40).varint)
        assertEquals(321L, request.firstField(11).varint)
        assertEquals(0L, request.firstField(10).varint)
        assertEquals(123L, request.firstField(1).varint)
        assertEquals("", request.firstField(12).stringValue())
        assertEquals("", request.firstField(13).stringValue())
        assertEquals(0L, request.firstField(15).varint)
        assertEquals("", request.firstField(16).stringValue())
        assertEquals(456L, request.firstField(2).varint)
        assertEquals(2L, request.firstField(4).varint)
        assertEquals(0L, request.firstField(18).varint)
        assertEquals(3.0, request.firstField(7).fixed64)
        assertEquals(2400L, request.firstField(6).varint)
        assertEquals(1080L, request.firstField(5).varint)
        assertEquals(789L, request.firstField(3).varint)
        assertEquals("", request.firstField(8).stringValue())
        assertEquals("", request.firstField(17).stringValue())
    }
}
