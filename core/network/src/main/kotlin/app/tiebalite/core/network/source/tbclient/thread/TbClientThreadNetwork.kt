package app.tiebalite.core.network.source.tbclient.thread

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object TbClientThreadNetwork {
    fun createPbFloorNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
    ): PbFloorNetworkSource =
        createPbFloorNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl, okHttpClient = okHttpClient),
        )

    private fun createPbFloorNetworkSource(
        retrofit: Retrofit,
    ): PbFloorNetworkSource {
        val api = retrofit.create(PbFloorApi::class.java)
        return PbFloorNetworkSource(api = api)
    }

    fun createPbPageNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
    ): PbPageNetworkSource =
        createPbPageNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl, okHttpClient = okHttpClient),
        )

    private fun createPbPageNetworkSource(
        retrofit: Retrofit,
    ): PbPageNetworkSource {
        val api = retrofit.create(PbPageApi::class.java)
        return PbPageNetworkSource(api = api)
    }
}
