package app.tiebalite.core.network.source.web

import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl

class RawCookieJar(
    private val store: WebCookieJarStore,
) : CookieJar {
    override fun saveFromResponse(
        url: HttpUrl,
        cookies: List<Cookie>,
    ) {
        if (cookies.isEmpty()) {
            return
        }
        val current = parseRawCookie(store.loadRawCookie())
        cookies.forEach { cookie ->
            current[cookie.name] = cookie.value
        }
        store.saveRawCookie(current.entries.joinToString(separator = "; ") { (key, value) -> "$key=$value" })
    }

    override fun loadForRequest(url: HttpUrl): List<Cookie> =
        parseRawCookie(store.loadRawCookie())
            .map { (name, value) ->
                Cookie
                    .Builder()
                    .name(name)
                    .value(value)
                    .domain(url.host)
                    .path("/")
                    .build()
            }
}

private fun parseRawCookie(rawCookie: String?): LinkedHashMap<String, String> {
    val parsed = linkedMapOf<String, String>()
    val value = rawCookie.orEmpty()
    if (value.isBlank()) {
        return parsed
    }
    value
        .split(";")
        .map { segment -> segment.trim() }
        .forEach { segment ->
            val separator = segment.indexOf('=')
            if (separator <= 0) {
                return@forEach
            }
            val name = segment.substring(0, separator).trim()
            val cookieValue = segment.substring(separator + 1).trim()
            if (name.isNotBlank() && cookieValue.isNotBlank()) {
                parsed[name] = cookieValue
            }
        }
    return parsed
}
