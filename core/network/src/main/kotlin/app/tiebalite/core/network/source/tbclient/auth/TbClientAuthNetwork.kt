package app.tiebalite.core.network.source.tbclient.auth

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientFormInterceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object TbClientAuthNetwork {
    fun createLoginNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
    ): TbClientLoginNetworkSource {
        val signedOkHttpClient =
            okHttpClient
                .newBuilder()
                .apply {
                    interceptors().add(0, TbClientFormInterceptor())
                }
                .build()

        return createLoginNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl, okHttpClient = signedOkHttpClient),
        )
    }

    private fun createLoginNetworkSource(
        retrofit: Retrofit,
    ): TbClientLoginNetworkSource {
        val api = retrofit.create(TbClientLoginApi::class.java)
        return TbClientLoginNetworkSource(api = api)
    }
}
