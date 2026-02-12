package app.tiebalite.ui.components

import androidx.compose.animation.graphics.ExperimentalAnimationGraphicsApi
import androidx.compose.animation.graphics.res.animatedVectorResource
import androidx.compose.animation.graphics.res.rememberAnimatedVectorPainter
import androidx.compose.animation.graphics.vector.AnimatedImageVector
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalRippleConfiguration
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.RippleConfiguration
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import app.tiebalite.MainDestination

@OptIn(ExperimentalAnimationGraphicsApi::class)
@Composable
fun TiebaliteBottomBar(
    destinations: Array<MainDestination>,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val itemColors = NavigationBarItemDefaults.colors(
        indicatorColor = Color.Transparent,
        selectedIconColor = MaterialTheme.colorScheme.secondary,
        selectedTextColor = MaterialTheme.colorScheme.secondary,
        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Column(modifier = modifier) {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(
            modifier = Modifier.height(72.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        ) {
            destinations.forEach { destination ->
                val selected = currentRoute == destination.route
                val interactionSource = remember(destination.route) { MutableInteractionSource() }
                CompositionLocalProvider(LocalRippleConfiguration provides null) {
                    NavigationBarItem(
                        selected = selected,
                        onClick = { onNavigate(destination.route) },
                        interactionSource = interactionSource,
                        icon = {
                            val animatedIcon =
                                AnimatedImageVector.animatedVectorResource(destination.iconRes)
                            CompositionLocalProvider(
                                LocalRippleConfiguration provides RippleConfiguration()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .indication(
                                            interactionSource = interactionSource,
                                            indication = ripple(
                                                bounded = false,
                                                radius = 56.dp
                                            )
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = rememberAnimatedVectorPainter(
                                            animatedImageVector = animatedIcon,
                                            atEnd = selected
                                        ),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                        },
                        colors = itemColors
                    )
                }
            }
        }
    }
}
