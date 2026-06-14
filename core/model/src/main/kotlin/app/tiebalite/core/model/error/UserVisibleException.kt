package app.tiebalite.core.model.error

class UserVisibleException(
    val userMessage: String,
    cause: Throwable? = null,
) : RuntimeException(userMessage, cause)

fun Throwable.userVisibleMessageOrNull(): String? =
    when (this) {
        is UserVisibleException -> userMessage
        else -> cause?.userVisibleMessageOrNull()
    }
