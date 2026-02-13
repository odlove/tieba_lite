package app.tiebalite.core.network.source.tbclient.auth

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import kotlin.coroutines.cancellation.CancellationException
import org.json.JSONObject

interface TbClientLoginApi {
    @FormUrlEncoded
    @POST("c/s/login")
    suspend fun login(
        @Field("bdusstoken") bdussToken: String,
        @Field("stoken") stoken: String,
        @Field("user_id") userId: String? = null,
        @Field("channel_id") channelId: String = "",
        @Field("channel_uid") channelUid: String = "",
        @Field("_client_version") clientVersion: String = NetworkDefaults.TBCLIENT_CLIENT_VERSION,
        @Field("authsid") authSid: String = "null",
        @Header("User-Agent") userAgent: String = NetworkDefaults.TBCLIENT_USER_AGENT,
        @Header("Cookie") cookie: String = "ka=open",
    ): ResponseBody
}

object TbClientAuthNetwork {
    fun createLoginNetworkSource(
        baseUrl: String = NetworkDefaults.TBCLIENT_BASE_URL,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
    ): TbClientLoginNetworkSource =
        createLoginNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl, okHttpClient = okHttpClient),
        )

    fun createLoginNetworkSource(
        retrofit: Retrofit,
    ): TbClientLoginNetworkSource {
        val api = retrofit.create(TbClientLoginApi::class.java)
        return TbClientLoginNetworkSource(api = api)
    }
}

class TbClientLoginNetworkSource(
    private val api: TbClientLoginApi,
) {
    suspend fun login(
        bduss: String,
        stoken: String,
    ): Result<TbClientLoginRaw> {
        return try {
            val responseText =
                api.login(
                    bdussToken = "$bduss|null",
                    stoken = stoken,
                ).string()
            val root = JSONObject(responseText)
            val errorCode = root.optString("error_code").toIntOrNull() ?: 0
            if (errorCode != 0) {
                throw IllegalStateException(
                    "tbclient login api failed: $errorCode ${root.optString("error_msg")}",
                )
            }
            val anti =
                root.optJSONObject("anti")
                    ?: throw IllegalStateException("tbclient login api missing anti")
            val user =
                root.optJSONObject("user")
                    ?: throw IllegalStateException("tbclient login api missing user")
            Result.success(
                TbClientLoginRaw(
                    userId = user.optString("id"),
                    userName = user.optString("name"),
                    portrait = user.optString("portrait"),
                    tbs = anti.optString("tbs"),
                ),
            )
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }
}

data class TbClientLoginRaw(
    val userId: String,
    val userName: String,
    val portrait: String,
    val tbs: String,
)
