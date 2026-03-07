package app.tiebalite.feature.thread.main.list

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.Velocity

internal data class BottomPullToLoadLatestState(
    val nestedScrollConnection: NestedScrollConnection,
    val pullDistancePx: Float,
    val triggerDistancePx: Float,
    val isReady: Boolean,
)

@Composable
internal fun rememberBottomPullToLoadLatestState(
    listState: LazyListState,
    hasMore: Boolean,
    isRefreshing: Boolean,
    isLoadingMore: Boolean,
    onTriggered: () -> Unit,
): BottomPullToLoadLatestState {
    val onTriggeredState by rememberUpdatedState(onTriggered)
    val triggerDistancePx = with(LocalDensity.current) { BottomPullTriggerDistance.toPx() }
    val pullDistancePxState = remember { mutableFloatStateOf(0f) }
    val isReady by remember(triggerDistancePx) {
        derivedStateOf { pullDistancePxState.floatValue >= triggerDistancePx }
    }

    val connection =
        remember(listState, hasMore, isRefreshing, isLoadingMore, triggerDistancePx) {
            object : NestedScrollConnection {
                private fun canPullToLoadLatest(): Boolean =
                    !hasMore &&
                        !isRefreshing &&
                        !isLoadingMore &&
                        listState.layoutInfo.totalItemsCount > 0 &&
                        !listState.canScrollForward

                private fun resetPullDistance() {
                    if (pullDistancePxState.floatValue != 0f) {
                        pullDistancePxState.floatValue = 0f
                    }
                }

                override fun onPostScroll(
                    consumed: Offset,
                    available: Offset,
                    source: NestedScrollSource,
                ): Offset {
                    if (!canPullToLoadLatest() || source != NestedScrollSource.UserInput) {
                        resetPullDistance()
                        return Offset.Zero
                    }

                    val pullDistancePx = pullDistancePxState.floatValue
                    if (available.y < 0f) {
                        pullDistancePxState.floatValue =
                            (pullDistancePx - available.y)
                                .coerceAtMost(triggerDistancePx * BottomPullMaxDistanceMultiplier)
                    } else if (available.y > 0f && pullDistancePx > 0f) {
                        pullDistancePxState.floatValue = (pullDistancePx - available.y).coerceAtLeast(0f)
                    }
                    return Offset.Zero
                }

                override suspend fun onPreFling(available: Velocity): Velocity {
                    val isTriggerReady = pullDistancePxState.floatValue >= triggerDistancePx
                    if (canPullToLoadLatest() && isTriggerReady) {
                        onTriggeredState()
                    }
                    resetPullDistance()
                    return Velocity.Zero
                }

                override suspend fun onPostFling(
                    consumed: Velocity,
                    available: Velocity,
                ): Velocity {
                    resetPullDistance()
                    return Velocity.Zero
                }
            }
        }

    return BottomPullToLoadLatestState(
        nestedScrollConnection = connection,
        pullDistancePx = pullDistancePxState.floatValue,
        triggerDistancePx = triggerDistancePx,
        isReady = isReady,
    )
}

private val BottomPullTriggerDistance = 72.dp
private const val BottomPullMaxDistanceMultiplier = 1.5f
