package app.tiebalite.feature.thread.common.post

import app.tiebalite.core.ui.format.formatDateTime

internal fun formatPostMeta(
    seconds: Long?,
    ipLocation: String?,
): String {
    val values =
        listOfNotNull(
            formatDateTime(seconds),
            ipLocation?.trim()?.takeIf { it.isNotEmpty() },
        )
    return values.joinToString(" · ").ifBlank { "未知时间" }
}
