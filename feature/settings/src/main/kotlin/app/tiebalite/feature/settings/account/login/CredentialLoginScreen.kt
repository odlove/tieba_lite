package app.tiebalite.feature.settings.account.login

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.auth.AuthSession
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.feature.settings.R

@Composable
fun CredentialLoginScreen(
    paddingValues: PaddingValues,
    onBack: () -> Unit,
    onLoginSuccess: (AuthSession) -> Unit,
) {
    val context = LocalContext.current
    val invalidInputText = stringResource(R.string.settings_credential_login_invalid)
    var bduss by rememberSaveable { mutableStateOf("") }
    var stoken by rememberSaveable { mutableStateOf("") }

    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding()),
    ) {
        AppTopBar(
            title = stringResource(R.string.settings_credential_login_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
        )
        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.settings_credential_login_tip),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            OutlinedTextField(
                value = bduss,
                onValueChange = { bduss = it },
                label = { Text(text = "BDUSS") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = stoken,
                onValueChange = { stoken = it },
                label = { Text(text = "STOKEN") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
            )
            Button(
                onClick = {
                    if (bduss.isBlank() || stoken.isBlank()) {
                        Toast
                            .makeText(
                                context,
                                invalidInputText,
                                Toast.LENGTH_SHORT,
                            ).show()
                        return@Button
                    }
                    onLoginSuccess(
                        AuthSession(
                            bduss = bduss.trim(),
                            stoken = stoken.trim(),
                        ),
                    )
                },
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(text = stringResource(R.string.settings_credential_login_submit))
            }
        }
    }
}
