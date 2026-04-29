package app.tiebalite

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.activity.compose.BackHandler
import android.net.Uri
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import app.tiebalite.theme.ThemeState
import app.tiebalite.core.model.imageviewer.ImageViewerArgs
import app.tiebalite.core.ui.theme.runtime.TiebaliteTheme
import app.tiebalite.feature.history.HistoryRoute
import app.tiebalite.feature.history.HistoryRoutes
import app.tiebalite.feature.forum.ForumRoute
import app.tiebalite.feature.imageviewer.ImageViewerEntry
import app.tiebalite.feature.settings.SettingsRoutes
import app.tiebalite.feature.settings.SettingsHomeRoute
import app.tiebalite.feature.settings.account.SettingsAccountDetailRoute
import app.tiebalite.feature.settings.account.SettingsAccountRoute
import app.tiebalite.feature.settings.account.login.CredentialLoginRoute
import app.tiebalite.feature.settings.account.login.LoginRoute
import app.tiebalite.feature.settings.ThemeSettingsEvent
import app.tiebalite.feature.settings.ThemeSettingsScreen
import app.tiebalite.feature.settings.ThemeSettingsState
import app.tiebalite.feature.thread.ThreadRoute
import app.tiebalite.feature.thread.ThreadSubPostsRoute
import kotlinx.coroutines.delay

enum class MainDestination(
    val route: String,
    val iconRes: Int
) {
    MyForums(
        "my_forums",
        R.drawable.ic_animated_rounded_inventory_2
    ),
    Explore(
        "explore",
        R.drawable.ic_animated_toy_fans
    ),
    Messages(
        "messages",
        R.drawable.ic_animated_rounded_notifications
    ),
    Profile(
        "profile",
        R.drawable.ic_animated_rounded_person
    )
}

private object ThreadRoutes {
    const val ThreadIdArg = "threadId"
    const val PostIdArg = "postId"
    const val Thread = "thread/{$ThreadIdArg}"
    const val SubPosts = "thread/{$ThreadIdArg}/subposts/{$PostIdArg}"

    fun thread(threadId: String): String = "thread/$threadId"
    fun subPosts(threadId: Long, postId: Long): String = "thread/$threadId/subposts/$postId"
}

private object ForumRoutes {
    const val ForumNameArg = "forumName"
    const val Forum = "forum/{$ForumNameArg}"

    fun forum(forumName: String): String = "forum/${Uri.encode(forumName)}"
}

private object RootRoutes {
    const val Main = "main"
}

@Composable
fun TiebaliteApp(
    themeState: ThemeState,
) {
    val state by themeState.state.collectAsState()

    val navController = rememberNavController()
    var imageViewerArgs by rememberSaveable { mutableStateOf<ImageViewerArgs?>(null) }
    var isImageViewerVisible by rememberSaveable { mutableStateOf(false) }
    val openImageViewer: (ImageViewerArgs) -> Unit = { args ->
        imageViewerArgs = args
    }
    val closeImageViewer: (Boolean) -> Unit = { animated ->
        if (animated) {
            isImageViewerVisible = false
        } else {
            isImageViewerVisible = false
            imageViewerArgs = null
        }
    }

    val appliedState = state
    val seedColorHex = String.format("#%06X", appliedState.seedColor and 0xFFFFFF)

    TiebaliteTheme(
        themeMode = appliedState.themeMode,
        useDynamicColor = appliedState.useDynamicColor,
        seedColorHex = seedColorHex
    ) {
        LaunchedEffect(isImageViewerVisible, imageViewerArgs) {
            if (!isImageViewerVisible && imageViewerArgs != null) {
                delay(ImageViewerExitDurationMillis.toLong())
                if (!isImageViewerVisible) {
                    imageViewerArgs = null
                }
            }
        }
        LaunchedEffect(imageViewerArgs) {
            if (imageViewerArgs != null) {
                isImageViewerVisible = true
            }
        }
        Box(modifier = Modifier.fillMaxSize()) {
            androidx.compose.material3.Scaffold(
                contentWindowInsets = WindowInsets.safeDrawing
                    .only(WindowInsetsSides.Horizontal + WindowInsetsSides.Top),
            ) { paddingValues ->
                NavHost(
                    navController = navController,
                    startDestination = RootRoutes.Main,
                    modifier = Modifier.fillMaxSize(),
                    enterTransition = { fadeIn(animationSpec = tween(320)) },
                    exitTransition = { fadeOut(animationSpec = tween(320)) },
                    popEnterTransition = { fadeIn(animationSpec = tween(260)) },
                    popExitTransition = { fadeOut(animationSpec = tween(260)) },
                ) {
                    mainGraph(
                        navController = navController,
                        paddingValues = paddingValues,
                        onOpenImageViewer = openImageViewer,
                    )
                    settingsGraph(
                        navController = navController,
                        paddingValues = paddingValues,
                        themeSettingsState = ThemeSettingsState(
                            themeMode = state.themeMode,
                            useDynamicColor = state.useDynamicColor,
                            seedColorHex = seedColorHex
                        ),
                        onThemeSettingsEvent = { event ->
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
                    )
                    historyGraph(
                        navController = navController,
                        paddingValues = paddingValues,
                    )
                    forumGraph(
                        navController = navController,
                        paddingValues = paddingValues,
                        onOpenImageViewer = openImageViewer,
                    )
                    threadGraph(
                        navController = navController,
                        paddingValues = paddingValues,
                        onOpenImageViewer = openImageViewer,
                    )
                }
            }
            imageViewerArgs?.let { args ->
                BackHandler {
                    closeImageViewer(true)
                }
                AnimatedVisibility(
                    visible = isImageViewerVisible,
                    enter = fadeIn(animationSpec = tween(ImageViewerEnterDurationMillis)),
                    exit = fadeOut(animationSpec = tween(ImageViewerExitDurationMillis)),
                ) {
                    ImageViewerEntry(
                        paddingValues = PaddingValues(),
                        args = args,
                        onBack = { closeImageViewer(true) },
                        onDragDismissed = { closeImageViewer(false) },
                    )
                }
            }
        }
    }
}

private fun NavGraphBuilder.mainGraph(
    navController: NavController,
    paddingValues: PaddingValues,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
) {
    composable(RootRoutes.Main) {
        MainShell(
            rootPaddingValues = paddingValues,
            onOpenThread = { threadId ->
                navController.navigate(ThreadRoutes.thread(threadId))
            },
            onOpenForum = { forumName ->
                navController.navigate(ForumRoutes.forum(forumName))
            },
            onOpenHistory = { navController.navigate(HistoryRoutes.Home) },
            onOpenSettings = { navController.navigate(SettingsRoutes.Home) },
            onOpenImageViewer = onOpenImageViewer,
        )
    }
}

private fun NavGraphBuilder.settingsGraph(
    navController: NavController,
    paddingValues: PaddingValues,
    themeSettingsState: ThemeSettingsState,
    onThemeSettingsEvent: (ThemeSettingsEvent) -> Unit,
) {
    composable(SettingsRoutes.Home) {
        SettingsHomeRoute(
            paddingValues = paddingValues,
            onOpenAccountManage = { navController.navigate(SettingsRoutes.Account) },
            onOpenTheme = { navController.navigate(SettingsRoutes.Theme) },
            onBack = { navController.popBackStack() },
        )
    }
    composable(SettingsRoutes.Account) {
        SettingsAccountRoute(
            paddingValues = paddingValues,
            onOpenWebLogin = { navController.navigate(SettingsRoutes.Login) },
            onOpenCredentialLogin = { navController.navigate(SettingsRoutes.CredentialLogin) },
            onOpenAccountDetail = { accountId ->
                navController.navigate(SettingsRoutes.accountDetail(accountId))
            },
            onBack = { navController.popBackStack() },
        )
    }
    composable(
        route = SettingsRoutes.AccountDetail,
        arguments =
            listOf(
                navArgument(SettingsRoutes.AccountIdArg) {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val accountId =
            backStackEntry.arguments
                ?.getString(SettingsRoutes.AccountIdArg)
                .orEmpty()
        SettingsAccountDetailRoute(
            paddingValues = paddingValues,
            accountId = accountId,
            onBack = { navController.popBackStack() },
        )
    }
    composable(SettingsRoutes.Login) {
        LoginRoute(
            paddingValues = paddingValues,
            onBack = { navController.popBackStack() },
        )
    }
    composable(SettingsRoutes.CredentialLogin) {
        CredentialLoginRoute(
            paddingValues = paddingValues,
            onBack = { navController.popBackStack() },
        )
    }
    composable(SettingsRoutes.Theme) {
        ThemeSettingsScreen(
            paddingValues = paddingValues,
            state = themeSettingsState,
            onEvent = onThemeSettingsEvent,
            onBack = { navController.popBackStack() }
        )
    }
}

private fun NavGraphBuilder.historyGraph(
    navController: NavController,
    paddingValues: PaddingValues,
) {
    composable(HistoryRoutes.Home) {
        HistoryRoute(
            paddingValues = paddingValues,
            onOpenThread = { threadId ->
                navController.navigate(ThreadRoutes.thread(threadId.toString()))
            },
            onOpenForum = { forumName ->
                navController.navigate(ForumRoutes.forum(forumName))
            },
            onBack = { navController.popBackStack() },
        )
    }
}

private fun NavGraphBuilder.forumGraph(
    navController: NavController,
    paddingValues: PaddingValues,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
) {
    composable(
        route = ForumRoutes.Forum,
        arguments =
            listOf(
                navArgument(ForumRoutes.ForumNameArg) {
                    type = NavType.StringType
                },
            ),
    ) { backStackEntry ->
        val forumName =
            backStackEntry.arguments
                ?.getString(ForumRoutes.ForumNameArg)
                ?.let(Uri::decode)
                ?.takeIf { it.isNotBlank() }
                ?: return@composable
        ForumRoute(
            paddingValues = paddingValues,
            forumName = forumName,
            onBack = { navController.popBackStack() },
            onOpenThread = { threadId ->
                navController.navigate(ThreadRoutes.thread(threadId))
            },
            onOpenImageViewer = onOpenImageViewer,
        )
    }
}

private fun NavGraphBuilder.threadGraph(
    navController: NavController,
    paddingValues: PaddingValues,
    onOpenImageViewer: (ImageViewerArgs) -> Unit,
) {
    composable(
        route = ThreadRoutes.Thread,
        arguments =
            listOf(
                navArgument(ThreadRoutes.ThreadIdArg) {
                    type = NavType.LongType
                },
            ),
    ) { backStackEntry ->
        val threadId =
            backStackEntry.arguments
                ?.getLong(ThreadRoutes.ThreadIdArg)
                ?: return@composable
        ThreadRoute(
            paddingValues = paddingValues,
            threadId = threadId,
            onBack = { navController.popBackStack() },
            onOpenForum = { forumName ->
                navController.navigate(ForumRoutes.forum(forumName))
            },
            onOpenSubPosts = { postId ->
                navController.navigate(ThreadRoutes.subPosts(threadId, postId))
            },
            onOpenImageViewer = onOpenImageViewer,
        )
    }
    composable(
        route = ThreadRoutes.SubPosts,
        arguments =
            listOf(
                navArgument(ThreadRoutes.ThreadIdArg) {
                    type = NavType.LongType
                },
                navArgument(ThreadRoutes.PostIdArg) {
                    type = NavType.LongType
                },
            ),
    ) { backStackEntry ->
        val threadId =
            backStackEntry.arguments
                ?.getLong(ThreadRoutes.ThreadIdArg)
                ?: return@composable
        val postId =
            backStackEntry.arguments
                ?.getLong(ThreadRoutes.PostIdArg)
                ?: return@composable
        ThreadSubPostsRoute(
            paddingValues = paddingValues,
            threadId = threadId,
            postId = postId,
            onBack = { navController.popBackStack() },
            onOpenImageViewer = onOpenImageViewer,
        )
    }
}

private const val ImageViewerEnterDurationMillis = 220
private const val ImageViewerExitDurationMillis = 180
