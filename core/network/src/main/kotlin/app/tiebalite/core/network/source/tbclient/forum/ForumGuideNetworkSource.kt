package app.tiebalite.core.network.source.tbclient.forum

import android.content.res.Resources
import android.os.Build
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.proto.forumguide.ForumGuideCommonReqLite
import app.tiebalite.core.network.proto.forumguide.ForumGuideReqDataLite
import app.tiebalite.core.network.proto.forumguide.ForumGuideReqLite
import app.tiebalite.core.network.proto.forumguide.ForumGuideResLite
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
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
        @Part data: MultipartBody.Part,
    ): ResponseBody
}

class ForumGuideNetworkSource internal constructor(
    private val api: ForumGuideApi,
) {
    private val identity = DeviceIdentity.create()

    suspend fun fetchForumGuide(
        bduss: String,
        stoken: String,
        sortType: Int = DEFAULT_SORT_TYPE,
        callFrom: Int = DEFAULT_CALL_FROM,
    ): Result<ForumGuideRaw> {
        return try {
            val requestBytes =
                buildRequestBody(
                    bduss = bduss,
                    stoken = stoken,
                    sortType = sortType,
                    callFrom = callFrom,
                )
            val dataPart =
                MultipartBody.Part.createFormData(
                    "data",
                    "file",
                    requestBytes.toRequestBody(BinaryMediaType),
                )
            val responseBytes =
                api.getForumGuide(
                    cookie = buildCookie(identity.cuid),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    data = dataPart,
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

    private fun buildRequestBody(
        bduss: String,
        stoken: String,
        sortType: Int,
        callFrom: Int,
    ): ByteArray {
        val metrics = Resources.getSystem().displayMetrics
        val scrW = metrics.widthPixels.takeIf { it > 0 } ?: DefaultScrW
        val scrH = metrics.heightPixels.takeIf { it > 0 } ?: DefaultScrH
        val scrDip = metrics.density.takeIf { it > 0f }?.toDouble() ?: DefaultScrDip
        val timestamp = System.currentTimeMillis()

        val common =
            ForumGuideCommonReqLite.newBuilder()
                .setClientType(2)
                .setClientVersion(NetworkDefaults.TBCLIENT_CLIENT_VERSION)
                .setClientId(identity.clientId)
                .setPhoneImei("")
                .setFrom(From)
                .setCuid(identity.cuid)
                .setTimestamp(timestamp)
                .setModel(Build.MODEL.orEmpty())
                .setBDUSS(bduss)
                .setNetType(1)
                .setPhoneNewimei("")
                .setPversion("")
                .setOsVersion(Build.VERSION.RELEASE.orEmpty())
                .setBrand(Build.BRAND.orEmpty())
                .setLegoLibVersion("")
                .setStoken(stoken)
                .setCuidGalaxy2(identity.cuidGalaxy2)
                .setCuidGid("")
                .setOaid("")
                .setC3Aid(identity.c3Aid)
                .setScrW(scrW)
                .setScrH(scrH)
                .setScrDip(scrDip)
                .setQType(0)
                .setIsTeenager(0)
                .setSdkVer("")
                .setFrameworkVer("")
                .setNawsGameVer("")
                .setActiveTimestamp(timestamp)
                .setFirstInstallTime(0L)
                .setLastUpdateTime(0L)
                .setEventDay(eventDay(timestamp))
                .setAndroidId("")
                .setCmode(1)
                .setStartScheme("")
                .setStartType(0)
                .setMac("02:00:00:00:00:00")
                .setUserAgent(NetworkDefaults.TBCLIENT_USER_AGENT)
                .setPersonalizedRecSwitch(1)
                .setDeviceScore("")
                .build()

        return ForumGuideReqLite.newBuilder()
            .setData(
                ForumGuideReqDataLite.newBuilder()
                    .setCommon(common)
                    .setSortType(sortType)
                    .setCallFrom(callFrom)
                    .build(),
            ).build()
            .toByteArray()
    }

    private fun buildCookie(cuid: String): String = "ka=open;CUID=$cuid;TBBRAND=${Build.MODEL};"
}

data class ForumGuideRaw(
    val body: ByteArray,
    val response: ForumGuideResLite,
)

private data class DeviceIdentity(
    val clientId: String,
    val cuid: String,
    val cuidGalaxy2: String,
    val c3Aid: String,
) {
    companion object {
        fun create(): DeviceIdentity {
            val initTime = System.currentTimeMillis()
            val cuid = UUID.randomUUID().toString().replace("-", "")
            val c3Aid = UUID.randomUUID().toString().replace("-", "")
            return DeviceIdentity(
                clientId = "wappc_${initTime}_${(Math.random() * 1000).roundToInt()}",
                cuid = cuid,
                cuidGalaxy2 = cuid,
                c3Aid = c3Aid,
            )
        }
    }
}

private fun eventDay(timestamp: Long): String =
    SimpleDateFormat("yyyyMdd", Locale.getDefault()).format(Date(timestamp))

private val BinaryMediaType = "application/octet-stream".toMediaType()

private const val DEFAULT_SORT_TYPE = 3
private const val DEFAULT_CALL_FROM = 4
private const val From = "tieba"
private const val DefaultScrW = 1080
private const val DefaultScrH = 2400
private const val DefaultScrDip = 3.0
