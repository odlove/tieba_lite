package app.tiebalite.core.network.source.tbclient.thread

import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientDevice
import app.tiebalite.core.network.client.TbClientIdentity
import app.tiebalite.core.network.proto.thread.PbFloorResponseLite
import app.tiebalite.core.network.source.tbclient.ProtoWire
import app.tiebalite.core.network.source.tbclient.TbClientScreen
import app.tiebalite.core.network.source.tbclient.buildDataPart
import app.tiebalite.core.network.source.tbclient.buildTbClientCookie
import app.tiebalite.core.network.source.tbclient.buildTextParts
import app.tiebalite.core.network.source.tbclient.commonReqFields
import app.tiebalite.core.network.source.tbclient.stokenParts
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

internal interface PbFloorApi {
    @Multipart
    @POST("c/f/pb/floor")
    suspend fun getPbFloor(
        @Query("cmd") cmd: Int = NetworkDefaults.PB_FLOOR_CMD,
        @Query("format") format: String = "protobuf",
        @Header("Charset") charset: String = "UTF-8",
        @Header("client_type") clientType: String = "2",
        @Header("client_user_token") clientUserToken: String? = null,
        @Header("cookie") cookie: String,
        @Header("cuid") cuid: String,
        @Header("cuid_galaxy2") cuidGalaxy2: String,
        @Header("cuid_gid") cuidGid: String = "",
        @Header("c3_aid") c3Aid: String,
        @Header("User-Agent") userAgent: String = NetworkDefaults.TBCLIENT_USER_AGENT,
        @Header("x_bd_data_type") xBdDataType: String = "protobuf",
        @Header("thread_id") threadId: String,
        @PartMap formParts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part data: MultipartBody.Part,
    ): ResponseBody
}

class PbFloorNetworkSource internal constructor(
    private val api: PbFloorApi,
    private val device: TbClientDevice = TbClientDevice.current,
    private val identity: TbClientIdentity = TbClientIdentity.default,
) {
    suspend fun fetchFloor(
        threadId: Long,
        postId: Long,
        page: Int = 1,
        subPostId: Long = 0L,
        forumId: Long = 0L,
        cmd: Int = NetworkDefaults.PB_FLOOR_CMD,
        clientUserToken: String? = null,
        bduss: String? = null,
        stoken: String? = null,
    ): Result<PbFloorRaw> {
        return try {
            require(threadId > 0L) { "threadId must be positive" }
            require(postId > 0L) { "postId must be positive" }
            val requestBytes =
                buildPbFloorRequestBody(
                    identity = identity,
                    device = device,
                    screen = TbClientScreen.current(),
                    timestamp = System.currentTimeMillis(),
                    threadId = threadId,
                    postId = postId,
                    page = page,
                    subPostId = subPostId,
                    forumId = forumId,
                    isCommReverse = 0,
                    requestTimes = 0,
                    bduss = bduss,
                    stoken = stoken,
                )
            val formParts = buildTextParts(stokenParts(stoken))

            val responseBytes =
                api.getPbFloor(
                    cmd = cmd,
                    clientUserToken = clientUserToken,
                    cookie = buildTbClientCookie(identity = identity, device = device),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    threadId = threadId.toString(),
                    formParts = formParts,
                    data = buildDataPart(requestBytes),
                ).bytes()
            val response = PbFloorResponseLite.parseFrom(responseBytes)
            val errorNo = response.error.errorno
            if (errorNo != 0) {
                val errorMessage =
                    response.error.errmsg.ifBlank {
                        response.error.usermsg
                    }
                throw IllegalStateException("pb floor api failed: $errorNo $errorMessage")
            }
            Result.success(
                PbFloorRaw(
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

data class PbFloorRaw(
    val body: ByteArray,
    val response: PbFloorResponseLite,
)

private const val From = "1020031h"

internal fun buildPbFloorRequestBody(
    identity: TbClientIdentity,
    device: TbClientDevice,
    screen: TbClientScreen,
    timestamp: Long,
    threadId: Long,
    postId: Long,
    page: Int,
    subPostId: Long,
    forumId: Long,
    isCommReverse: Int,
    requestTimes: Int,
    bduss: String?,
    stoken: String?,
): ByteArray {
    val common =
        commonReqFields(
            identity = identity,
            device = device,
            screen = screen,
            timestamp = timestamp,
            from = From,
            qType = 0,
            bduss = bduss,
            stoken = stoken,
            includeApplist = true,
        )
    val data =
        listOf(
            ProtoWire.message(9, common),
            ProtoWire.varint(11, forumId),
            ProtoWire.varint(10, isCommReverse),
            ProtoWire.varint(1, threadId),
            ProtoWire.string(12, ""),
            ProtoWire.string(13, ""),
            ProtoWire.varint(15, 0),
            ProtoWire.string(16, ""),
            ProtoWire.varint(2, postId),
            ProtoWire.varint(4, page.coerceAtLeast(1)),
            ProtoWire.varint(18, requestTimes),
            ProtoWire.double(7, screen.density),
            ProtoWire.varint(6, screen.height),
            ProtoWire.varint(5, screen.width),
            ProtoWire.varint(3, subPostId),
            ProtoWire.string(8, ""),
            ProtoWire.string(17, ""),
        )
    return ProtoWire.encode(listOf(ProtoWire.message(1, data)))
}
