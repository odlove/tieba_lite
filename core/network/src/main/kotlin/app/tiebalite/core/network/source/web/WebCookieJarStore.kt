package app.tiebalite.core.network.source.web

interface WebCookieJarStore {
    fun loadRawCookie(): String?

    fun saveRawCookie(rawCookie: String)
}
