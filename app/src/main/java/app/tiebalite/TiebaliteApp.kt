package app.tiebalite

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.tiebalite.core.ui.theme.runtime.TiebaliteTheme
import app.tiebalite.core.ui.theme.state.ThemeState
import app.tiebalite.core.data.theme.ThemePreferences
import app.tiebalite.feature.messages.MessagesScreen
import app.tiebalite.feature.profile.ProfileScreen
import app.tiebalite.feature.recommend.RecommendationScreen
import app.tiebalite.feature.settings.SettingsHomeScreen
import app.tiebalite.feature.settings.ThemeSettingsEvent
import app.tiebalite.feature.settings.ThemeSettingsScreen
import app.tiebalite.feature.settings.ThemeSettingsState

private enum class MainDestination(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector
) {
    Recommendation("recommendation", R.string.nav_recommend, Icons.Filled.Home),
    Messages("messages", R.string.nav_messages, Icons.Filled.Mail),
    Profile("profile", R.string.nav_profile, Icons.Filled.Person)
}

@Composable
fun TiebaliteApp() {
    val context = LocalContext.current
    val scope = androidx.compose.runtime.rememberCoroutineScope()
    val themeState = androidx.compose.runtime.remember(context, scope) {
        ThemeState(ThemePreferences(context), scope)
    }
    val state by themeState.state.collectAsState()

    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val appliedState = state
    val seedColorHex = String.format("#%06X", appliedState.seedColor and 0xFFFFFF)

    TiebaliteTheme(
        themeMode = appliedState.themeMode,
        useDynamicColor = appliedState.useDynamicColor,
        seedColorHex = seedColorHex
    ) {
        app.tiebalite.core.ui.system.ApplySystemBars()
        // app.tiebalite.core.ui.system.SystemBarsBackground()
        // app.tiebalite.core.ui.system.SystemBarsBackground(color = androidx.compose.ui.graphics.Color.Red)

        AppScaffold(
            showBottomBar = currentRoute?.let { route ->
                MainDestination.values().any { it.route == route }
            } ?: true,
            currentRoute = currentRoute,
            onNavigate = { route ->
                navController.navigate(route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = MainDestination.Recommendation.route,
                modifier = Modifier,
                enterTransition = { fadeIn(animationSpec = tween(320)) },
                exitTransition = { fadeOut(animationSpec = tween(320)) },
                popEnterTransition = { fadeIn(animationSpec = tween(260)) },
                popExitTransition = { fadeOut(animationSpec = tween(260)) }
            ) {
                composable(MainDestination.Recommendation.route) {
                    RecommendationScreen(paddingValues)
                }
                composable(MainDestination.Messages.route) {
                    MessagesScreen(paddingValues)
                }
                composable(MainDestination.Profile.route) {
                    ProfileScreen(
                        paddingValues = paddingValues,
                        onOpenSettings = { navController.navigate("settings/home") }
                    )
                }
                composable("settings/home") {
                    SettingsHomeScreen(
                        paddingValues = paddingValues,
                        onOpenTheme = { navController.navigate("settings/theme") },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("settings/theme") {
                    ThemeSettingsScreen(
                        paddingValues = paddingValues,
                        state = ThemeSettingsState(
                            themeMode = state.themeMode,
                            useDynamicColor = state.useDynamicColor,
                            seedColorHex = seedColorHex
                        ),
                        onEvent = { event ->
                            when (event) {
                                is ThemeSettingsEvent.SetThemeMode ->
                                    themeState.setThemeMode(event.mode)
                                is ThemeSettingsEvent.SetDynamicColor ->
                                    themeState.setDynamicColor(event.enabled)
                                is ThemeSettingsEvent.SetSeedColor ->
                                    run {
                                        val cleaned = event.value.trim().removePrefix("#")
                                        if (cleaned.length != 6) {
                                            null
                                        } else {
                                            cleaned.toLongOrNull(16)?.let { 0xFF000000 or it }
                                        }
                                    }?.let { themeState.setSeedColor(it) }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

@Composable
private fun AppScaffold(
    showBottomBar: Boolean,
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    content: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    MainDestination.values().forEach { destination ->
                        val selected = currentRoute == destination.route
                        NavigationBarItem(
                            selected = selected,
                            onClick = { onNavigate(destination.route) },
                            icon = { Icon(destination.icon, contentDescription = null) },
                            label = { Text(text = stringResource(destination.labelRes)) }
                        )
                    }
                }
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        content = content
    )
}
