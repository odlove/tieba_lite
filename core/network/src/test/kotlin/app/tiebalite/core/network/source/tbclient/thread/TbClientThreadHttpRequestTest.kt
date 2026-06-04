package app.tiebalite.core.network.source.tbclient.thread

import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.source.tbclient.CapturingInterceptor
import app.tiebalite.core.network.source.tbclient.buildDataPart
import app.tiebalite.core.network.source.tbclient.buildTextParts
import app.tiebalite.core.network.source.tbclient.requestBodyText
import app.tiebalite.core.network.source.tbclient.retrofitForCapture
import app.tiebalite.core.network.source.tbclient.runSuspend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TbClientThreadHttpRequestTest {
    @Test
    fun pbPageRequestSendsThreadHeaderAndNoStokenPart() {
        val capture = CapturingInterceptor()
        val api = retrofitForCapture(capture).create(PbPageApi::class.java)

        runSuspend {
            api.getPbPage(
                cookie = "ka=open;CUID=cuid;TBBRAND=Android;",
                cuid = "cuid",
                cuidGalaxy2 = "cuid",
                c3Aid = "c3aid",
                threadId = "123",
                data = buildDataPart(byteArrayOf(1, 2, 3)),
            ).close()
        }

        val request = capture.request
        val body = requestBodyText(request)
        assertEquals("/c/f/pb/page", request.url.encodedPath)
        assertEquals(NetworkDefaults.PB_PAGE_CMD.toString(), request.url.queryParameter("cmd"))
        assertEquals("protobuf", request.url.queryParameter("format"))
        assertEquals("123", request.header("thread_id"))
        assertFalse(body.contains("name=\"stoken\""))
        assertTrue(body.contains("name=\"data\"; filename=\"file\""))
    }

    @Test
    fun pbFloorRequestSendsThreadHeaderAndStokenPart() {
        val capture = CapturingInterceptor()
        val api = retrofitForCapture(capture).create(PbFloorApi::class.java)

        runSuspend {
            api.getPbFloor(
                cookie = "ka=open;CUID=cuid;TBBRAND=Android;",
                cuid = "cuid",
                cuidGalaxy2 = "cuid",
                c3Aid = "c3aid",
                threadId = "123",
                formParts = buildTextParts(mapOf("stoken" to "STOKEN")),
                data = buildDataPart(byteArrayOf(1, 2, 3)),
            ).close()
        }

        val request = capture.request
        val body = requestBodyText(request)
        assertEquals("/c/f/pb/floor", request.url.encodedPath)
        assertEquals(NetworkDefaults.PB_FLOOR_CMD.toString(), request.url.queryParameter("cmd"))
        assertEquals("protobuf", request.url.queryParameter("format"))
        assertEquals("123", request.header("thread_id"))
        assertTrue(body.contains("name=\"stoken\""))
        assertTrue(body.contains("name=\"data\"; filename=\"file\""))
    }
}
