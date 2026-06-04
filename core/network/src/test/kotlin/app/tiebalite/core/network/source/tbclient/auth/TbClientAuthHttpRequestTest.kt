package app.tiebalite.core.network.source.tbclient.auth

import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientFormInterceptor
import app.tiebalite.core.network.source.tbclient.CapturingInterceptor
import app.tiebalite.core.network.source.tbclient.buildDataPart
import app.tiebalite.core.network.source.tbclient.buildTextParts
import app.tiebalite.core.network.source.tbclient.requestBodyText
import app.tiebalite.core.network.source.tbclient.retrofitForCapture
import app.tiebalite.core.network.source.tbclient.runSuspend
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TbClientAuthHttpRequestTest {
    @Test
    fun loginRequestUsesSignedFormShape() {
        val capture =
            CapturingInterceptor(
                responseBody = """{"error_code":"0","anti":{"tbs":"tbs"},"user":{"id":"1","name":"name","portrait":"portrait"}}""".toByteArray(),
                responseContentType = "application/json",
            )
        val retrofit =
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
        val api = retrofit.create(TbClientLoginApi::class.java)

        runSuspend {
            api.login(
                bdussToken = "BDUSS|",
                stoken = "STOKEN",
            ).close()
        }

        val request = capture.request
        val body = requestBodyText(request)
        assertEquals("/c/s/login", request.url.encodedPath)
        assertEquals("POST", request.method)
        assertEquals(NetworkDefaults.TBCLIENT_USER_AGENT, request.header("User-Agent"))
        assertEquals("ka=open", request.header("Cookie"))
        assertTrue(body.contains("bdusstoken=BDUSS%7C"))
        assertTrue(body.contains("stoken=STOKEN"))
        assertTrue(body.contains("channel_id="))
        assertTrue(body.contains("channel_uid="))
        assertTrue(body.contains("first_login=1"))
        assertTrue(body.contains("send_tb=0"))
        assertTrue(body.contains("_client_id=wappc_1_2"))
        assertTrue(body.contains("_client_type=2"))
        assertTrue(body.contains("_client_version=${NetworkDefaults.TBCLIENT_CLIENT_VERSION}"))
        assertTrue(body.contains("_os_version=15"))
        assertTrue(body.contains("_timestamp=1234"))
        assertTrue(body.contains("model=Android"))
        assertTrue(body.contains("net_type=1"))
        assertTrue(body.contains("phone_imei="))
        assertNotNull(body.substringAfter("sign=", missingDelimiterValue = "").takeIf { it.length >= 32 })
    }

    @Test
    fun profileRequestOmitsFormatQueryAndSendsStokenPart() {
        val capture = CapturingInterceptor()
        val api = retrofitForCapture(capture).create(TbClientProfileApi::class.java)

        runSuspend {
            api.getProfile(
                cookie = "ka=open;CUID=cuid;TBBRAND=Android;",
                cuid = "cuid",
                cuidGalaxy2 = "cuid",
                c3Aid = "c3aid",
                formParts = buildTextParts(mapOf("stoken" to "STOKEN")),
                data = buildDataPart(byteArrayOf(1, 2, 3)),
            ).close()
        }

        val request = capture.request
        val body = requestBodyText(request)
        assertEquals("/c/u/user/profile", request.url.encodedPath)
        assertEquals(NetworkDefaults.PROFILE_CMD.toString(), request.url.queryParameter("cmd"))
        assertFalse(request.url.queryParameterNames.contains("format"))
        assertEquals("protobuf", request.header("x_bd_data_type"))
        assertEquals(NetworkDefaults.TBCLIENT_USER_AGENT, request.header("User-Agent"))
        assertTrue(body.contains("name=\"stoken\""))
        assertFalse(body.contains("name=\"BDUSS\""))
        assertTrue(body.contains("name=\"data\"; filename=\"file\""))
    }
}
