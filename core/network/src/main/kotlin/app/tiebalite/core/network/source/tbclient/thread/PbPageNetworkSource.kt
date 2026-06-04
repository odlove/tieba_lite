package app.tiebalite.core.network.source.tbclient.thread

import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientDevice
import app.tiebalite.core.network.client.TbClientIdentity
import app.tiebalite.core.network.proto.thread.PbListResponseLite
import app.tiebalite.core.network.source.tbclient.ProtoWire
import app.tiebalite.core.network.source.tbclient.TbClientScreen
import app.tiebalite.core.network.source.tbclient.buildDataPart
import app.tiebalite.core.network.source.tbclient.buildTbClientCookie
import app.tiebalite.core.network.source.tbclient.commonReqFields
import kotlin.coroutines.cancellation.CancellationException
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

internal interface PbPageApi {
    @Multipart
    @POST("c/f/pb/page")
    suspend fun getPbPage(
        @Query("cmd") cmd: Int = NetworkDefaults.PB_PAGE_CMD,
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
        @Part data: MultipartBody.Part,
    ): ResponseBody
}

data class PbPageRaw(
    val body: ByteArray,
    val response: PbListResponseLite,
)

class PbPageNetworkSource internal constructor(
    private val api: PbPageApi,
    private val device: TbClientDevice = TbClientDevice.current,
    private val identity: TbClientIdentity = TbClientIdentity.default,
) {
    suspend fun fetchPage(
        threadId: Long,
        page: Int = 1,
        seeLz: Boolean = false,
        sortType: Int = 0,
        lastPostId: Long? = null,
        cmd: Int = NetworkDefaults.PB_PAGE_CMD,
        clientUserToken: String? = null,
        bduss: String? = null,
        stoken: String? = null,
    ): Result<PbPageRaw> {
        return try {
            require(threadId > 0L) { "threadId must be positive" }
            val requestBytes =
                buildPbPageRequestBody(
                    identity = identity,
                    device = device,
                    screen = TbClientScreen.current(),
                    timestamp = System.currentTimeMillis(),
                    threadId = threadId,
                    page = page,
                    seeLz = seeLz,
                    sortType = sortType,
                    lastPostId = lastPostId,
                    bduss = bduss,
                    stoken = stoken,
                )

            val responseBytes =
                api.getPbPage(
                    cmd = cmd,
                    clientUserToken = clientUserToken,
                    cookie = buildTbClientCookie(identity = identity, device = device),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    threadId = threadId.toString(),
                    data = buildDataPart(requestBytes),
                ).bytes()
            val response = PbListResponseLite.parseFrom(responseBytes)
            val errorNo = response.error.errorno
            if (errorNo != 0) {
                val errorMessage =
                    response.error.errmsg.ifBlank {
                        response.error.usermsg
                    }
                throw IllegalStateException("pb page api failed: $errorNo $errorMessage")
            }
            Result.success(
                PbPageRaw(
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

internal fun buildPbPageRequestBody(
    identity: TbClientIdentity,
    device: TbClientDevice,
    screen: TbClientScreen,
    timestamp: Long,
    threadId: Long,
    page: Int,
    seeLz: Boolean,
    sortType: Int,
    lastPostId: Long?,
    bduss: String?,
    stoken: String?,
): ByteArray {
    val latestPostAnchor = lastPostId?.takeIf { value -> value > 0L }
    val common =
        commonReqFields(
            identity = identity,
            device = device,
            screen = screen,
            timestamp = timestamp,
            from = "tieba",
            qType = 2,
            bduss = bduss,
            stoken = stoken,
            includeApplist = true,
        )
    val adParam =
        listOf(
            ProtoWire.varint(4, 1),
            ProtoWire.varint(1, 0),
            ProtoWire.varint(2, 0),
            ProtoWire.string(3, ""),
        )
    val data =
        buildList {
            add(ProtoWire.message(18, adParam))
            add(ProtoWire.varint(6, 0))
            add(ProtoWire.message(1, common))
            add(ProtoWire.varint(2, threadId))
            latestPostAnchor?.let { value ->
                add(ProtoWire.varint(4, value))
            }
            if (seeLz) {
                add(ProtoWire.varint(7, 1))
            }
            add(ProtoWire.varint(8, if (latestPostAnchor != null) 2 else 0))
            add(ProtoWire.varint(3, page.coerceAtLeast(1)))
            add(ProtoWire.varint(5, sortType))
            add(ProtoWire.varint(36, 1))
        }
    return ProtoWire.encode(listOf(ProtoWire.message(1, data)))
}
