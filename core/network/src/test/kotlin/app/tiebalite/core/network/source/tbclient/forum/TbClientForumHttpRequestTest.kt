package app.tiebalite.core.network.source.tbclient.forum

import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientFormInterceptor
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

    @Test
    fun forumLikeRequestUsesOfficialSignedFormShape() {
        val capture = CapturingInterceptor(responseBody = """{"error_code":"0"}""".toByteArray())
        val api = signedRetrofitForCapture(capture).create(ForumLikeApi::class.java)

        runSuspend {
            api.likeForum(
                bduss = "BDUSS",
                tbs = "TBS",
                forumName = "python",
                forumId = "155829",
                duplicatedForumName = "python",
                userId = "1",
                userName = "name",
            ).close()
        }

        val request = capture.request
        val body = requestBodyText(request)
        assertEquals("/c/c/forum/like", request.url.encodedPath)
        assertEquals(NetworkDefaults.TBCLIENT_USER_AGENT, request.header("User-Agent"))
        assertEquals("ka=open", request.header("Cookie"))
        assertTrue(body.contains("BDUSS=BDUSS"))
        assertTrue(body.contains("tbs=TBS"))
        assertTrue(body.contains("kw=python"))
        assertTrue(body.contains("fid=155829"))
        assertTrue(body.contains("forum_name=python"))
        assertTrue(body.contains("has_head=0"))
        assertTrue(body.contains("user_id=1"))
        assertTrue(body.contains("user_name=name"))
        assertTrue(body.contains("_phone_imei="))
        assertTrue(body.contains("stErrorNums=0"))
        assertTrue(body.contains("sign=6258194D4C7DBBD165778E49A31490AA"))
    }

    @Test
    fun forumUnlikeRequestUsesOfficialSignedFormShape() {
        val capture = CapturingInterceptor(responseBody = """{"error_code":"0"}""".toByteArray())
        val api = signedRetrofitForCapture(capture).create(ForumLikeApi::class.java)

        runSuspend {
            api.unlikeForum(
                bduss = "BDUSS",
                tbs = "TBS",
                forumName = "python",
                forumId = "155829",
            ).close()
        }

        val request = capture.request
        val body = requestBodyText(request)
        assertEquals("/c/c/forum/unfavolike", request.url.encodedPath)
        assertTrue(body.contains("BDUSS=BDUSS"))
        assertTrue(body.contains("tbs=TBS"))
        assertTrue(body.contains("kw=python"))
        assertTrue(body.contains("fid=155829"))
        assertTrue(body.contains("favo_type=1"))
        assertTrue(body.contains("st_type=$DEFAULT_UNLIKE_ST_TYPE"))
        assertTrue(body.contains("_phone_imei="))
        assertTrue(body.contains("stErrorNums=0"))
        assertTrue(body.contains("sign=AD2AC6F7FE7498E06DE7A1384BCE6F91"))
    }

    private fun signedRetrofitForCapture(capture: CapturingInterceptor) =
        retrofitForCapture(capture) {
            addInterceptor(
                TbClientFormInterceptor(
                    clientIdProvider = { "wappc_1_2" },
                    timestampProvider = { 1234L },
                    modelProvider = { "Android" },
                    osVersionProvider = { "15" },
                ),
            )
        }
}
