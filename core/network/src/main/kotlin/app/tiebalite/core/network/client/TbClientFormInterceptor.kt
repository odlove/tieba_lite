package app.tiebalite.core.network.client

import okhttp3.FormBody
import okhttp3.Interceptor
import okhttp3.Response
import java.security.MessageDigest

class TbClientFormInterceptor(
    private val clientIdProvider: () -> String = { TbClientIdentity.default.clientId },
    private val timestampProvider: () -> Long = { System.currentTimeMillis() },
    private val modelProvider: () -> String = { TbClientDevice.current.model },
    private val osVersionProvider: () -> String = { TbClientDevice.current.osVersion },
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val body = request.body
        if (body !is FormBody || hasFormName(body, SIGN_PARAM)) {
            return chain.proceed(request)
        }

        val formBody =
            buildSignedFormBody(
                body = body,
                clientId = clientIdProvider(),
                timestampMillis = timestampProvider(),
                model = modelProvider(),
                osVersion = osVersionProvider(),
            )

        return chain.proceed(
            request
                .newBuilder()
                .method(request.method, formBody)
                .build(),
        )
    }

    companion object {
        private const val SIGN_PARAM = "sign"
        private const val SIGN_SALT = "tiebaclient!!!"

        internal fun buildSignedFormBody(
            body: FormBody,
            clientId: String,
            timestampMillis: Long,
            model: String,
            osVersion: String,
        ): FormBody {
            val params = linkedMapOf<String, String>()
            repeat(body.size) { index ->
                params[body.name(index)] = body.value(index)
            }
            putIfAbsent(params, "_client_id", clientId)
            putIfAbsent(params, "_client_type", "2")
            putIfAbsent(params, "_client_version", NetworkDefaults.TBCLIENT_CLIENT_VERSION)
            putIfAbsent(params, "_os_version", osVersion)
            putIfAbsent(params, "_timestamp", timestampMillis.toString())
            putIfAbsent(params, "model", model)
            putIfAbsent(params, "net_type", "1")
            putIfAbsent(params, "_phone_imei", "")
            putIfAbsent(params, "stErrorNums", "0")

            params[SIGN_PARAM] = calculateSign(params)

            return FormBody.Builder()
                .apply {
                    params.toSortedMap().forEach { (name, value) ->
                        add(name, value)
                    }
                }.build()
        }

        internal fun calculateSign(params: Map<String, String>): String {
            val sortedRaw =
                params
                    .filterKeys { key -> key != SIGN_PARAM }
                    .toSortedMap()
                    .entries
                    .joinToString(separator = "") { (key, value) -> "$key=$value" }
            return md5(sortedRaw + SIGN_SALT)
        }

        private fun putIfAbsent(
            params: MutableMap<String, String>,
            key: String,
            value: String,
        ) {
            if (!params.containsKey(key)) {
                params[key] = value
            }
        }

        private fun hasFormName(
            body: FormBody,
            name: String,
        ): Boolean {
            repeat(body.size) { index ->
                if (body.name(index) == name) {
                    return true
                }
            }
            return false
        }

        private fun md5(value: String): String {
            val digest = MessageDigest.getInstance("MD5").digest(value.toByteArray())
            return digest.joinToString(separator = "") { byte -> "%02X".format(byte) }
        }
    }
}
