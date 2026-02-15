package app.tiebalite.core.model.auth

data class AuthProfile(
    val userId: String,
    val userName: String,
    val displayName: String,
    val avatarUrl: String,
)
