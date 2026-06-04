package app.tiebalite.core.data.auth.service

import app.tiebalite.core.data.common.mapper.portraitToAvatarUrl
import app.tiebalite.core.model.auth.AuthProfile
import app.tiebalite.core.network.source.tbclient.auth.TbClientLoginRaw
import app.tiebalite.core.network.source.tbclient.auth.TbClientProfileRaw
import app.tiebalite.core.network.source.web.auth.WebMyInfoRaw

internal fun TbClientLoginRaw.toAuthProfile(): AuthProfile =
    AuthProfile(
        userId = user.id,
        userName = user.name,
        displayName = user.name,
        avatarUrl = portraitToAvatarUrl(user.portrait).orEmpty(),
    )

internal fun WebMyInfoRaw.toAuthProfile(): AuthProfile =
    AuthProfile(
        userId = (data.uid ?: data.id ?: 0L).takeIf { it > 0L }?.toString().orEmpty(),
        userName = data.name,
        displayName = data.showName.ifBlank { data.name },
        avatarUrl =
            portraitToAvatarUrl(
                data.portraitUrl
                    .trim()
                    .ifBlank { data.portrait.trim() },
            ).orEmpty(),
    )

internal fun TbClientProfileRaw.toAuthProfile(): AuthProfile =
    response.data.user.let { user ->
        AuthProfile(
            userId = user.id.takeIf { it > 0L }?.toString().orEmpty(),
            userName = user.name,
            displayName = user.nameShow.ifBlank { user.name },
            avatarUrl = portraitToAvatarUrl(user.portrait).orEmpty(),
        )
    }

internal fun TbClientLoginRaw.toProfilePayload(): AuthProfilePayload =
    AuthProfilePayload(
        profile = toAuthProfile(),
        tbs = anti.tbs.takeIf { it.isNotBlank() },
    )

internal fun TbClientProfileRaw.toProfilePayload(fallbackTbs: String? = null): AuthProfilePayload =
    AuthProfilePayload(
        profile = toAuthProfile(),
        tbs = response.data.antiStat.tbs.takeIf { it.isNotBlank() } ?: fallbackTbs?.takeIf { it.isNotBlank() },
    )

internal fun WebMyInfoRaw.toProfilePayload(): AuthProfilePayload =
    AuthProfilePayload(
        profile = toAuthProfile(),
        tbs =
            data.tbs
                .ifBlank { data.itbTbs }
                .takeIf { it.isNotBlank() },
    )

internal data class AuthProfilePayload(
    val profile: AuthProfile,
    val tbs: String?,
)
