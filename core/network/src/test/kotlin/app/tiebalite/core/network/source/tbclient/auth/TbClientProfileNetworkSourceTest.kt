package app.tiebalite.core.network.source.tbclient.auth

import app.tiebalite.core.network.source.tbclient.TestTbClientDevice
import app.tiebalite.core.network.source.tbclient.TestTbClientIdentity
import app.tiebalite.core.network.source.tbclient.TestTbClientScreen
import app.tiebalite.core.network.source.tbclient.TestTbClientTimestamp
import app.tiebalite.core.network.source.tbclient.decodeProtoFields
import app.tiebalite.core.network.source.tbclient.fieldNumbers
import app.tiebalite.core.network.source.tbclient.firstField
import org.junit.Assert.assertEquals
import org.junit.Test

class TbClientProfileNetworkSourceTest {
    @Test
    fun buildProfileRequestBodyMatchesApiLabFields() {
        val request =
            decodeProtoFields(
                buildProfileRequestBody(
                    identity = TestTbClientIdentity,
                    device = TestTbClientDevice,
                    screen = TestTbClientScreen,
                    timestamp = TestTbClientTimestamp,
                    userId = 123L,
                    page = 2,
                    postCount = 3,
                    bduss = "BDUSS",
                    stoken = "STOKEN",
                ),
            ).firstField(1).decodeMessage()

        assertEquals(listOf(1, 2, 6, 7, 8, 14, 15, 10, 11, 13, 12, 17, 18, 19, 9), request.fieldNumbers())
        assertEquals(123L, request.firstField(1).varint)
        assertEquals(3L, request.firstField(2).varint)
        assertEquals(1L, request.firstField(6).varint)
        assertEquals(1L, request.firstField(7).varint)
        assertEquals(1L, request.firstField(8).varint)
        assertEquals(1L, request.firstField(14).varint)
        assertEquals(2L, request.firstField(15).varint)
        assertEquals(1080L, request.firstField(10).varint)
        assertEquals(2400L, request.firstField(11).varint)
        assertEquals(3.0, request.firstField(13).fixed64)
        assertEquals(0L, request.firstField(12).varint)
        assertEquals("", request.firstField(17).stringValue())
        assertEquals("", request.firstField(18).stringValue())
        assertEquals(1L, request.firstField(19).varint)
        assertEquals(0L, request.firstField(9).decodeMessage().firstField(40).varint)
    }
}
