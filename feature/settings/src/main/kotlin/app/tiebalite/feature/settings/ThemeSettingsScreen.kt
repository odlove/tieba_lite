package app.tiebalite.feature.settings

import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.tiebalite.core.model.theme.ThemeMode
import app.tiebalite.core.model.theme.ThemeSettings
import app.tiebalite.core.ui.components.AppTopBar

private val presetSeedColors = listOf(
    0xFF0F6B5FL to R.string.settings_seed_teal,
    0xFF2F6BFFL to R.string.settings_seed_blue,
    0xFFFF8A3DL to R.string.settings_seed_orange,
    0xFF5B6770L to R.string.settings_seed_gray,
)

@Composable
fun ThemeSettingsScreen(
    paddingValues: PaddingValues,
    state: ThemeSettings,
    onEvent: (ThemeSettingsEvent) -> Unit,
    onBack: () -> Unit,
) {
    val layoutDirection = LocalLayoutDirection.current
    val supportsDynamicColor = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S

    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            title = stringResource(R.string.settings_theme_title),
            navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
            onNavigationClick = onBack,
        )

        LazyColumn(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentPadding = PaddingValues(
                start = paddingValues.calculateStartPadding(layoutDirection),
                end = paddingValues.calculateEndPadding(layoutDirection),
                top = 12.dp,
                bottom = paddingValues.calculateBottomPadding() + 24.dp,
            ),
        ) {
            item {
                ThemeSection(
                    title = stringResource(R.string.settings_theme_mode),
                    choices = {
                        ThemeTextChoices(
                            labels = ThemeMode.entries.map { stringResource(themeModeLabel(it)) },
                            selectedIndex = state.themeMode.ordinal,
                            onSelect = { onEvent(ThemeSettingsEvent.SetThemeMode(ThemeMode.entries[it])) },
                        )
                    },
                )
            }

            item {
                ThemeSection(
                    title = stringResource(R.string.settings_color_source),
                    choices = {
                        if (supportsDynamicColor) {
                            ThemeTextChoices(
                                labels = listOf(
                                    stringResource(R.string.settings_dynamic_color),
                                    stringResource(R.string.settings_custom_color),
                                ),
                                selectedIndex = if (state.useDynamicColor) 0 else 1,
                                onSelect = { onEvent(ThemeSettingsEvent.SetDynamicColor(it == 0)) },
                            )
                        }
                    },
                ) {
                    if (!supportsDynamicColor || !state.useDynamicColor) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().selectableGroup(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            presetSeedColors.forEach { (seedColor, labelRes) ->
                                ThemeSeedSwatch(
                                    color = Color(seedColor),
                                    label = stringResource(labelRes),
                                    selected = seedColor == state.seedColor,
                                    onClick = { onEvent(ThemeSettingsEvent.SetSeedColor(seedColor)) },
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ThemeSection(
    title: String,
    choices: @Composable () -> Unit,
    content: @Composable () -> Unit = {},
) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(end = 16.dp).semantics { heading() },
            )
            Spacer(modifier = Modifier.weight(1f))
            choices()
        }
        content()
    }
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outlineVariant,
    )
}

@Composable
private fun ThemeTextChoices(labels: List<String>, selectedIndex: Int, onSelect: (Int) -> Unit) {
    FlowRow(
        modifier = Modifier.selectableGroup(),
        horizontalArrangement = Arrangement.End,
    ) {
        labels.forEachIndexed { index, label ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .selectable(
                            selected = index == selectedIndex,
                            interactionSource = null,
                            indication = ripple(bounded = false),
                            role = Role.RadioButton,
                            onClick = { onSelect(index) },
                        )
                        .sizeIn(minWidth = 48.dp, minHeight = 48.dp)
                        .padding(horizontal = 8.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (index == selectedIndex) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (index == selectedIndex) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                    )
                }
                if (index < labels.lastIndex) {
                    Text(
                        text = "/",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun themeModeLabel(mode: ThemeMode): Int = when (mode) {
    ThemeMode.System -> R.string.settings_system
    ThemeMode.Light -> R.string.settings_light
    ThemeMode.Dark -> R.string.settings_dark
}

@Composable
private fun ThemeSeedSwatch(
    color: Color,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .selectable(selected = selected, role = Role.RadioButton, onClick = onClick)
            .semantics { contentDescription = label },
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = if (color.luminance() > 0.179f) Color.Black else Color.White,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}
