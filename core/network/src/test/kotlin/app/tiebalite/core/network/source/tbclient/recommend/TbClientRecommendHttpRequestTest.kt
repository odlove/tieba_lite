package app.tiebalite.core.network.source.tbclient.recommend

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

class TbClientRecommendHttpRequestTest {
    @Test
    fun personalizedRequestOmitsFormatQueryAndSendsStokenPart() {
        val capture = CapturingInterceptor()
        val api = retrofitForCapture(capture).create(PersonalizedApi::class.java)

        runSuspend {
            api.getPersonalizedFeed(
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
        assertEquals("/c/f/excellent/personalized", request.url.encodedPath)
        assertEquals(NetworkDefaults.PERSONALIZED_CMD.toString(), request.url.queryParameter("cmd"))
        assertFalse(request.url.queryParameterNames.contains("format"))
        assertEquals("protobuf", request.header("x_bd_data_type"))
        assertTrue(body.contains("name=\"stoken\""))
        assertTrue(body.contains("name=\"data\"; filename=\"file\""))
    }
}
