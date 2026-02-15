package app.tiebalite.core.model.auth

data class AuthAccount(
    val accountId: String,
    val session: AuthSession,
    val profile: AuthProfile?,
    val updatedAtMillis: Long,
)
