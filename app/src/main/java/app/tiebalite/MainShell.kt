package app.tiebalite

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.feature.explore.ExploreRoute
import app.tiebalite.feature.messages.MessagesScreen
import app.tiebalite.feature.myforums.MyForumsRoute
import app.tiebalite.feature.profile.ProfileScreen
import app.tiebalite.ui.components.TiebaliteBottomBar

@Composable
internal fun MainShell(
    rootPaddingValues: PaddingValues,
    onOpenThread: (String) -> Unit,
    onOpenHistory: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        bottomBar = {
            TiebaliteBottomBar(
                destinations = MainDestination.values(),
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { innerPadding ->
        val paddingValues = mergePaddingValues(rootPaddingValues, innerPadding)

        NavHost(
            navController = navController,
            startDestination = MainDestination.Explore.route,
            enterTransition = { fadeIn(animationSpec = tween(320)) },
            exitTransition = { fadeOut(animationSpec = tween(320)) },
            popEnterTransition = { fadeIn(animationSpec = tween(260)) },
            popExitTransition = { fadeOut(animationSpec = tween(260)) },
        ) {
            composable(MainDestination.MyForums.route) {
                MyForumsRoute(paddingValues = paddingValues)
            }
            composable(MainDestination.Explore.route) {
                ExploreRoute(
                    paddingValues = paddingValues,
                    onOpenThread = onOpenThread,
                    onOpenImageViewer = onOpenImageViewer,
                )
            }
            composable(MainDestination.Messages.route) {
                MessagesScreen(paddingValues)
            }
            composable(MainDestination.Profile.route) {
                ProfileScreen(
                    paddingValues = paddingValues,
                    onOpenHistory = onOpenHistory,
                    onOpenSettings = onOpenSettings,
                )
            }
        }
    }
}

@Composable
private fun mergePaddingValues(
    outer: PaddingValues,
    inner: PaddingValues,
): PaddingValues {
    val layoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        start = outer.calculateStartPadding(layoutDirection) + inner.calculateStartPadding(layoutDirection),
        top = outer.calculateTopPadding() + inner.calculateTopPadding(),
        end = outer.calculateEndPadding(layoutDirection) + inner.calculateEndPadding(layoutDirection),
        bottom = outer.calculateBottomPadding() + inner.calculateBottomPadding(),
    )
}
