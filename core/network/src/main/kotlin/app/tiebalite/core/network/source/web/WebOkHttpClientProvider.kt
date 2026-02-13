package app.tiebalite.core.network.source.web

import app.tiebalite.core.network.client.NetworkClientFactory
import okhttp3.OkHttpClient

object WebOkHttpClientProvider {
    fun create(
        store: WebCookieJarStore,
        connectTimeoutSeconds: Long = 15,
        readTimeoutSeconds: Long = 20,
        writeTimeoutSeconds: Long = 20,
    ): OkHttpClient =
        NetworkClientFactory
            .createOkHttpClient(
                connectTimeoutSeconds = connectTimeoutSeconds,
                readTimeoutSeconds = readTimeoutSeconds,
                writeTimeoutSeconds = writeTimeoutSeconds,
            ).newBuilder()
            .cookieJar(RawCookieJar(store))
            .build()
}
