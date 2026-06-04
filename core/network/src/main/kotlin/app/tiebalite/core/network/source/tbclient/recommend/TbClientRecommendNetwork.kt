package app.tiebalite.core.network.source.tbclient.recommend

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object TbClientRecommendNetwork {
    fun createPersonalizedNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
    ): PersonalizedNetworkSource =
        createPersonalizedNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl, okHttpClient = okHttpClient),
        )

    private fun createPersonalizedNetworkSource(
        retrofit: Retrofit,
    ): PersonalizedNetworkSource {
        val api = retrofit.create(PersonalizedApi::class.java)
        return PersonalizedNetworkSource(api = api)
    }
}
