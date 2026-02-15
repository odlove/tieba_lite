package app.tiebalite.core.network.source.web.auth

import app.tiebalite.core.network.client.NetworkClientFactory
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.source.web.WebCookieJarStore
import app.tiebalite.core.network.source.web.WebOkHttpClientProvider
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Header
import kotlin.coroutines.cancellation.CancellationException
import org.json.JSONArray
import org.json.JSONObject

interface WebMyInfoApi {
    @GET("/mo/q/newmoindex?need_user=1")
    suspend fun getMyInfo(
        @Header("cookie") cookie: String? = null,
    ): ResponseBody
}

object WebAuthNetwork {
    fun createMyInfoNetworkSource(
        store: WebCookieJarStore,
        baseUrl: String = NetworkDefaults.BASE_URL,
    ): WebMyInfoNetworkSource {
        val okHttpClient = WebOkHttpClientProvider.create(store = store)
        return createMyInfoNetworkSource(baseUrl = baseUrl, okHttpClient = okHttpClient)
    }

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
            val raw = root.toRawMap()
            val errorCode = root.optInt("no", 0)
            if (errorCode != 0) {
                throw IllegalStateException(
                    "web myInfo api failed: $errorCode ${root.optString("error")}",
                )
            }
            val data =
                root.optJSONObject("data")
                    ?: throw IllegalStateException("web myInfo api missing data")
            val dataRaw = data.toRawMap()
            val isLogin = data.optBoolean("is_login", true)
            if (!isLogin) {
                throw IllegalStateException("web myInfo api returned not logged in")
            }
            Result.success(
                WebMyInfoRaw(
                    body = responseText,
                    raw = raw,
                    no = errorCode,
                    error = root.optString("error").takeIf { it.isNotBlank() },
                    data =
                        WebMyInfoRaw.Data(
                            raw = dataRaw,
                            isLogin = isLogin,
                            uid = data.optLong("uid").takeIf { data.has("uid") },
                            id = data.optLong("id").takeIf { data.has("id") },
                            name = data.optString("name"),
                            showName = data.optString("name_show"),
                            portrait = data.optString("portrait"),
                            portraitUrl = data.optString("portrait_url"),
                            tbs = data.optString("tbs"),
                            itbTbs = data.optString("itb_tbs"),
                            postNum = data.optInt("post_num").takeIf { data.has("post_num") },
                            fansNum = data.optInt("fans_num").takeIf { data.has("fans_num") },
                            concernNum = data.optInt("concern_num").takeIf { data.has("concern_num") },
                            likeForumNum = data.optInt("like_forum_num").takeIf { data.has("like_forum_num") },
                            intro = data.optString("intro"),
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

data class WebMyInfoRaw(
    val body: String,
    val raw: Map<String, Any?>,
    val no: Int,
    val error: String?,
    val data: Data,
) {
    data class Data(
        val raw: Map<String, Any?>,
        val isLogin: Boolean,
        val uid: Long?,
        val id: Long?,
        val name: String,
        val showName: String,
        val portrait: String,
        val portraitUrl: String,
        val tbs: String,
        val itbTbs: String,
        val postNum: Int?,
        val fansNum: Int?,
        val concernNum: Int?,
        val likeForumNum: Int?,
        val intro: String,
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
