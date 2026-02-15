package app.tiebalite.core.data.auth.service

import app.tiebalite.core.model.auth.AuthProfile
import app.tiebalite.core.network.source.tbclient.auth.TbClientLoginRaw
import app.tiebalite.core.network.source.web.auth.WebMyInfoRaw

internal fun TbClientLoginRaw.toAuthProfile(): AuthProfile =
    AuthProfile(
        userId = user.id,
        userName = user.name,
        displayName = user.name,
        avatarUrl =
            user.portrait
                .trim()
                .let { portrait ->
                    when {
                        portrait.startsWith("http://") || portrait.startsWith("https://") -> portrait
                        portrait.isBlank() -> ""
                        else -> "http://tb.himg.baidu.com/sys/portrait/item/$portrait"
                    }
                },
    )

internal fun WebMyInfoRaw.toAuthProfile(): AuthProfile =
    AuthProfile(
        userId = (data.uid ?: data.id ?: 0L).takeIf { it > 0L }?.toString().orEmpty(),
        userName = data.name,
        displayName = data.showName.ifBlank { data.name },
        avatarUrl =
            data.portraitUrl
                .trim()
                .ifBlank { data.portrait.trim() }
                .let { portrait ->
                    when {
                        portrait.startsWith("http://") || portrait.startsWith("https://") -> portrait
                        portrait.isBlank() -> ""
                        else -> "http://tb.himg.baidu.com/sys/portrait/item/$portrait"
                    }
                },
    )

internal fun TbClientLoginRaw.toProfilePayload(): AuthProfilePayload =
    AuthProfilePayload(
        profile = toAuthProfile(),
        tbs = anti.tbs.takeIf { it.isNotBlank() },
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
