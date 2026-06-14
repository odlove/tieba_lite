package app.tiebalite.core.network.client

import okhttp3.FormBody
import org.junit.Assert.assertEquals
import org.junit.Test

class TbClientFormInterceptorTest {
    @Test
    fun buildSignedFormBodyAddsCommonParamsAndSign() {
        val body =
            FormBody.Builder()
                .add("bdusstoken", "bduss|suffix")
                .add("stoken", "stoken")
                .add("_client_version", "12.41.7.1")
                .build()

        val signed =
            TbClientFormInterceptor.buildSignedFormBody(
                body = body,
                clientId = "wappc_1_2",
                timestampMillis = 1234L,
                model = "Android",
                osVersion = "15",
            )
        val params = signed.toParamMap()

        assertEquals("bduss|suffix", params["bdusstoken"])
        assertEquals("stoken", params["stoken"])
        assertEquals("wappc_1_2", params["_client_id"])
        assertEquals("2", params["_client_type"])
        assertEquals("12.41.7.1", params["_client_version"])
        assertEquals("15", params["_os_version"])
        assertEquals("1234", params["_timestamp"])
        assertEquals("Android", params["model"])
        assertEquals("1", params["net_type"])
        assertEquals("", params["_phone_imei"])
        assertEquals("3E9867D50CF1E25849A474DC05EA9382", params["sign"])
        assertEquals("3E9867D50CF1E25849A474DC05EA9382", TbClientFormInterceptor.calculateSign(params))
        assertEquals(
            listOf(
                "_client_id",
                "_client_type",
                "_client_version",
                "_os_version",
                "_phone_imei",
                "_timestamp",
                "bdusstoken",
                "model",
                "net_type",
                "sign",
                "stoken",
            ),
            signed.names(),
        )
    }
}

private fun FormBody.toParamMap(): Map<String, String> =
    buildMap {
        repeat(this@toParamMap.size) { index ->
            put(name(index), value(index))
        }
    }

private fun FormBody.names(): List<String> =
    List(size) { index ->
        name(index)
    }
