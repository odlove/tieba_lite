package app.tiebalite.core.ui.components

import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

object SettingsDefaults {
    val itemPadding: PaddingValues = PaddingValues(horizontal = 16.dp)
    val minItemHeight: Dp = 48.dp
    val iconSlotMinWidth: Dp = 40.dp
    val iconSlotMinHeight: Dp = 40.dp
    val iconTextSpacing: Dp = 16.dp
    val trailingSpacing: Dp = 24.dp
    private val singleLineVerticalPadding: Dp = 16.dp
    private val multiLineVerticalPadding: Dp = 16.dp

    fun topPadding(hasSubtitle: Boolean): Dp =
        if (hasSubtitle) multiLineVerticalPadding else singleLineVerticalPadding

    fun bottomPadding(hasSubtitle: Boolean): Dp =
        if (hasSubtitle) multiLineVerticalPadding else singleLineVerticalPadding
}

@Composable
fun SettingsItem(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    contentPadding: PaddingValues = SettingsDefaults.itemPadding,
    backgroundColor: Color = Color.Transparent,
    shape: Shape = RectangleShape,
    onClick: (() -> Unit)? = null,
    leadingContent: @Composable (() -> Unit)? = null,
    trailingContent: @Composable (() -> Unit)? = null,
    contentSpacing: Dp = SettingsDefaults.iconTextSpacing,
    title: @Composable () -> Unit,
    subtitle: (@Composable () -> Unit)? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(
            interactionSource = interactionSource,
            indication = indication,
            enabled = enabled,
            role = Role.Button,
            onClick = onClick
        )
    } else {
        Modifier
    }

    val titleStyle: TextStyle = MaterialTheme.typography.titleMedium
    val subtitleStyle: TextStyle = MaterialTheme.typography.bodyMedium

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = shape,
        color = backgroundColor,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        val hasSubtitle = subtitle != null
        val startPadding = contentPadding.calculateLeftPadding(LayoutDirection.Ltr)
        val endPadding = contentPadding.calculateRightPadding(LayoutDirection.Ltr)
        Row(
            modifier = clickableModifier
                .fillMaxWidth()
                .heightIn(min = SettingsDefaults.minItemHeight)
                .padding(
                    start = startPadding,
                    end = endPadding,
                    top = SettingsDefaults.topPadding(hasSubtitle),
                    bottom = SettingsDefaults.bottomPadding(hasSubtitle)
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(contentSpacing)
        ) {
            leadingContent?.let {
                Box(
                    modifier = Modifier.sizeIn(
                        minWidth = SettingsDefaults.iconSlotMinWidth,
                        minHeight = SettingsDefaults.iconSlotMinHeight
                    ),
                    contentAlignment = Alignment.CenterStart
                ) {
                    it()
                }
            }
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                CompositionLocalProvider(
                    androidx.compose.material3.LocalTextStyle provides titleStyle,
                    androidx.compose.material3.LocalContentColor provides MaterialTheme.colorScheme.onSurface
                ) {
                    title()
                }
                subtitle?.let { sub ->
                    CompositionLocalProvider(
                        androidx.compose.material3.LocalTextStyle provides subtitleStyle,
                        androidx.compose.material3.LocalContentColor provides
                            MaterialTheme.colorScheme.onSurfaceVariant
                    ) {
                        sub()
                    }
                }
            }
            trailingContent?.let {
                Spacer(modifier = Modifier.width(SettingsDefaults.trailingSpacing))
                CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides
                        MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    it()
                }
            }
        }
    }
}
