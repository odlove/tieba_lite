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

internal interface ForumLikeApi {
    @FormUrlEncoded
    @POST("c/c/forum/like")
    suspend fun likeForum(
        @Field("BDUSS") bduss: String,
        @Field("tbs") tbs: String,
        @Field("kw") forumName: String,
        @Field("fid") forumId: String,
        @Field("forum_name") duplicatedForumName: String,
        @Field("user_id") userId: String,
        @Field("user_name") userName: String,
        @Field("has_head") hasHead: String = "0",
        @Field("st_type") stType: String? = null,
        @Field("authsid") authSid: String? = null,
        @Field("dev_id") devId: String? = null,
        @Field("pagefrom") pageFrom: String? = null,
        @Header("User-Agent") userAgent: String = NetworkDefaults.TBCLIENT_USER_AGENT,
        @Header("Cookie") cookie: String = "ka=open",
    ): ResponseBody

    @FormUrlEncoded
    @POST("c/c/forum/unfavolike")
    suspend fun unlikeForum(
        @Field("BDUSS") bduss: String,
        @Field("tbs") tbs: String,
        @Field("kw") forumName: String,
        @Field("fid") forumId: String,
        @Field("favo_type") favoType: String = "1",
        @Field("st_type") stType: String = DEFAULT_UNLIKE_ST_TYPE,
        @Field("authsid") authSid: String? = null,
        @Header("User-Agent") userAgent: String = NetworkDefaults.TBCLIENT_USER_AGENT,
        @Header("Cookie") cookie: String = "ka=open",
    ): ResponseBody
}

class ForumLikeNetworkSource internal constructor(
    private val api: ForumLikeApi,
) {
    suspend fun followForum(
        bduss: String,
        tbs: String,
        forumName: String,
        forumId: Long,
        userId: String,
        userName: String,
    ): Result<Unit> =
        request {
            requireForum(forumName = forumName, forumId = forumId)
            require(bduss.isNotBlank()) { "bduss is required" }
            require(tbs.isNotBlank()) { "tbs is required" }
            require(userId.isNotBlank()) { "userId is required" }
            require(userName.isNotBlank()) { "userName is required" }
            api.likeForum(
                bduss = bduss,
                tbs = tbs,
                forumName = forumName,
                forumId = forumId.toString(),
                duplicatedForumName = forumName,
                userId = userId,
                userName = userName,
            )
        }

    suspend fun unfollowForum(
        bduss: String,
        tbs: String,
        forumName: String,
        forumId: Long,
    ): Result<Unit> =
        request {
            requireForum(forumName = forumName, forumId = forumId)
            require(bduss.isNotBlank()) { "bduss is required" }
            require(tbs.isNotBlank()) { "tbs is required" }
            api.unlikeForum(
                bduss = bduss,
                tbs = tbs,
                forumName = forumName,
                forumId = forumId.toString(),
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

const val DEFAULT_UNLIKE_ST_TYPE = "bar_detail"
