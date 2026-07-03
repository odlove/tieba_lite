package app.tiebalite.core.network.source.tbclient.forum

import app.tiebalite.core.model.error.UserVisibleException
import app.tiebalite.core.network.client.NetworkDefaults
import kotlin.coroutines.cancellation.CancellationException
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

internal interface ForumSignApi {
    @FormUrlEncoded
    @POST("c/c/forum/sign")
    suspend fun signInForum(
        @Field("BDUSS") bduss: String,
        @Field("tbs") tbs: String,
        @Field("fid") forumId: String,
        @Field("kw") forumName: String,
        @Field("authsid") authSid: String? = null,
        @Header("User-Agent") userAgent: String = NetworkDefaults.TBCLIENT_USER_AGENT,
        @Header("Cookie") cookie: String = "ka=open",
    ): ResponseBody
}

class ForumSignNetworkSource internal constructor(
    private val api: ForumSignApi,
) {
    suspend fun signInForum(
        bduss: String,
        tbs: String,
        forumName: String,
        forumId: Long,
        authSid: String? = null,
    ): Result<Unit> =
        request {
            requireForum(forumName = forumName, forumId = forumId)
            require(bduss.isNotBlank()) { "bduss is required" }
            require(tbs.isNotBlank()) { "tbs is required" }
            api.signInForum(
                bduss = bduss,
                tbs = tbs,
                forumName = forumName,
                forumId = forumId.toString(),
                authSid = authSid,
            )
        }

    private suspend fun request(call: suspend () -> ResponseBody): Result<Unit> =
        try {
            val root = JSONObject(call().string())
            val errorCode = root.optString("error_code").toIntOrNull()
                ?: throw UserVisibleException("服务端未确认操作结果")
            if (errorCode != 0) {
                val message =
                    root.optString("usermsg")
                        .ifBlank { root.optString("error_msg") }
                        .ifBlank { "服务端返回错误 $errorCode" }
                throw UserVisibleException(message)
            }
            Result.success(Unit)
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }

    private fun requireForum(
        forumName: String,
        forumId: Long,
    ) {
        require(forumName.isNotBlank()) { "forumName is required" }
        require(forumId > 0L) { "forumId must be positive" }
    }
}
