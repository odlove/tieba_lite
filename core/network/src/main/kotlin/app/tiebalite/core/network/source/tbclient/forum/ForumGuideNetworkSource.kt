package app.tiebalite.core.network.source.tbclient.forum

import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientDevice
import app.tiebalite.core.network.client.TbClientIdentity
import app.tiebalite.core.network.proto.forumguide.ForumGuideResLite
import app.tiebalite.core.network.source.tbclient.ProtoWire
import app.tiebalite.core.network.source.tbclient.buildDataPart
import app.tiebalite.core.network.source.tbclient.buildTbClientCookie
import app.tiebalite.core.network.source.tbclient.buildTextParts
import kotlin.coroutines.cancellation.CancellationException
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

internal interface ForumGuideApi {
    @Multipart
    @POST("c/f/forum/forumGuide")
    suspend fun getForumGuide(
        @Query("cmd") cmd: Int = NetworkDefaults.FORUM_GUIDE_CMD,
        @Query("format") format: String = "protobuf",
        @Header("Charset") charset: String = "UTF-8",
        @Header("client_type") clientType: String = "2",
        @Header("cookie") cookie: String,
        @Header("cuid") cuid: String,
        @Header("cuid_galaxy2") cuidGalaxy2: String,
        @Header("cuid_gid") cuidGid: String = "",
        @Header("c3_aid") c3Aid: String,
        @Header("User-Agent") userAgent: String = NetworkDefaults.TBCLIENT_USER_AGENT,
        @Header("x_bd_data_type") xBdDataType: String = "protobuf",
        @PartMap formParts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part data: MultipartBody.Part,
    ): ResponseBody
}

class ForumGuideNetworkSource internal constructor(
    private val api: ForumGuideApi,
    private val device: TbClientDevice = TbClientDevice.current,
    private val identity: TbClientIdentity = TbClientIdentity.default,
) {
    suspend fun fetchForumGuide(
        bduss: String,
        stoken: String,
        sortType: Int = DEFAULT_SORT_TYPE,
        callFrom: Int = DEFAULT_CALL_FROM,
    ): Result<ForumGuideRaw> {
        return try {
            require(bduss.isNotBlank()) { "bduss is required" }
            require(stoken.isNotBlank()) { "stoken is required" }
            val requestBytes =
                buildForumGuideRequestBody(
                    sortType = sortType,
                    callFrom = callFrom,
                )
            val formParts = buildTextParts(mapOf("BDUSS" to bduss, "stoken" to stoken))
            val responseBytes =
                api.getForumGuide(
                    cookie = buildTbClientCookie(identity = identity, device = device),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    formParts = formParts,
                    data = buildDataPart(requestBytes),
                ).bytes()
            val response = ForumGuideResLite.parseFrom(responseBytes)
            val errorNo = response.error.errorno
            if (errorNo != 0) {
                val errorMessage =
                    response.error.errmsg.ifBlank {
                        response.error.usermsg
                    }
                throw IllegalStateException("forum guide api failed: $errorNo $errorMessage")
            }
            Result.success(
                ForumGuideRaw(
                    body = responseBytes,
                    response = response,
                ),
            )
        } catch (cancellationException: CancellationException) {
            throw cancellationException
        } catch (throwable: Throwable) {
            Result.failure(throwable)
        }
    }

}

data class ForumGuideRaw(
    val body: ByteArray,
    val response: ForumGuideResLite,
)

private const val DEFAULT_SORT_TYPE = 3
private const val DEFAULT_CALL_FROM = 0

internal fun buildForumGuideRequestBody(
    sortType: Int,
    callFrom: Int,
): ByteArray {
    val data =
        listOf(
            ProtoWire.varint(2, sortType),
            ProtoWire.varint(3, callFrom),
        )
    return ProtoWire.encode(listOf(ProtoWire.message(1, data)))
}
