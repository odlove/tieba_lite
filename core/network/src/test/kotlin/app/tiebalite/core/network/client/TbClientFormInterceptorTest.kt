package app.tiebalite.core.network.client

import okhttp3.FormBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class TbClientFormInterceptorTest {
    @Test
    fun buildSignedFormBodyAddsCommonParamsAndSign() {
        val body =
            FormBody.Builder()
                .add("bdusstoken", "bduss|null")
                .add("stoken", "stoken")
                .add("_client_version", "12.41.7.1")
                .build()

        val signed =
            TbClientFormInterceptor.buildSignedFormBody(
                body = body,
                clientId = "wappc_1_2",
                timestampMillis = 1234L,
                model = "Android",
                osVersion = "35",
            )
        val params = signed.toParamMap()

        assertEquals("bduss|null", params["bdusstoken"])
        assertEquals("stoken", params["stoken"])
        assertEquals("wappc_1_2", params["_client_id"])
        assertEquals("2", params["_client_type"])
        assertEquals("12.41.7.1", params["_client_version"])
        assertEquals("35", params["_os_version"])
        assertEquals("1234", params["_timestamp"])
        assertEquals("Android", params["model"])
        assertEquals("1", params["net_type"])
        assertEquals("", params["phone_imei"])
        assertNotNull(params["sign"])
        assertEquals(TbClientFormInterceptor.calculateSign(params), params["sign"])
    }
}

private fun FormBody.toParamMap(): Map<String, String> =
    buildMap {
        repeat(this@toParamMap.size) { index ->
            put(name(index), value(index))
        }
    }
