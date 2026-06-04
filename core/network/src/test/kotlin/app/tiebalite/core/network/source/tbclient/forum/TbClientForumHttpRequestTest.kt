package app.tiebalite.core.network.source.tbclient.forum

import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.source.tbclient.CapturingInterceptor
import app.tiebalite.core.network.source.tbclient.buildDataPart
import app.tiebalite.core.network.source.tbclient.buildTextParts
import app.tiebalite.core.network.source.tbclient.requestBodyText
import app.tiebalite.core.network.source.tbclient.retrofitForCapture
import app.tiebalite.core.network.source.tbclient.runSuspend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class TbClientForumHttpRequestTest {
    @Test
    fun forumGuideRequestSendsFormatAndAuthParts() {
        val capture = CapturingInterceptor()
        val api = retrofitForCapture(capture).create(ForumGuideApi::class.java)

        runSuspend {
            api.getForumGuide(
                cookie = "ka=open;CUID=cuid;TBBRAND=Android;",
                cuid = "cuid",
                cuidGalaxy2 = "cuid",
                c3Aid = "c3aid",
                formParts = buildTextParts(mapOf("BDUSS" to "BDUSS", "stoken" to "STOKEN")),
                data = buildDataPart(byteArrayOf(1, 2, 3)),
            ).close()
        }

        val request = capture.request
        val body = requestBodyText(request)
        assertEquals("/c/f/forum/forumGuide", request.url.encodedPath)
        assertEquals(NetworkDefaults.FORUM_GUIDE_CMD.toString(), request.url.queryParameter("cmd"))
        assertEquals("protobuf", request.url.queryParameter("format"))
        assertEquals("protobuf", request.header("x_bd_data_type"))
        assertTrue(body.contains("name=\"BDUSS\""))
        assertTrue(body.contains("name=\"stoken\""))
        assertTrue(body.contains("name=\"data\"; filename=\"file\""))
    }

    @Test
    fun frsPageRequestSendsEncodedForumHeaderAndStokenPart() {
        val capture = CapturingInterceptor()
        val api = retrofitForCapture(capture).create(FrsPageApi::class.java)

        runSuspend {
            api.getFrsPage(
                cookie = "ka=open;CUID=cuid;TBBRAND=Android;",
                cuid = "cuid",
                cuidGalaxy2 = "cuid",
                c3Aid = "c3aid",
                forumName = "%E8%B4%B4%E5%90%A7",
                formParts = buildTextParts(mapOf("stoken" to "STOKEN")),
                data = buildDataPart(byteArrayOf(1, 2, 3)),
            ).close()
        }

        val request = capture.request
        val body = requestBodyText(request)
        assertEquals("/c/f/frs/page", request.url.encodedPath)
        assertEquals(NetworkDefaults.FRS_PAGE_CMD.toString(), request.url.queryParameter("cmd"))
        assertEquals("protobuf", request.url.queryParameter("format"))
        assertEquals("%E8%B4%B4%E5%90%A7", request.header("forum_name"))
        assertTrue(body.contains("name=\"stoken\""))
        assertTrue(body.contains("name=\"data\"; filename=\"file\""))
    }
}
