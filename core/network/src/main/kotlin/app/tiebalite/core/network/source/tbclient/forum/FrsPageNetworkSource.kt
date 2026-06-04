package app.tiebalite.core.network.source.tbclient.forum

import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.client.TbClientDevice
import app.tiebalite.core.network.client.TbClientIdentity
import app.tiebalite.core.network.proto.frs.FrsPageResponseLite
import app.tiebalite.core.network.source.tbclient.ProtoWire
import app.tiebalite.core.network.source.tbclient.TbClientScreen
import app.tiebalite.core.network.source.tbclient.appPosFields
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

internal interface FrsPageApi {
    @Multipart
    @POST("c/f/frs/page")
    suspend fun getFrsPage(
        @Query("cmd") cmd: Int = NetworkDefaults.FRS_PAGE_CMD,
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
        @Header("forum_name") forumName: String? = null,
        @PartMap formParts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part data: MultipartBody.Part,
    ): ResponseBody
}

data class FrsPageRaw(
    val body: ByteArray,
    val response: FrsPageResponseLite,
)

class FrsPageNetworkSource internal constructor(
    private val api: FrsPageApi,
    private val device: TbClientDevice = TbClientDevice.current,
    private val identity: TbClientIdentity = TbClientIdentity.default,
) {
    suspend fun fetchPage(
        forumName: String,
        page: Int = 1,
        loadType: Int = 1,
        sortType: Int = DEFAULT_SORT_TYPE,
        goodClassifyId: Int? = null,
        clientUserToken: String? = null,
        bduss: String? = null,
        stoken: String? = null,
    ): Result<FrsPageRaw> {
        return try {
            require(forumName.isNotBlank()) { "forumName is required" }
            val requestBytes =
                buildFrsPageRequestBody(
                    identity = identity,
                    device = device,
                    screen = TbClientScreen.current(),
                    timestamp = System.currentTimeMillis(),
                    forumName = forumName,
                    page = page,
                    loadType = loadType,
                    sortType = sortType,
                    goodClassifyId = goodClassifyId,
                    bduss = bduss,
                    stoken = stoken,
                )
            val formParts = buildTextParts(stokenParts(stoken))

            val encodedForumName = percentEncodeUtf8(forumName)
            val responseBytes =
                api.getFrsPage(
                    clientUserToken = clientUserToken,
                    cookie = buildTbClientCookie(identity = identity, device = device),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    forumName = encodedForumName,
                    formParts = formParts,
                    data = buildDataPart(requestBytes),
                ).bytes()
            val response = FrsPageResponseLite.parseFrom(responseBytes)
            val errorNo = response.error.errorno
            if (errorNo != 0) {
                val errorMessage =
                    response.error.errmsg.ifBlank {
                        response.error.usermsg
                    }
                throw IllegalStateException("frs page api failed: $errorNo $errorMessage")
            }
            Result.success(
                FrsPageRaw(
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
private const val DEFAULT_SORT_TYPE = -1

internal fun buildFrsPageRequestBody(
    identity: TbClientIdentity,
    device: TbClientDevice,
    screen: TbClientScreen,
    timestamp: Long,
    forumName: String,
    page: Int,
    loadType: Int,
    sortType: Int,
    goodClassifyId: Int?,
    bduss: String?,
    stoken: String?,
): ByteArray {
    val normalizedLoadType = loadType.coerceAtLeast(1)
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
    val adParam =
        listOf(
            ProtoWire.varint(1, 0),
            ProtoWire.varint(2, 0),
            ProtoWire.string(3, ""),
        )
    val data =
        listOf(
            ProtoWire.string(1, percentEncodeUtf8(forumName)),
            ProtoWire.varint(15, page.coerceAtLeast(1)),
            ProtoWire.varint(2, 90),
            ProtoWire.varint(3, 30),
            ProtoWire.varint(8, 1),
            ProtoWire.varint(4, if (goodClassifyId != null) 1 else 0),
            ProtoWire.varint(5, goodClassifyId ?: 0),
            ProtoWire.varint(11, screen.width),
            ProtoWire.varint(12, screen.height),
            ProtoWire.double(13, screen.density),
            ProtoWire.string(16, ""),
            ProtoWire.varint(14, 2),
            ProtoWire.varint(27, 0),
            ProtoWire.varint(17, 0),
            ProtoWire.varint(18, 0),
            ProtoWire.varint(19, 0),
            ProtoWire.string(40, ""),
            ProtoWire.varint(44, 0),
            ProtoWire.varint(69, 1),
            ProtoWire.string(45, ""),
            ProtoWire.varint(47, sortType),
            ProtoWire.varint(87, 0),
            ProtoWire.varint(48, 0L),
            ProtoWire.message(50, appPosFields()),
            ProtoWire.varint(49, normalizedLoadType),
            ProtoWire.string(52, "2"),
            ProtoWire.string(53, "-2"),
            ProtoWire.varint(55, 0),
            ProtoWire.varint(56, 0),
            ProtoWire.varint(58, 0L),
            ProtoWire.string(67, ""),
            ProtoWire.string(65, ""),
            ProtoWire.string(78, ""),
            ProtoWire.double(68, 0.0),
            ProtoWire.varint(66, 0),
            ProtoWire.message(51, adParam),
            ProtoWire.varint(59, 1),
            ProtoWire.string(60, ""),
            ProtoWire.string(61, ""),
            ProtoWire.varint(63, 0),
            ProtoWire.string(62, buildAdExtParams(loadType = normalizedLoadType)),
            ProtoWire.message(64, buildAppTransmitDataFields()),
            ProtoWire.varint(70, 0L),
            ProtoWire.message(39, common),
            ProtoWire.string(72, ""),
            ProtoWire.varint(73, 1),
            ProtoWire.string(74, ""),
            ProtoWire.string(75, ""),
            ProtoWire.string(76, "%7B%7D"),
            ProtoWire.varint(91, 0),
            ProtoWire.varint(92, 0),
            ProtoWire.varint(84, 0),
            ProtoWire.varint(80, 0L),
            ProtoWire.varint(88, 0),
            ProtoWire.string(86, ""),
            ProtoWire.string(82, ""),
            ProtoWire.varint(89, 0),
            ProtoWire.varint(90, 0),
        )
    return ProtoWire.encode(listOf(ProtoWire.message(1, data)))
}

private fun buildAdExtParams(loadType: Int): String {
    val reqType = if (loadType != 1) 1 else 0
    return """{"iadex":"","nad_core_version":"6.39.0.5","floor_info":"","req_type":$reqType,"pre_ad_thread_count":0}"""
}

private fun buildAppTransmitDataFields(): List<ProtoWire.Field> =
    listOf(
        ProtoWire.string(2, ""),
        ProtoWire.string(3, ""),
        ProtoWire.string(4, ""),
        ProtoWire.varint(6, 0),
        ProtoWire.string(15, "Asia/Shanghai"),
    )

private fun percentEncodeUtf8(value: String): String {
    val builder = StringBuilder()
    value.toByteArray(Charsets.UTF_8).forEach { rawByte ->
        val byte = rawByte.toInt() and 0xff
        val char = byte.toChar()
        if (char in 'A'..'Z' || char in 'a'..'z' || char in '0'..'9' || char == '-' || char == '_' || char == '.' || char == '~') {
            builder.append(char)
        } else {
            builder.append('%')
            builder.append(UpperHexChars[byte ushr 4])
            builder.append(UpperHexChars[byte and 0x0f])
        }
    }
    return builder.toString()
}

private const val UpperHexChars = "0123456789ABCDEF"
