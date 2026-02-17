package app.tiebalite.core.ui.format

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatDateTime(seconds: Long?): String? {
    val value = seconds?.takeIf { it > 0L } ?: return null
    return SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.CHINA).format(Date(value * 1000))
}
