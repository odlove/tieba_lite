package app.tiebalite.feature.settings.account.login

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.feature.settings.R

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LoginScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onLoginSuccess: (AuthSession, String) -> Unit,
) {
    val context = LocalContext.current
    var loading by rememberSaveable { mutableStateOf(true) }
    var consumed by rememberSaveable { mutableStateOf(false) }
    var failureShown by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = paddingValues.calculateBottomPadding()),
    ) {
        AppTopBar(
            title = stringResource(R.string.settings_login_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
        )
        if (loading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Text(
            text = stringResource(R.string.settings_login_tip),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp),
        )
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { webContext ->
                WebView(webContext).apply {
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    settings.loadsImagesAutomatically = true
                    val cookieManager = CookieManager.getInstance()
                    cookieManager.setAcceptCookie(true)
                    cookieManager.setAcceptThirdPartyCookies(this, true)
                    webViewClient =
                        object : WebViewClient() {
                            override fun onPageStarted(
                                view: WebView?,
                                url: String?,
                                favicon: android.graphics.Bitmap?,
                            ) {
                                loading = true
                                if (url?.contains("wappass.baidu.com") == true) {
                                    failureShown = false
                                }
                            }

                            override fun onPageFinished(
                                view: WebView?,
                                url: String?,
                            ) {
                                loading = false
                                if (consumed) {
                                    return
                                }
                                val currentUrl = url ?: return
                                if (!isTiebaMineUrl(currentUrl)) {
                                    return
                                }
                                val cookieStr = cookieManager.getCookie(currentUrl) ?: run {
                                    if (!failureShown) {
                                        notifyLoginFailure(context)
                                        failureShown = true
                                    }
                                    return
                                }
                                val session = parseAuthSession(cookieStr) ?: run {
                                    if (!failureShown) {
                                        notifyLoginFailure(context)
                                        failureShown = true
                                    }
                                    return
                                }
                                consumed = true
                                onLoginSuccess(session, cookieStr)
                            }

                            override fun onReceivedError(
                                view: WebView?,
                                request: WebResourceRequest?,
                                error: WebResourceError?,
                            ) {
                                if (request?.isForMainFrame == true) {
                                    loading = false
                                }
                            }
                        }
                    loadUrl(LoginUrl)
                }
            },
        )
    }
}

internal fun parseAuthSession(cookie: String): AuthSession? {
    val cookieMap =
        cookie
            .split(";")
            .map { segment -> segment.trim() }
            .mapNotNull { segment ->
                val separator = segment.indexOf('=')
                if (separator <= 0) {
                    null
                } else {
                    val key = segment.substring(0, separator).trim().uppercase()
                    val value = segment.substring(separator + 1).trim()
                    key to value
                }
            }.toMap()
    val bduss = cookieMap["BDUSS"]?.takeIf { it.isNotBlank() } ?: return null
    val stoken = cookieMap["STOKEN"]?.takeIf { it.isNotBlank() } ?: return null
    return AuthSession(
        bduss = bduss,
        stoken = stoken,
    )
}

private fun notifyLoginFailure(context: Context) {
    Toast
        .makeText(
            context,
            context.getString(R.string.settings_login_failed),
            Toast.LENGTH_SHORT,
        ).show()
}

private const val LoginUrl =
    "https://wappass.baidu.com/passport?login&u=https%3A%2F%2Ftieba.baidu.com%2Findex%2Ftbwise%2Fmine"

private const val TiebaMineUrlPrefix = "https://tieba.baidu.com/index/tbwise/"
private const val TiebaCMineUrlPrefix = "https://tiebac.baidu.com/index/tbwise/"

private fun isTiebaMineUrl(url: String): Boolean =
    url.startsWith(TiebaMineUrlPrefix) || url.startsWith(TiebaCMineUrlPrefix)
