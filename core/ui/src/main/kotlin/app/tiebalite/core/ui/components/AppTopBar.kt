package app.tiebalite.core.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun AppTopBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: ImageVector? = null,
    onNavigationClick: (() -> Unit)? = null,
) {
    val contentPadding = PaddingValues(0.dp)
    val windowInsets = TopAppBarDefaults.windowInsets

    Surface(color = MaterialTheme.colorScheme.background) {
        Column {
            Layout(
                modifier = modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(windowInsets)
                    .clipToBounds(),
                content = {
                    Box(
                        modifier = Modifier
                            .layoutId("navigationIcon")
                            .padding(start = TopAppBarHorizontalPadding)
                    ) {
                        if (navigationIcon != null && onNavigationClick != null) {
                            IconButton(onClick = onNavigationClick) {
                                Icon(
                                    imageVector = navigationIcon,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onBackground
                                )
                            }
                        }
                    }
                    Box(
                        modifier = Modifier
                            .layoutId("title")
                            .padding(horizontal = TopAppBarHorizontalPadding)
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    Box(
                        modifier = Modifier
                            .layoutId("actionIcons")
                            .padding(end = TopAppBarHorizontalPadding)
                    )
                }
            ) { measurables, constraints ->
                val navigationIconPlaceable =
                    measurables
                        .first { it.layoutId == "navigationIcon" }
                        .measure(constraints.copy(minWidth = 0))
                val actionIconsPlaceable =
                    measurables
                        .first { it.layoutId == "actionIcons" }
                        .measure(constraints.copy(minWidth = 0))
                val startPadding = contentPadding.calculateStartPadding(layoutDirection).roundToPx()
                val endPadding = contentPadding.calculateEndPadding(layoutDirection).roundToPx()
                val titleStartSpace =
                    max(TopAppBarTitleInset.roundToPx(), navigationIconPlaceable.width)
                val maxTitleWidth =
                    if (constraints.maxWidth == Constraints.Infinity) {
                        constraints.maxWidth
                    } else {
                        (constraints.maxWidth -
                                titleStartSpace -
                                actionIconsPlaceable.width -
                                startPadding -
                                endPadding)
                            .coerceAtLeast(0)
                    }
                val titlePlaceable =
                    measurables
                        .first { it.layoutId == "title" }
                        .measure(constraints.copy(minWidth = 0, maxWidth = maxTitleWidth))
                val topPadding = 0
                val bottomPadding = 0
                val maxLayoutHeight =
                    max(TopAppBarHeight.roundToPx(), titlePlaceable.height) + topPadding + bottomPadding
                val layoutHeight =
                    if (constraints.maxHeight == Constraints.Infinity) {
                        maxLayoutHeight
                    } else {
                        maxLayoutHeight.coerceAtLeast(0)
                    }

                layout(constraints.maxWidth, layoutHeight) {
                    val contentHeight = layoutHeight + topPadding - bottomPadding

                    navigationIconPlaceable.placeRelative(
                        x = startPadding,
                        y = (contentHeight - navigationIconPlaceable.height) / 2
                    )

                    val start = max(TopAppBarTitleInset.roundToPx(), navigationIconPlaceable.width)
                    val end = actionIconsPlaceable.width
                    var titleX =
                        Alignment.Start.align(
                            size = titlePlaceable.width,
                            space = constraints.maxWidth,
                            layoutDirection = LayoutDirection.Ltr
                        )

                    if (titleX < start) {
                        titleX += startPadding + (start - titleX)
                    } else if (titleX + titlePlaceable.width > constraints.maxWidth - end) {
                        titleX +=
                            startPadding +
                                ((constraints.maxWidth - end) - (titleX + titlePlaceable.width))
                    }

                    titlePlaceable.placeRelative(
                        x = titleX,
                        y = (contentHeight - titlePlaceable.height) / 2
                    )

                    actionIconsPlaceable.placeRelative(
                        x = constraints.maxWidth - actionIconsPlaceable.width - endPadding,
                        y = (contentHeight - actionIconsPlaceable.height) / 2
                    )
                }
            }
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            )
        }
    }
}

private val TopAppBarHeight = 48.dp
private val TopAppBarHorizontalPadding = 4.dp
private val TopAppBarTitleInset = 16.dp - TopAppBarHorizontalPadding
