package com.twinspace.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.twinspace.app.ui.SelectionSlot
import com.twinspace.app.ui.TwinSpaceViewModel
import com.twinspace.app.ui.components.LaunchOutcomeDialog
import com.twinspace.app.ui.components.PrivacyDisclosureDialog
import com.twinspace.app.ui.screens.AppPickerScreen
import com.twinspace.app.ui.screens.CompatibilityScreen
import com.twinspace.app.ui.screens.DashboardScreen
import com.twinspace.app.ui.screens.HelpTutorialScreen
import com.twinspace.app.ui.screens.SettingsScreen
import com.twinspace.app.ui.theme.TwinSpaceTheme

private object Routes {
    const val DASHBOARD = "dashboard"
    const val PICKER_PATTERN = "picker/{slot}"
    const val COMPATIBILITY = "compatibility"
    const val SETTINGS = "settings"
    const val HELP = "help"

    fun picker(slot: SelectionSlot): String = "picker/${slot.name}"
}

class MainActivity : ComponentActivity() {

    private val viewModel: TwinSpaceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        viewModel.refreshCompatibility(this)

        setContent {
            TwinSpaceTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TwinSpaceRoot(
                        activity = this,
                        viewModel = viewModel
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshCompatibility(this)
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean, newConfig: Configuration) {
        super.onMultiWindowModeChanged(isInMultiWindowMode, newConfig)
        viewModel.refreshCompatibility(this)
    }
}

@Composable
private fun TwinSpaceRoot(
    activity: MainActivity,
    viewModel: TwinSpaceViewModel
) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val iconMap by viewModel.iconMap.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.refreshCompatibility(activity)
    }

    if (uiState.settings.showPrivacyDisclosureOnStart) {
        PrivacyDisclosureDialog(
            onAccept = { viewModel.acknowledgePrivacyDisclosure() }
        )
    }

    uiState.lastLaunchResult?.let { launchResult ->
        LaunchOutcomeDialog(
            result = launchResult,
            onDismiss = { viewModel.dismissLaunchResult() },
            onForceDirectAttempt = {
                viewModel.openBoth(activity = activity, forceDirectAttempt = true)
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = Routes.DASHBOARD,
        enterTransition = { fadeIn() },
        exitTransition = { fadeOut() }
    ) {
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                uiState = uiState,
                iconMap = iconMap,
                onRequestIcon = { viewModel.ensureIconLoaded(it) },
                onSelectSlot = { slot ->
                    viewModel.dismissBanner()
                    viewModel.onSearchQueryChanged("")
                    if (uiState.allApps.isEmpty() && !uiState.isLoadingApps) {
                        viewModel.refreshInstalledApps()
                    }
                    navController.navigate(Routes.picker(slot)) {
                        launchSingleTop = true
                    }
                },
                onSwapApps = { viewModel.swapApps() },
                onClearSelection = { viewModel.clearSelection() },
                onOpenBoth = { viewModel.openBoth(activity = activity) },
                onOpenAllApps = {
                    viewModel.dismissBanner()
                    viewModel.onSearchQueryChanged("")
                    if (uiState.allApps.isEmpty() && !uiState.isLoadingApps) {
                        viewModel.refreshInstalledApps()
                    }
                    navController.navigate(Routes.picker(SelectionSlot.BROWSE_ONLY)) {
                        launchSingleTop = true
                    }
                },
                onOpenRecentApp = { app ->
                    viewModel.selectApp(app, SelectionSlot.BROWSE_ONLY)
                },
                onOpenCompatibility = {
                    viewModel.refreshCompatibility(activity)
                    navController.navigate(Routes.COMPATIBILITY) {
                        launchSingleTop = true
                    }
                },
                onOpenSettings = {
                    navController.navigate(Routes.SETTINGS) {
                        launchSingleTop = true
                    }
                },
                onOpenHelp = {
                    navController.navigate(Routes.HELP) {
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(
            route = Routes.PICKER_PATTERN,
            arguments = listOf(navArgument("slot") { type = NavType.StringType })
        ) { backStackEntry ->
            val slot = SelectionSlot.fromRouteArg(backStackEntry.arguments?.getString("slot"))
            AppPickerScreen(
                slot = slot,
                uiState = uiState,
                iconMap = iconMap,
                onRequestIcon = { viewModel.ensureIconLoaded(it) },
                onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                onRefreshApps = { viewModel.refreshInstalledApps() },
                onAppPicked = { app ->
                    val accepted = viewModel.selectApp(app, slot)
                    if (accepted) {
                        viewModel.onSearchQueryChanged("")
                        navController.popBackStack()
                    }
                },
                onBack = {
                    viewModel.onSearchQueryChanged("")
                    viewModel.dismissBanner()
                    navController.popBackStack()
                }
            )
        }

        composable(Routes.COMPATIBILITY) {
            CompatibilityScreen(
                compatibility = uiState.compatibility,
                onRefresh = { viewModel.refreshCompatibility(activity) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(
                settings = uiState.settings,
                compatibility = uiState.compatibility,
                onToggleAllowSameApp = { viewModel.updateAllowSameAppTwice(it) },
                onTogglePreferFreeform = { viewModel.updatePreferFreeform(it) },
                onShowPrivacyDisclosure = { viewModel.reopenPrivacyDisclosure() },
                onClearRecentApps = { viewModel.clearRecentHistory() },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.HELP) {
            HelpTutorialScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
