package app.tiebalite.core.network.source.tbclient.recommend

import app.tiebalite.core.network.source.tbclient.TestTbClientDevice
import app.tiebalite.core.network.source.tbclient.TestTbClientIdentity
import app.tiebalite.core.network.source.tbclient.TestTbClientScreen
import app.tiebalite.core.network.source.tbclient.TestTbClientTimestamp
import app.tiebalite.core.network.source.tbclient.decodeProtoFields
import app.tiebalite.core.network.source.tbclient.fieldNumbers
import app.tiebalite.core.network.source.tbclient.firstField
import org.junit.Assert.assertEquals
import org.junit.Test

class PersonalizedNetworkSourceTest {
    @Test
    fun buildPersonalizedRequestBodyMatchesApiLabFields() {
        val request =
            decodeProtoFields(
                buildPersonalizedRequestBody(
                    identity = TestTbClientIdentity,
                    device = TestTbClientDevice,
                    screen = TestTbClientScreen,
                    timestamp = TestTbClientTimestamp,
                    loadType = 1,
                    page = 2,
                    threadCount = 11,
                    requestTimes = 1,
                    isNewfeed = 1,
                    bduss = "BDUSS",
                    stoken = "STOKEN",
                ),
            ).firstField(1).decodeMessage()

        assertEquals(listOf(36, 1, 29, 40, 4, 22, 3, 27, 23, 5, 6, 26, 11, 28, 10, 9, 8, 7, 2), request.fieldNumbers())
        assertEquals(listOf(6, 2, 1, 7, 3), request.firstField(36).decodeMessage().fieldNumbers())
        assertEquals(0L, request.firstField(1).decodeMessage().firstField(40).varint)
        assertEquals("", request.firstField(29).stringValue())
        assertEquals(1L, request.firstField(40).varint)
        assertEquals(1L, request.firstField(4).varint)
        assertEquals(0L, request.firstField(22).varint)
        assertEquals(0L, request.firstField(3).varint)
        assertEquals(0L, request.firstField(27).varint)
        assertEquals(1L, request.firstField(23).varint)
        assertEquals(11L, request.firstField(5).varint)
        assertEquals(2L, request.firstField(6).varint)
        assertEquals(0L, request.firstField(26).varint)
        assertEquals(1L, request.firstField(11).varint)
        assertEquals(1L, request.firstField(28).varint)
        assertEquals(3.0, request.firstField(10).fixed64)
        assertEquals(2400L, request.firstField(9).varint)
        assertEquals(1080L, request.firstField(8).varint)
        assertEquals(0L, request.firstField(7).varint)
        assertEquals(0L, request.firstField(2).varint)
    }
}
