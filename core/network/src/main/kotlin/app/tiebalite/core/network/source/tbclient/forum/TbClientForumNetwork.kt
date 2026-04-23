package app.tiebalite.core.network.source.tbclient.forum

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import okhttp3.OkHttpClient
import retrofit2.Retrofit

object TbClientForumNetwork {
    fun createFrsPageNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
    ): FrsPageNetworkSource =
        createFrsPageNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl, okHttpClient = okHttpClient),
        )

    fun createFrsPageNetworkSource(
        retrofit: Retrofit,
    ): FrsPageNetworkSource {
        val api = retrofit.create(FrsPageApi::class.java)
        return FrsPageNetworkSource(api = api)
    }

    fun createForumGuideNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
    ): ForumGuideNetworkSource =
        createForumGuideNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl, okHttpClient = okHttpClient),
        )

    fun createForumGuideNetworkSource(
        retrofit: Retrofit,
    ): ForumGuideNetworkSource {
        val api = retrofit.create(ForumGuideApi::class.java)
        return ForumGuideNetworkSource(api = api)
    }
}
