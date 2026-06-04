package app.tiebalite.core.network.source.tbclient.recommend

import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientDevice
import app.tiebalite.core.network.client.TbClientIdentity
import app.tiebalite.core.network.proto.recommend.PersonalizedResponseLite
import app.tiebalite.core.network.source.tbclient.ProtoWire
import app.tiebalite.core.network.source.tbclient.TbClientScreen
import app.tiebalite.core.network.source.tbclient.appPosFields
import app.tiebalite.core.network.source.tbclient.buildDataPart
import app.tiebalite.core.network.source.tbclient.buildTbClientCookie
import app.tiebalite.core.network.source.tbclient.buildTextParts
import app.tiebalite.core.network.source.tbclient.commonReqFields
import app.tiebalite.core.network.source.tbclient.stokenParts
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import kotlin.coroutines.cancellation.CancellationException
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

internal interface PersonalizedApi {
    @Multipart
    @POST("c/f/excellent/personalized")
    suspend fun getPersonalizedFeed(
        @Query("cmd") cmd: Int = NetworkDefaults.PERSONALIZED_CMD,
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
        @PartMap formParts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part data: MultipartBody.Part,
    ): ResponseBody
}

data class PersonalizedFeedRaw(
    val body: ByteArray,
    val response: PersonalizedResponseLite,
)

class PersonalizedNetworkSource internal constructor(
    private val api: PersonalizedApi,
    private val device: TbClientDevice = TbClientDevice.current,
    private val identity: TbClientIdentity = TbClientIdentity.default,
) {
    suspend fun fetchFeed(
        loadType: Int = 1,
        page: Int = 1,
        threadCount: Int = 11,
        requestTimes: Int = 1,
        isNewfeed: Int = 1,
        cmd: Int = NetworkDefaults.PERSONALIZED_CMD,
        clientUserToken: String? = null,
        bduss: String? = null,
        stoken: String? = null,
    ): Result<PersonalizedFeedRaw> {
        return try {
            val requestBytes =
                buildPersonalizedRequestBody(
                    identity = identity,
                    device = device,
                    screen = TbClientScreen.current(),
                    timestamp = System.currentTimeMillis(),
                    loadType = loadType,
                    page = page,
                    threadCount = threadCount,
                    requestTimes = requestTimes,
                    isNewfeed = isNewfeed,
                    bduss = bduss,
                    stoken = stoken,
                )
            val formParts = buildTextParts(stokenParts(stoken))

            val responseBytes =
                api.getPersonalizedFeed(
                    cmd = cmd,
                    clientUserToken = clientUserToken,
                    cookie = buildTbClientCookie(identity = identity, device = device),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    formParts = formParts,
                    data = buildDataPart(requestBytes),
                ).bytes()
            val response = PersonalizedResponseLite.parseFrom(responseBytes)
            val errorNo = response.error.errorno
            if (errorNo != 0) {
                val errorMessage =
                    response.error.errmsg.ifBlank {
                        response.error.usermsg
                    }
                throw IllegalStateException("personalized api failed: $errorNo $errorMessage")
            }
            Result.success(
                PersonalizedFeedRaw(
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

private const val From = "1020031h"

internal fun buildPersonalizedRequestBody(
    identity: TbClientIdentity,
    device: TbClientDevice,
    screen: TbClientScreen,
    timestamp: Long,
    loadType: Int,
    page: Int,
    threadCount: Int,
    requestTimes: Int,
    isNewfeed: Int,
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
            ProtoWire.message(36, appPosFields()),
            ProtoWire.message(1, common),
            ProtoWire.string(29, ""),
            ProtoWire.varint(40, isNewfeed),
            ProtoWire.varint(4, loadType.coerceAtLeast(1)),
            ProtoWire.varint(22, 0),
            ProtoWire.varint(3, 0),
            ProtoWire.varint(27, 0),
            ProtoWire.varint(23, 1),
            ProtoWire.varint(5, threadCount.coerceAtLeast(0)),
            ProtoWire.varint(6, page.coerceAtLeast(1)),
            ProtoWire.varint(26, 0),
            ProtoWire.varint(11, 1),
            ProtoWire.varint(28, requestTimes.coerceAtLeast(0)),
            ProtoWire.double(10, screen.density),
            ProtoWire.varint(9, screen.height),
            ProtoWire.varint(8, screen.width),
            ProtoWire.varint(7, 0),
            ProtoWire.varint(2, 0),
        )
    return ProtoWire.encode(listOf(ProtoWire.message(1, data)))
}
