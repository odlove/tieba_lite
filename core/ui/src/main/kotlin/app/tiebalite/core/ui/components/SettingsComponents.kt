package app.tiebalite.core.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class SettingsItemStyle {
    Standard,
    WideLeading,
}

private object SettingsItemDefaults {
    val minItemHeight: Dp = 48.dp
    val horizontalPadding: Dp = 16.dp
    val verticalPadding: Dp = 16.dp
    val standardLeadingSlotMinWidth: Dp = 40.dp
    val wideLeadingSlotMinWidth: Dp = 56.dp
    val leadingSlotMinHeight: Dp = 40.dp
    val standardContentSpacing: Dp = 16.dp
    val wideContentSpacing: Dp = 0.dp
    val trailingSpacing: Dp = 24.dp
    val wideLeadingIconContainerSize: Dp = 36.dp
    val leadingIconSize: Dp = 24.dp

    fun leadingSlotMinWidth(style: SettingsItemStyle): Dp =
        when (style) {
            SettingsItemStyle.Standard -> standardLeadingSlotMinWidth
            SettingsItemStyle.WideLeading -> wideLeadingSlotMinWidth
        }

    fun contentSpacing(style: SettingsItemStyle): Dp =
        when (style) {
            SettingsItemStyle.Standard -> standardContentSpacing
            SettingsItemStyle.WideLeading -> wideContentSpacing
        }
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    style: SettingsItemStyle = SettingsItemStyle.Standard,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    val rowModifier =
        modifier
            .fillMaxWidth()
            .let { baseModifier ->
                if (onClick != null) {
                    baseModifier.clickable(
                        interactionSource = interactionSource,
                        indication = indication,
                        enabled = enabled,
                        role = Role.Button,
                        onClick = onClick,
                    )
                } else {
                    baseModifier
                }
            }
            .heightIn(min = SettingsItemDefaults.minItemHeight)
            .padding(
                horizontal = SettingsItemDefaults.horizontalPadding,
                vertical = SettingsItemDefaults.verticalPadding,
            )

    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(SettingsItemDefaults.contentSpacing(style)),
    ) {
        leadingContent?.let {
            Box(
                modifier = Modifier.sizeIn(
                    minWidth = SettingsItemDefaults.leadingSlotMinWidth(style),
                    minHeight = SettingsItemDefaults.leadingSlotMinHeight,
                ),
                contentAlignment = Alignment.CenterStart,
            ) {
                it()
            }
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center,
        ) {
            CompositionLocalProvider(
                androidx.compose.material3.LocalTextStyle provides MaterialTheme.typography.titleMedium,
                androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface,
            ) {
                title()
            }
            subtitle?.let { sub ->
                CompositionLocalProvider(
                    androidx.compose.material3.LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                    androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    sub()
                }
            }
        }

        trailingContent?.let {
            Spacer(modifier = Modifier.width(SettingsItemDefaults.trailingSpacing))
            CompositionLocalProvider(
                androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant,
            ) {
                it()
            }
        }
    }
}

@Composable
fun SettingsItemIcon(
    imageVector: ImageVector,
    contentDescription: String? = null,
    style: SettingsItemStyle = SettingsItemStyle.Standard,
    tint: Color = MaterialTheme.colorScheme.primary,
) {
    when (style) {
        SettingsItemStyle.Standard -> {
            Icon(
                imageVector = imageVector,
                contentDescription = contentDescription,
                modifier = Modifier.size(SettingsItemDefaults.leadingIconSize),
                tint = tint,
            )
        }

        SettingsItemStyle.WideLeading -> {
            Box(
                modifier = Modifier.size(SettingsItemDefaults.wideLeadingIconContainerSize),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = imageVector,
                    contentDescription = contentDescription,
                    modifier = Modifier.size(SettingsItemDefaults.leadingIconSize),
                    tint = tint,
                )
            }
        }
    }
}
