package app.tiebalite.feature.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.theme.ThemeMode
import app.tiebalite.core.ui.components.AppTopBar
import java.util.Locale

private val presetSeedColors =
    listOf(
        "#0F6B5F",
        "#2F6BFF",
        "#FF8A3D",
        "#5B6770",
    )

@Composable
fun ThemeSettingsScreen(
    paddingValues: PaddingValues,
    state: ThemeSettingsState,
    onEvent: (ThemeSettingsEvent) -> Unit,
    onBack: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val contentPadding =
        PaddingValues(
            start = paddingValues.calculateStartPadding(layoutDirection),
            end = paddingValues.calculateEndPadding(layoutDirection),
            top = 12.dp,
            bottom = paddingValues.calculateBottomPadding() + 24.dp,
        )

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.settings_theme_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = contentPadding,
        ) {
            item {
                ThemeSectionCard(
                    title = stringResource(R.string.settings_theme_mode),
                    subtitle = stringResource(R.string.settings_theme_subtitle),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        ThemeModeButton(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.settings_system),
                            selected = state.themeMode == ThemeMode.System,
                            onClick = { onEvent(ThemeSettingsEvent.SetThemeMode(ThemeMode.System)) },
                        )
                        ThemeModeButton(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.settings_light),
                            selected = state.themeMode == ThemeMode.Light,
                            onClick = { onEvent(ThemeSettingsEvent.SetThemeMode(ThemeMode.Light)) },
                        )
                        ThemeModeButton(
                            modifier = Modifier.weight(1f),
                            label = stringResource(R.string.settings_dark),
                            selected = state.themeMode == ThemeMode.Dark,
                            onClick = { onEvent(ThemeSettingsEvent.SetThemeMode(ThemeMode.Dark)) },
                        )
                    }
                }
            }

            item {
                ThemeToggleCard(
                    title = stringResource(R.string.settings_dynamic_color),
                    subtitle = stringResource(R.string.settings_dynamic_color_desc),
                    checked = state.useDynamicColor,
                    onCheckedChange = { onEvent(ThemeSettingsEvent.SetDynamicColor(it)) },
                )
            }

            item {
                ThemeSectionCard(
                    title = stringResource(R.string.settings_seed_color),
                    subtitle = state.seedColorHex.uppercase(Locale.ROOT),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        presetSeedColors.forEach { colorHex ->
                            ThemeSeedSwatch(
                                modifier = Modifier.weight(1f),
                                hex = colorHex,
                                selected = colorHex.equals(state.seedColorHex, ignoreCase = true),
                                onClick = { onEvent(ThemeSettingsEvent.SetSeedColor(colorHex)) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            content()
        }
    }
}

@Composable
private fun ThemeToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .clickable { onCheckedChange(!checked) }
                    .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
            )
        }
    }
}

@Composable
private fun ThemeModeButton(
    modifier: Modifier = Modifier,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor =
        if (selected) {
            MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }
    val containerColor =
        if (selected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
        } else {
            Color.Transparent
        }
    val textColor =
        if (selected) {
            MaterialTheme.colorScheme.onPrimaryContainer
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant
        }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = BorderStroke(1.dp, borderColor),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
                color = textColor,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun ThemeSeedSwatch(
    modifier: Modifier = Modifier,
    hex: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val swatchColor = hex.toThemeColorOrNull() ?: MaterialTheme.colorScheme.surfaceVariant
    val borderColor =
        if (selected) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.outlineVariant
        }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = swatchColor),
        border = BorderStroke(if (selected) 2.dp else 1.dp, borderColor),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .clickable(onClick = onClick)
                    .padding(10.dp),
            contentAlignment = Alignment.BottomEnd,
        ) {
            if (selected) {
                Box(
                    modifier =
                        Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.96f))
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                                shape = CircleShape,
                            ),
                )
            }
        }
    }
}

private fun String.toThemeColorOrNull(): Color? {
    val cleaned = trim().removePrefix("#")
    if (cleaned.length != 6) {
        return null
    }
    return cleaned.toLongOrNull(16)?.let { Color(0xFF000000 or it) }
}
