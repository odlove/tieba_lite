package app.tiebalite.feature.settings

import android.net.Uri

object SettingsRoutes {
    const val AccountIdArg = "accountId"

    const val Home = "settings/home"
    const val Account = "settings/account"
    const val AccountDetail = "settings/account/detail/{$AccountIdArg}"
    const val Login = "settings/account/login"
    const val CredentialLogin = "settings/account/credential-login"
    const val Theme = "settings/theme"

    fun accountDetail(accountId: String): String =
        "settings/account/detail/${Uri.encode(accountId)}"
}
