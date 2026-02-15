package app.tiebalite.core.data.auth.store

import app.tiebalite.core.model.auth.AuthAccount
import app.tiebalite.core.model.auth.AuthProfile
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.proto.auth.AuthAccountProto

internal fun AuthAccountProto.toModel(): AuthAccount =
    AuthAccount(
        accountId = accountId,
        session =
            AuthSession(
                bduss = bduss,
                stoken = stoken,
                tbs = tbs.takeIf { it.isNotBlank() },
            ),
        profile = toProfileOrNull(),
        updatedAtMillis = updatedAtMillis,
    )

internal fun AuthAccountProto.toProfileOrNull(): AuthProfile? {
    val hasProfile =
        userId.isNotBlank() ||
            userName.isNotBlank() ||
            displayName.isNotBlank() ||
            avatarUrl.isNotBlank()
    if (!hasProfile) {
        return null
    }
    return AuthProfile(
        userId = userId,
        userName = userName,
        displayName = displayName,
        avatarUrl = avatarUrl,
    )
}
