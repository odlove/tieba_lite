package app.tiebalite.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import app.tiebalite.core.ui.components.AppTopBar
import app.tiebalite.core.ui.theme.Spacing
import app.tiebalite.core.ui.theme.ThemeMode
import app.tiebalite.core.ui.theme.toColorOrNull

@Composable
fun ThemeSettingsScreen(
    paddingValues: PaddingValues,
    state: ThemeSettingsState,
    onEvent: (ThemeSettingsEvent) -> Unit,
    onBack: () -> Unit
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding = PaddingValues(
        start = paddingValues.calculateStartPadding(layoutDirection) + Spacing.lg,
        end = paddingValues.calculateEndPadding(layoutDirection) + Spacing.lg,
        top = Spacing.sm,
        bottom = paddingValues.calculateBottomPadding() + Spacing.lg
    )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.settings_theme_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            contentPadding = contentPadding
        ) {
            item {
                SectionTitle(text = stringResource(R.string.settings_theme_mode))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    ModeChip(
                        label = stringResource(R.string.settings_light),
                        selected = state.themeMode == ThemeMode.Light,
                        onClick = { onEvent(ThemeSettingsEvent.SetThemeMode(ThemeMode.Light)) }
                    )
                    ModeChip(
                        label = stringResource(R.string.settings_dark),
                        selected = state.themeMode == ThemeMode.Dark,
                        onClick = { onEvent(ThemeSettingsEvent.SetThemeMode(ThemeMode.Dark)) }
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(top = Spacing.sm),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            item {
                SectionTitle(text = stringResource(R.string.settings_dynamic_color))
                Row(
                    modifier = Modifier.padding(vertical = Spacing.sm),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.settings_dynamic_color_desc),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = state.useDynamicColor,
                        onCheckedChange = { onEvent(ThemeSettingsEvent.SetDynamicColor(it)) }
                    )
                }
                HorizontalDivider(
                    modifier = Modifier.padding(top = Spacing.sm),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }

            item {
                SectionTitle(text = stringResource(R.string.settings_seed_color))
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                    ColorSwatch("#0F6B5F", state.seedColorHex, onEvent)
                    ColorSwatch("#2F6BFF", state.seedColorHex, onEvent)
                    ColorSwatch("#FF8A3D", state.seedColorHex, onEvent)
                    ColorSwatch("#5B6770", state.seedColorHex, onEvent)
                }
                OutlinedTextField(
                    value = state.seedColorHex,
                    onValueChange = { onEvent(ThemeSettingsEvent.SetSeedColor(it)) },
                    label = { Text(text = stringResource(R.string.settings_seed_hint)) },
                    placeholder = { Text(text = "#0F6B5F") },
                    singleLine = true,
                    modifier = Modifier.padding(top = Spacing.sm)
                )
                HorizontalDivider(
                    modifier = Modifier.padding(top = Spacing.md),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
private fun ModeChip(label: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text = label) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
        )
    )
}

@Composable
private fun ColorSwatch(
    hex: String,
    selected: String,
    onEvent: (ThemeSettingsEvent) -> Unit
) {
    val isSelected = hex.equals(selected, ignoreCase = true)
    val borderColor = if (isSelected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.outlineVariant
    }

    Card(
        shape = MaterialTheme.shapes.small,
        colors = CardDefaults.cardColors(
            containerColor = hex.toColorOrNull() ?: MaterialTheme.colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, borderColor),
        modifier = Modifier.clickable { onEvent(ThemeSettingsEvent.SetSeedColor(hex)) }
    ) {
        Column(modifier = Modifier.padding(Spacing.sm)) {
            Text(text = " ", modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
        }
    }
}
