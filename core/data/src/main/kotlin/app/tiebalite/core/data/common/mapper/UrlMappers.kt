package app.tiebalite.core.data.common.mapper

internal fun normalizeUrl(raw: String?): String? {
    val value = raw?.trim().orEmpty()
    if (value.isBlank()) {
        return null
    }
    return when {
        value.startsWith("http://") -> "https://${value.removePrefix("http://")}"
        value.startsWith("https://") -> value
        value.startsWith("//") -> "https:$value"
        else -> value
    }
}

internal fun portraitToAvatarUrl(portrait: String?): String? {
    val value = portrait?.trim().orEmpty()
    if (value.isBlank()) {
        return null
    }
    return if (value.startsWith("http://") || value.startsWith("https://")) {
        value
    } else {
        "http://tb.himg.baidu.com/sys/portrait/item/$value"
    }
}
