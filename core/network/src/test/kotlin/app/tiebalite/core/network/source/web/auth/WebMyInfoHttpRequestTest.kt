package app.tiebalite.core.network.source.web.auth

import app.tiebalite.core.network.source.tbclient.CapturingInterceptor
import app.tiebalite.core.network.source.tbclient.retrofitForCapture
import app.tiebalite.core.network.source.tbclient.runSuspend
import org.junit.Assert.assertEquals
import org.junit.Test

class WebMyInfoHttpRequestTest {
    @Test
    fun webMyInfoRequestUsesWebviewHeaders() {
        val capture =
            CapturingInterceptor(
                responseBody = """{"no":0,"data":{"is_login":true}}""".toByteArray(),
                responseContentType = "application/json",
            )
        val api = retrofitForCapture(capture).create(WebMyInfoApi::class.java)

        runSuspend {
            api.getMyInfo(cookie = "BDUSS=BDUSS;STOKEN=STOKEN").close()
        }

        val request = capture.request
        assertEquals("/mo/q/newmoindex", request.url.encodedPath)
        assertEquals("1", request.url.queryParameter("need_user"))
        assertEquals("BDUSS=BDUSS;STOKEN=STOKEN", request.header("cookie"))
        assertEquals(WEBVIEW_USER_AGENT, request.header("User-Agent"))
        assertEquals("https://tieba.baidu.com/", request.header("Referer"))
    }
}
