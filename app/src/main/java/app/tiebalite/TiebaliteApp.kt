package app.tiebalite

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import app.tiebalite.auth.AuthState
import app.tiebalite.core.data.auth.AuthService
import app.tiebalite.theme.ThemeState
import app.tiebalite.core.ui.theme.runtime.TiebaliteTheme
import app.tiebalite.feature.explore.ExploreRoute
import app.tiebalite.feature.messages.MessagesScreen
import app.tiebalite.feature.profile.ProfileScreen
import app.tiebalite.feature.recommend.RecommendationScreen
import app.tiebalite.feature.settings.SettingsRoutes
import app.tiebalite.feature.settings.SettingsHomeScreen
import app.tiebalite.feature.settings.account.SettingsAccountItem
import app.tiebalite.feature.settings.account.SettingsAccountScreen
import app.tiebalite.feature.settings.account.login.CredentialLoginScreen
import app.tiebalite.feature.settings.account.login.LoginScreen
import app.tiebalite.feature.settings.ThemeSettingsEvent
import app.tiebalite.feature.settings.ThemeSettingsScreen
import app.tiebalite.feature.settings.ThemeSettingsState
import app.tiebalite.ui.components.TiebaliteBottomBar
import kotlinx.coroutines.launch

enum class MainDestination(
    val route: String,
    val labelRes: Int,
    val iconRes: Int
) {
    Recommendation(
        "recommendation",
        R.string.nav_recommend,
        R.drawable.ic_animated_rounded_inventory_2
    ),
    Explore(
        "explore",
        R.string.nav_explore,
        R.drawable.ic_animated_toy_fans
    ),
    Messages(
        "messages",
        R.string.nav_messages,
        R.drawable.ic_animated_rounded_notifications
    ),
    Profile(
        "profile",
        R.string.nav_profile,
        R.drawable.ic_animated_rounded_person
    )
}

@Composable
fun TiebaliteApp(
    themeState: ThemeState,
    authState: AuthState,
    authService: AuthService,
) {
    val state by themeState.state.collectAsState()
    val auth by authState.state.collectAsState()
    val appScope = rememberCoroutineScope()

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
                composable(MainDestination.Explore.route) {
                    ExploreRoute(paddingValues)
                }
                composable(MainDestination.Messages.route) {
                    MessagesScreen(paddingValues)
                }
                composable(MainDestination.Profile.route) {
                    ProfileScreen(
                        paddingValues = paddingValues,
                        onOpenSettings = { navController.navigate(SettingsRoutes.Home) },
                    )
                }
                composable(SettingsRoutes.Home) {
                    SettingsHomeScreen(
                        paddingValues = paddingValues,
                        isLoggedIn = auth.isLoggedIn,
                        onOpenAccountManage = { navController.navigate(SettingsRoutes.Account) },
                        onOpenTheme = { navController.navigate(SettingsRoutes.Theme) },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(SettingsRoutes.Account) {
                    SettingsAccountScreen(
                        paddingValues = paddingValues,
                        isLoggedIn = auth.isLoggedIn,
                        accounts =
                            auth.accounts.map { account ->
                                toSettingsAccountItem(
                                    accountId = account.accountId,
                                    bduss = account.session.bduss,
                                    activeAccountId = auth.activeAccount?.accountId,
                                )
                            },
                        onOpenWebLogin = { navController.navigate(SettingsRoutes.Login) },
                        onOpenCredentialLogin = { navController.navigate(SettingsRoutes.CredentialLogin) },
                        onSwitchAccount = { accountId ->
                            appScope.launch {
                                authService.switchAccount(accountId)
                            }
                        },
                        onRemoveAccount = { accountId ->
                            appScope.launch {
                                authService.removeAccount(accountId)
                            }
                        },
                        onLogoutActive = {
                            appScope.launch {
                                authService.logoutActiveAccount()
                            }
                        },
                        onBack = { navController.popBackStack() },
                    )
                }
                composable(SettingsRoutes.Login) {
                    LoginScreen(
                        paddingValues = paddingValues,
                        onBack = { navController.popBackStack() },
                        onLoginSuccess = { session, rawCookie ->
                            appScope.launch {
                                authService.loginWithWeb(session, rawCookie)
                            }
                            navController.popBackStack()
                        },
                    )
                }
                composable(SettingsRoutes.CredentialLogin) {
                    CredentialLoginScreen(
                        paddingValues = paddingValues,
                        onBack = { navController.popBackStack() },
                        onLoginSuccess = { session ->
                            appScope.launch {
                                authService.loginWithCredential(session)
                            }
                            navController.popBackStack()
                        },
                    )
                }
                composable(SettingsRoutes.Theme) {
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

private fun toSettingsAccountItem(
    accountId: String,
    bduss: String,
    activeAccountId: String?,
): SettingsAccountItem =
    SettingsAccountItem(
        accountId = accountId,
        title = "账号 ${accountId.take(6)}",
        subtitle = "BDUSS: ${bduss.take(8)}...",
        isActive = accountId == activeAccountId,
    )

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
                TiebaliteBottomBar(
                    destinations = MainDestination.values(),
                    currentRoute = currentRoute,
                    onNavigate = onNavigate
                )
            }
        },
        contentWindowInsets = WindowInsets.safeDrawing
            .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
        content = content
    )
}
