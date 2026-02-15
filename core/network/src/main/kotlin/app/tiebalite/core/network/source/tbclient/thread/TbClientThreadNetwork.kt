package app.tiebalite.core.network.source.tbclient.thread

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import retrofit2.Retrofit

object TbClientThreadNetwork {
    fun createPbPageNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
    ): PbPageNetworkSource =
        createPbPageNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl),
        )

    fun createPbPageNetworkSource(
        retrofit: Retrofit,
    ): PbPageNetworkSource {
        val api = retrofit.create(PbPageApi::class.java)
        return PbPageNetworkSource(api = api)
    }
}
