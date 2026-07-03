package app.tiebalite.core.network.source.tbclient.forum

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientFormInterceptor
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

    private fun createFrsPageNetworkSource(
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

    private fun createForumGuideNetworkSource(
        retrofit: Retrofit,
    ): ForumGuideNetworkSource {
        val api = retrofit.create(ForumGuideApi::class.java)
        return ForumGuideNetworkSource(api = api)
    }

    fun createForumLikeNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
    ): ForumLikeNetworkSource {
        val signedOkHttpClient =
            okHttpClient
                .newBuilder()
                .apply {
                    interceptors().add(0, TbClientFormInterceptor())
                }.build()
        return createForumLikeNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl, okHttpClient = signedOkHttpClient),
        )
    }

    private fun createForumLikeNetworkSource(
        retrofit: Retrofit,
    ): ForumLikeNetworkSource {
        val api = retrofit.create(ForumLikeApi::class.java)
        return ForumLikeNetworkSource(api = api)
    }

    fun createForumSignNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
    ): ForumSignNetworkSource {
        val signedOkHttpClient =
            okHttpClient
                .newBuilder()
                .apply {
                    interceptors().add(0, TbClientFormInterceptor())
                }.build()
        return createForumSignNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl, okHttpClient = signedOkHttpClient),
        )
    }

    private fun createForumSignNetworkSource(
        retrofit: Retrofit,
    ): ForumSignNetworkSource {
        val api = retrofit.create(ForumSignApi::class.java)
        return ForumSignNetworkSource(api = api)
    }
}
