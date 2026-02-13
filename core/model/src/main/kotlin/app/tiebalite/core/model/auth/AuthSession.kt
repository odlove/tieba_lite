package app.tiebalite.core.model.auth

data class AuthSession(
    val bduss: String,
    val stoken: String,
) {
    val isValid: Boolean
        get() = bduss.isNotBlank() && stoken.isNotBlank()
}
