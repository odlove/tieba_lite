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
import org.json.JSONArray
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
            val raw = root.toRawMap()
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
                    body = responseText,
                    raw = raw,
                    errorCode = errorCode,
                    errorMessage = root.optString("error_msg"),
                    ctime = root.optLong("ctime").takeIf { root.has("ctime") },
                    logId = root.optLong("logid").takeIf { root.has("logid") },
                    serverTime = root.optString("server_time").takeIf { it.isNotBlank() },
                    time = root.optLong("time").takeIf { root.has("time") },
                    anti =
                        TbClientLoginRaw.Anti(
                            tbs = anti.optString("tbs"),
                            raw = anti.toRawMap(),
                        ),
                    user =
                        TbClientLoginRaw.User(
                            id = user.optString("id"),
                            name = user.optString("name"),
                            portrait = user.optString("portrait"),
                            raw = user.toRawMap(),
                        ),
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
    val body: String,
    val raw: Map<String, Any?>,
    val errorCode: Int,
    val errorMessage: String?,
    val ctime: Long?,
    val logId: Long?,
    val serverTime: String?,
    val time: Long?,
    val anti: Anti,
    val user: User,
) {
    data class Anti(
        val tbs: String,
        val raw: Map<String, Any?>,
    )

    data class User(
        val id: String,
        val name: String,
        val portrait: String,
        val raw: Map<String, Any?>,
    )
}

private fun JSONObject.toRawMap(): Map<String, Any?> =
    keys().asSequence().associateWith { key ->
        opt(key).toRawValue()
    }

private fun JSONArray.toRawList(): List<Any?> =
    List(length()) { index ->
        opt(index).toRawValue()
    }

private fun Any?.toRawValue(): Any? =
    when (this) {
        null,
        JSONObject.NULL,
        -> null
        is JSONObject -> toRawMap()
        is JSONArray -> toRawList()
        else -> this
    }
