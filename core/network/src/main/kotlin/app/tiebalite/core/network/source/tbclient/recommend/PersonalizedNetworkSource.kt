package app.tiebalite.core.network.source.tbclient.recommend

import android.content.res.Resources
import android.os.Build
import app.tiebalite.core.network.client.NetworkDefaults
import app.tiebalite.core.network.proto.recommend.AppPosInfoLite
import app.tiebalite.core.network.proto.recommend.CommonReqLite
import app.tiebalite.core.network.proto.recommend.PersonalizedRequestDataLite
import app.tiebalite.core.network.proto.recommend.PersonalizedRequestLite
import app.tiebalite.core.network.proto.recommend.PersonalizedResponseLite
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import kotlin.coroutines.cancellation.CancellationException
import kotlin.math.roundToInt
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.PartMap
import retrofit2.http.Query

interface PersonalizedApi {
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

class PersonalizedNetworkSource(
    private val api: PersonalizedApi,
) {
    private val identity = DeviceIdentity.create()

    suspend fun fetchFeed(
        loadType: Int = 1,
        page: Int = 1,
        cmd: Int = NetworkDefaults.PERSONALIZED_CMD,
        clientUserToken: String? = null,
        bduss: String? = null,
        stoken: String? = null,
        tbs: String? = null,
    ): Result<PersonalizedFeedRaw> {
        return try {
            val requestBytes =
                buildRequestBody(
                    loadType = loadType,
                    page = page,
                    bduss = bduss,
                    stoken = stoken,
                    tbs = tbs,
                )
            val formParts = buildFormParts(stoken)
            val dataPart =
                MultipartBody.Part.createFormData(
                    "data",
                    "file",
                    requestBytes.toRequestBody(BinaryMediaType),
                )

            val responseBytes =
                api.getPersonalizedFeed(
                    cmd = cmd,
                    clientUserToken = clientUserToken,
                    cookie = buildCookie(identity.cuid),
                    cuid = identity.cuid,
                    cuidGalaxy2 = identity.cuidGalaxy2,
                    c3Aid = identity.c3Aid,
                    formParts = formParts,
                    data = dataPart,
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

    private fun buildFormParts(
        stoken: String?,
    ): Map<String, RequestBody> {
        val parts = linkedMapOf<String, RequestBody>()
        stoken?.takeIf { it.isNotBlank() }?.let {
            parts["stoken"] = it.toRequestBody(PlainTextMediaType)
        }
        return parts
    }

    private fun buildRequestBody(
        loadType: Int,
        page: Int,
        bduss: String?,
        stoken: String?,
        tbs: String?,
    ): ByteArray {
        val metrics = Resources.getSystem().displayMetrics
        val scrW = metrics.widthPixels.takeIf { it > 0 } ?: DefaultScrW
        val scrH = metrics.heightPixels.takeIf { it > 0 } ?: DefaultScrH
        val scrDip = metrics.density.takeIf { it > 0f }?.toDouble() ?: DefaultScrDip

        val common =
            CommonReqLite.newBuilder()
                .setClientType(2)
                .setClientVersion(NetworkDefaults.TBCLIENT_CLIENT_VERSION)
                .setClientId(identity.clientId)
                .setPhoneImei("")
                .setFrom(From)
                .setCuid(identity.cuid)
                .setTimestamp(System.currentTimeMillis())
                .setModel(Build.MODEL)
                .setBduss(bduss.orEmpty())
                .setTbs(tbs.orEmpty())
                .setNetType(1)
                .setPhoneNewimei("")
                .setKa("open")
                .setStoken(stoken.orEmpty())
                .setCuidGalaxy2(identity.cuidGalaxy2)
                .setCuidGid("")
                .setOaid("")
                .setC3Aid(identity.c3Aid)
                .setScrW(scrW)
                .setScrH(scrH)
                .setScrDip(scrDip)
                .setQType(0)
                .setPersonalizedRecSwitch(1)
                .build()

        val appPos =
            AppPosInfoLite.newBuilder()
                .setApMac("02:00:00:00:00:00")
                .setApConnected(true)
                .setCoordinateType("BD09LL")
                .setAddrTimestamp(0L)
                .setAspShownInfo("")
                .build()

        return PersonalizedRequestLite.newBuilder()
            .setData(
                PersonalizedRequestDataLite.newBuilder()
                    .setCommon(common)
                    .setTagCode(0)
                    .setNeedTags(0)
                    .setLoadType(loadType.coerceAtLeast(1))
                    .setPageThreadCount(11)
                    .setPn(page.coerceAtLeast(1))
                    .setSugCount(0)
                    .setScrW(scrW)
                    .setScrH(scrH)
                    .setScrDip(scrDip)
                    .setQType(1)
                    .setNeedForumlist(0)
                    .setNewNetType(1)
                    .setPreAdThreadCount(0)
                    .setNewInstall(0)
                    .setRequestTimes(0)
                    .setInvokeSource("")
                    .setAppPos(appPos)
                    .build(),
            )
            .build()
            .toByteArray()
    }

    private fun buildCookie(cuid: String): String = "ka=open;CUID=$cuid;TBBRAND=${Build.MODEL};"
}

private val PlainTextMediaType = "text/plain".toMediaType()
private val BinaryMediaType = "application/octet-stream".toMediaType()

private const val From = "1020031h"
private const val DefaultScrW = 1080
private const val DefaultScrH = 2400
private const val DefaultScrDip = 3.0

private data class DeviceIdentity(
    val clientId: String,
    val cuid: String,
    val cuidGalaxy2: String,
    val c3Aid: String,
) {
    companion object {
        fun create(): DeviceIdentity {
            val initTime = System.currentTimeMillis()
            val clientId = "wappc_${initTime}_${(Math.random() * 1000).roundToInt()}"
            val cuid = java.util.UUID.randomUUID().toString().replace("-", "")
            val c3Aid = java.util.UUID.randomUUID().toString().replace("-", "")
            return DeviceIdentity(
                clientId = clientId,
                cuid = cuid,
                cuidGalaxy2 = cuid,
                c3Aid = c3Aid,
            )
        }
    }
}
