package app.tiebalite.core.network.source.tbclient.thread

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import retrofit2.Retrofit

object TbClientThreadNetwork {
    fun createPbFloorNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
    ): PbFloorNetworkSource =
        createPbFloorNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl),
        )

    fun createPbFloorNetworkSource(
        retrofit: Retrofit,
    ): PbFloorNetworkSource {
        val api = retrofit.create(PbFloorApi::class.java)
        return PbFloorNetworkSource(api = api)
    }

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
