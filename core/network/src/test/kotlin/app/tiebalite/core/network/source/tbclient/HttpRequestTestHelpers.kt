package app.tiebalite.core.network.source.tbclient

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import retrofit2.Retrofit

internal class CapturingInterceptor(
    private val responseBody: ByteArray = ByteArray(0),
    private val responseContentType: String = "application/octet-stream",
) : Interceptor {
    lateinit var request: Request
        private set

    val hasRequest: Boolean
        get() = ::request.isInitialized

    override fun intercept(chain: Interceptor.Chain): Response {
        request = chain.request()
        return Response
            .Builder()
            .request(request)
            .protocol(Protocol.HTTP_1_1)
            .code(200)
            .message("OK")
            .body(responseBody.toResponseBody(responseContentType.toMediaType()))
            .build()
    }
}

internal fun retrofitForCapture(
    capture: CapturingInterceptor,
    configureClient: OkHttpClient.Builder.() -> Unit = {},
): Retrofit {
    val client =
        OkHttpClient
            .Builder()
            .apply(configureClient)
            .addInterceptor(capture)
            .build()
    return Retrofit
        .Builder()
        .baseUrl("https://tiebac.baidu.com/")
        .client(client)
        .build()
}

internal fun requestBodyText(request: Request): String {
    val buffer = Buffer()
    request.body?.writeTo(buffer)
    return buffer.readByteArray().toString(Charsets.ISO_8859_1)
}

internal fun <T> runSuspend(block: suspend () -> T): T = runBlocking { block() }
