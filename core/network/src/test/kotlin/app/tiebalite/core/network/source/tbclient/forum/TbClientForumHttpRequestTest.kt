package app.tiebalite.core.network.source.tbclient.forum

import app.tiebalite.core.model.error.UserVisibleException
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientFormInterceptor
import app.tiebalite.core.network.source.tbclient.CapturingInterceptor
import app.tiebalite.core.network.source.tbclient.buildDataPart
import app.tiebalite.core.network.source.tbclient.buildTextParts
import app.tiebalite.core.network.source.tbclient.requestBodyText
import app.tiebalite.core.network.source.tbclient.retrofitForCapture
import app.tiebalite.core.network.source.tbclient.runSuspend
import okhttp3.FormBody
import okhttp3.Request
import okhttp3.ResponseBody
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    @Test
    fun forumSignRequestUsesOfficialSignedFormShape() {
        val capture = CapturingInterceptor(responseBody = """{"error_code":"0"}""".toByteArray())
        val api = signedRetrofitForCapture(capture).create(ForumSignApi::class.java)

        runSuspend {
            api.signInForum(
                bduss = "BDUSS",
                tbs = "TBS",
                forumName = "python",
                forumId = "155829",
            ).close()
        }

        val request = capture.request
        val params = requestFormParams(request)
        assertEquals("/c/c/forum/sign", request.url.encodedPath)
        assertEquals(NetworkDefaults.TBCLIENT_USER_AGENT, request.header("User-Agent"))
        assertEquals("ka=open", request.header("Cookie"))
        assertEquals(
            setOf(
                "BDUSS",
                "_client_id",
                "_client_type",
                "_client_version",
                "_os_version",
                "_phone_imei",
                "_timestamp",
                "fid",
                "kw",
                "model",
                "net_type",
                "sign",
                "stErrorNums",
                "tbs",
            ),
            params.keys,
        )
        assertEquals("BDUSS", params["BDUSS"])
        assertEquals("TBS", params["tbs"])
        assertEquals("155829", params["fid"])
        assertEquals("python", params["kw"])
        assertEquals("", params["_phone_imei"])
        assertEquals("0", params["stErrorNums"])
        assertEquals("7DC531CCD3B1C71F66195E272881E6F0", params["sign"])
    }

    @Test
    fun forumSignRequestCanPassOptionalAuthSid() {
        val capture = CapturingInterceptor(responseBody = """{"error_code":"0"}""".toByteArray())
        val api = signedRetrofitForCapture(capture).create(ForumSignApi::class.java)

        runSuspend {
            api.signInForum(
                bduss = "BDUSS",
                tbs = "TBS",
                forumName = "python",
                forumId = "155829",
                authSid = "AUTH",
            ).close()
        }

        val params = requestFormParams(capture.request)
        assertEquals("AUTH", params["authsid"])
    }

    @Test
    fun forumSignSourceSucceedsWhenErrorCodeIsExplicitZero() {
        val source = ForumSignNetworkSource(StubForumSignApi("""{"error_code":"0"}"""))

        val result =
            runSuspend {
                source.signInForum(
                    bduss = "BDUSS",
                    tbs = "TBS",
                    forumName = "python",
                    forumId = 155829,
                )
            }

        assertTrue(result.isSuccess)
    }

    @Test
    fun forumSignSourceReturnsUserVisibleServerError() {
        val source =
            ForumSignNetworkSource(
                StubForumSignApi("""{"error_code":"4","usermsg":"已经签过到"}"""),
            )

        val result =
            runSuspend {
                source.signInForum(
                    bduss = "BDUSS",
                    tbs = "TBS",
                    forumName = "python",
                    forumId = 155829,
                )
            }

        assertFalse(result.isSuccess)
        val throwable = result.exceptionOrNull() as UserVisibleException
        assertEquals("已经签过到", throwable.userMessage)
    }

    @Test
    fun forumSignSourceRejectsMissingErrorCode() {
        val source = ForumSignNetworkSource(StubForumSignApi("""{"usermsg":"ok"}"""))

        val result =
            runSuspend {
                source.signInForum(
                    bduss = "BDUSS",
                    tbs = "TBS",
                    forumName = "python",
                    forumId = 155829,
                )
            }

        assertFalse(result.isSuccess)
        val throwable = result.exceptionOrNull() as UserVisibleException
        assertEquals("服务端未确认操作结果", throwable.userMessage)
    }

    @Test
    fun forumSignSourceRejectsNonNumericErrorCode() {
        val source = ForumSignNetworkSource(StubForumSignApi("""{"error_code":"ok"}"""))

        val result =
            runSuspend {
                source.signInForum(
                    bduss = "BDUSS",
                    tbs = "TBS",
                    forumName = "python",
                    forumId = 155829,
                )
            }

        assertFalse(result.isSuccess)
        val throwable = result.exceptionOrNull() as UserVisibleException
        assertEquals("服务端未确认操作结果", throwable.userMessage)
    }

    private fun requestFormParams(request: Request): Map<String, String> {
        val body = request.body as FormBody
        return buildMap {
            repeat(body.size) { index ->
                put(body.name(index), body.value(index))
            }
        }
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

    private class StubForumSignApi(
        private val body: String,
    ) : ForumSignApi {
        override suspend fun signInForum(
            bduss: String,
            tbs: String,
            forumId: String,
            forumName: String,
            authSid: String?,
            userAgent: String,
            cookie: String,
        ): ResponseBody = body.toResponseBody()
    }
}
