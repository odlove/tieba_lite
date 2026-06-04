package app.tiebalite.core.network.source.tbclient.forum

import app.tiebalite.core.network.source.tbclient.TestTbClientDevice
import app.tiebalite.core.network.source.tbclient.TestTbClientIdentity
import app.tiebalite.core.network.source.tbclient.TestTbClientScreen
import app.tiebalite.core.network.source.tbclient.TestTbClientTimestamp
import app.tiebalite.core.network.source.tbclient.decodeProtoFields
import app.tiebalite.core.network.source.tbclient.fieldNumbers
import app.tiebalite.core.network.source.tbclient.firstField
import org.junit.Assert.assertEquals
import org.junit.Test

class FrsPageNetworkSourceTest {
    @Test
    fun buildFrsPageRequestBodyMatchesApiLabFields() {
        val request =
            decodeProtoFields(
                buildFrsPageRequestBody(
                    identity = TestTbClientIdentity,
                    device = TestTbClientDevice,
                    screen = TestTbClientScreen,
                    timestamp = TestTbClientTimestamp,
                    forumName = "贴吧",
                    page = 2,
                    loadType = 1,
                    sortType = -1,
                    goodClassifyId = null,
                    bduss = "BDUSS",
                    stoken = "STOKEN",
                ),
            ).firstField(1).decodeMessage()

        assertEquals(
            listOf(
                1, 15, 2, 3, 8, 4, 5, 11, 12, 13, 16, 14, 27, 17, 18, 19, 40, 44,
                69, 45, 47, 87, 48, 50, 49, 52, 53, 55, 56, 58, 67, 65, 78, 68, 66,
                51, 59, 60, 61, 63, 62, 64, 70, 39, 72, 73, 74, 75, 76, 91, 92, 84,
                80, 88, 86, 82, 89, 90,
            ),
            request.fieldNumbers(),
        )
        assertEquals("%E8%B4%B4%E5%90%A7", request.firstField(1).stringValue())
        assertEquals(2L, request.firstField(15).varint)
        assertEquals(90L, request.firstField(2).varint)
        assertEquals(30L, request.firstField(3).varint)
        assertEquals(1L, request.firstField(8).varint)
        assertEquals(0L, request.firstField(4).varint)
        assertEquals(0L, request.firstField(5).varint)
        assertEquals(1080L, request.firstField(11).varint)
        assertEquals(2400L, request.firstField(12).varint)
        assertEquals(3.0, request.firstField(13).fixed64)
        assertEquals("", request.firstField(16).stringValue())
        assertEquals(2L, request.firstField(14).varint)
        assertEquals(1L, request.firstField(69).varint)
        assertEquals(-1, request.firstField(47).signedVarint32())
        assertEquals("2", request.firstField(52).stringValue())
        assertEquals("-2", request.firstField(53).stringValue())
        assertEquals(listOf(6, 2, 1, 7, 3), request.firstField(50).decodeMessage().fieldNumbers())
        assertEquals(listOf(1, 2, 3), request.firstField(51).decodeMessage().fieldNumbers())
        assertEquals(1L, request.firstField(59).varint)
        assertEquals("""{"iadex":"","nad_core_version":"6.39.0.5","floor_info":"","req_type":0,"pre_ad_thread_count":0}""", request.firstField(62).stringValue())
        assertEquals("Asia/Shanghai", request.firstField(64).decodeMessage().firstField(15).stringValue())
        assertEquals("1020031h", request.firstField(39).decodeMessage().firstField(6).stringValue())
        assertEquals(0L, request.firstField(39).decodeMessage().firstField(40).varint)
        assertEquals(1L, request.firstField(73).varint)
        assertEquals("%7B%7D", request.firstField(76).stringValue())
    }

    @Test
    fun buildFrsPageRequestBodyMarksGoodClassifyWhenProvided() {
        val request =
            decodeProtoFields(
                buildFrsPageRequestBody(
                    identity = TestTbClientIdentity,
                    device = TestTbClientDevice,
                    screen = TestTbClientScreen,
                    timestamp = TestTbClientTimestamp,
                    forumName = "tieba",
                    page = 1,
                    loadType = 2,
                    sortType = 0,
                    goodClassifyId = 123,
                    bduss = null,
                    stoken = null,
                ),
            ).firstField(1).decodeMessage()

        assertEquals(1L, request.firstField(4).varint)
        assertEquals(123L, request.firstField(5).varint)
        assertEquals(2L, request.firstField(49).varint)
        assertEquals("""{"iadex":"","nad_core_version":"6.39.0.5","floor_info":"","req_type":1,"pre_ad_thread_count":0}""", request.firstField(62).stringValue())
    }
}
