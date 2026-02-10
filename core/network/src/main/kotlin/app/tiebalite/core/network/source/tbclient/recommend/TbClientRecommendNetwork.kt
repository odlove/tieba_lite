package app.tiebalite.core.network.source.tbclient.recommend

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import retrofit2.Retrofit

object TbClientRecommendNetwork {
    fun createPersonalizedNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
    ): PersonalizedNetworkSource =
        createPersonalizedNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl),
        )

    fun createPersonalizedNetworkSource(
        retrofit: Retrofit,
    ): PersonalizedNetworkSource {
        val api = retrofit.create(PersonalizedApi::class.java)
        return PersonalizedNetworkSource(api = api)
    }
}
