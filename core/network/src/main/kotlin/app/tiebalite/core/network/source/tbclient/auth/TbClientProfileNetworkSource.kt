package app.tiebalite.core.network.source.tbclient.auth

import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientDevice
import app.tiebalite.core.network.client.TbClientIdentity
import app.tiebalite.core.network.proto.profile.ProfileResponseLite
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

internal interface TbClientProfileApi {
    @Multipart
    @POST("c/u/user/profile")
    suspend fun getProfile(
        @Query("cmd") cmd: Int = NetworkDefaults.PROFILE_CMD,
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

class TbClientProfileNetworkSource internal constructor(
    private val api: TbClientProfileApi,
    private val device: TbClientDevice = TbClientDevice.current,
    private val identity: TbClientIdentity = TbClientIdentity.default,
) {
    suspend fun fetchProfile(
        userId: Long,
        bduss: String? = null,
        stoken: String? = null,
        page: Int = 1,
        postCount: Int = 1,
    ): Result<TbClientProfileRaw> {
        return try {
            require(userId > 0L) { "userId must be positive" }
            val requestBytes =
                buildProfileRequestBody(
                    identity = identity,
                    device = device,
                    screen = TbClientScreen.current(),
                    timestamp = System.currentTimeMillis(),
                    userId = userId,
                    page = page,
                    postCount = postCount,
                    bduss = bduss,
                    stoken = stoken,
                )
            val responseBytes =
                api.getProfile(
                    cookie = buildTbClientCookie(identity = identity, device = device),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    formParts = buildTextParts(stokenParts(stoken)),
                    data = buildDataPart(requestBytes),
                ).bytes()
            val response = ProfileResponseLite.parseFrom(responseBytes)
            val errorNo = response.error.errorno
            if (errorNo != 0) {
                val errorMessage =
                    response.error.errmsg.ifBlank {
                        response.error.usermsg
                    }
                throw IllegalStateException("tbclient profile api failed: $errorNo $errorMessage")
            }
            Result.success(
                TbClientProfileRaw(
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

data class TbClientProfileRaw(
    val body: ByteArray,
    val response: ProfileResponseLite,
)

internal fun buildProfileRequestBody(
    identity: TbClientIdentity,
    device: TbClientDevice,
    screen: TbClientScreen,
    timestamp: Long,
    userId: Long,
    page: Int,
    postCount: Int,
    bduss: String?,
    stoken: String?,
): ByteArray {
    val common =
        commonReqFields(
            identity = identity,
            device = device,
            screen = screen,
            timestamp = timestamp,
            from = "tieba",
            qType = 0,
            bduss = bduss,
            stoken = stoken,
            includeApplist = false,
        )
    val data =
        listOf(
            ProtoWire.varint(1, userId),
            ProtoWire.varint(2, postCount.coerceAtLeast(0)),
            ProtoWire.varint(6, 1),
            ProtoWire.varint(7, 1),
            ProtoWire.varint(8, 1),
            ProtoWire.varint(14, 1),
            ProtoWire.varint(15, page.coerceAtLeast(1)),
            ProtoWire.varint(10, screen.width),
            ProtoWire.varint(11, screen.height),
            ProtoWire.double(13, screen.density),
            ProtoWire.varint(12, 0),
            ProtoWire.string(17, ""),
            ProtoWire.string(18, ""),
            ProtoWire.varint(19, 1),
            ProtoWire.message(9, common),
        )
    return ProtoWire.encode(listOf(ProtoWire.message(1, data)))
}
