package app.tiebalite.core.network.source.tbclient.thread

import app.tiebalite.core.network.source.tbclient.TestTbClientDevice
import app.tiebalite.core.network.source.tbclient.TestTbClientIdentity
import app.tiebalite.core.network.source.tbclient.TestTbClientScreen
import app.tiebalite.core.network.source.tbclient.TestTbClientTimestamp
import app.tiebalite.core.network.source.tbclient.decodeProtoFields
import app.tiebalite.core.network.source.tbclient.fieldNumbers
import app.tiebalite.core.network.source.tbclient.firstField
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class PbPageNetworkSourceTest {
    @Test
    fun buildPbPageRequestBodyMatchesApiLabFields() {
        val request =
            decodeProtoFields(
                buildPbPageRequestBody(
                    identity = TestTbClientIdentity,
                    device = TestTbClientDevice,
                    screen = TestTbClientScreen,
                    timestamp = TestTbClientTimestamp,
                    threadId = 123L,
                    page = 2,
                    seeLz = false,
                    sortType = 0,
                    lastPostId = null,
                    bduss = "BDUSS",
                    stoken = "STOKEN",
                ),
            ).firstField(1).decodeMessage()

        assertEquals(listOf(18, 6, 1, 2, 8, 3, 5, 36), request.fieldNumbers())
        assertEquals(listOf(4, 1, 2, 3), request.firstField(18).decodeMessage().fieldNumbers())
        assertEquals(0L, request.firstField(6).varint)
        assertEquals(123L, request.firstField(2).varint)
        assertFalse(request.any { field -> field.number == 7 })
        assertEquals(0L, request.firstField(8).varint)
        assertEquals(2L, request.firstField(3).varint)
        assertEquals(0L, request.firstField(5).varint)
        assertEquals(1L, request.firstField(36).varint)
        assertFalse(request.any { field -> field.number == 4 })
        assertFalse(request.any { field -> field.number == 81 })
        assertEquals(2L, request.firstField(1).decodeMessage().firstField(40).varint)
    }

    @Test
    fun buildPbPageRequestBodyIncludesSeeLzOnlyWhenEnabled() {
        val request =
            decodeProtoFields(
                buildPbPageRequestBody(
                    identity = TestTbClientIdentity,
                    device = TestTbClientDevice,
                    screen = TestTbClientScreen,
                    timestamp = TestTbClientTimestamp,
                    threadId = 123L,
                    page = 1,
                    seeLz = true,
                    sortType = 1,
                    lastPostId = null,
                    bduss = null,
                    stoken = null,
                ),
            ).firstField(1).decodeMessage()

        assertEquals(listOf(18, 6, 1, 2, 7, 8, 3, 5, 36), request.fieldNumbers())
        assertEquals(1L, request.firstField(7).varint)
    }

    @Test
    fun buildPbPageRequestBodyIncludesLatestPostAnchor() {
        val request =
            decodeProtoFields(
                buildPbPageRequestBody(
                    identity = TestTbClientIdentity,
                    device = TestTbClientDevice,
                    screen = TestTbClientScreen,
                    timestamp = TestTbClientTimestamp,
                    threadId = 123L,
                    page = 1,
                    seeLz = false,
                    sortType = 0,
                    lastPostId = 456L,
                    bduss = null,
                    stoken = null,
                ),
            ).firstField(1).decodeMessage()

        assertEquals(listOf(18, 6, 1, 2, 4, 8, 3, 5, 36), request.fieldNumbers())
        assertEquals(456L, request.firstField(4).varint)
        assertEquals(2L, request.firstField(8).varint)
        assertFalse(request.any { field -> field.number == 81 })
    }
}
