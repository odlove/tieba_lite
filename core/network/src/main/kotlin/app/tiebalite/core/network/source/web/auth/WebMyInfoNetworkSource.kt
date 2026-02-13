package app.tiebalite.core.network.source.web.auth

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import kotlin.coroutines.cancellation.CancellationException
import org.json.JSONObject

interface WebMyInfoApi {
    @GET("/mo/q/newmoindex?need_user=1")
    suspend fun getMyInfo(
        @Header("cookie") cookie: String? = null,
    ): ResponseBody
}

object WebAuthNetwork {
    fun createMyInfoNetworkSource(
        baseUrl: String = NetworkDefaults.BASE_URL,
        okHttpClient: OkHttpClient = NetworkClientFactory.createOkHttpClient(),
    ): WebMyInfoNetworkSource =
        createMyInfoNetworkSource(
            retrofit = NetworkClientFactory.createRetrofit(baseUrl = baseUrl, okHttpClient = okHttpClient),
        )

    fun createMyInfoNetworkSource(
        retrofit: Retrofit,
    ): WebMyInfoNetworkSource {
        val api = retrofit.create(WebMyInfoApi::class.java)
        return WebMyInfoNetworkSource(api = api)
    }
}

class WebMyInfoNetworkSource(
    private val api: WebMyInfoApi,
) {
    suspend fun fetchMyInfo(cookie: String? = null): Result<WebMyInfoRaw> {
        return try {
            val responseText = api.getMyInfo(cookie = cookie).string()
            val root = JSONObject(responseText)
            val errorCode = root.optInt("no", 0)
            if (errorCode != 0) {
                throw IllegalStateException(
                    "web myInfo api failed: $errorCode ${root.optString("error")}",
                )
            }
            val data =
                root.optJSONObject("data")
                    ?: throw IllegalStateException("web myInfo api missing data")
            if (!data.optBoolean("is_login", true)) {
                throw IllegalStateException("web myInfo api returned not logged in")
            }
            Result.success(
                WebMyInfoRaw(
                    uid = data.optLong("uid", 0L),
                    name = data.optString("name"),
                    showName = data.optString("name_show"),
                    avatarUrl = data.optString("portrait_url"),
                    tbs =
                        data.optString("tbs")
                            .ifBlank { data.optString("itb_tbs") },
                ),
            )
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }
}

data class WebMyInfoRaw(
    val uid: Long,
    val name: String,
    val showName: String,
    val avatarUrl: String,
    val tbs: String,
)
