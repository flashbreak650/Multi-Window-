package com.twinspace.app

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
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
    const val PICKER = "picker"
    const val COMPATIBILITY = "compatibility"
    const val SETTINGS = "settings"
    const val HELP = "help"
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
    var activeSlot by remember { mutableStateOf(SelectionSlot.FIRST) }

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
                    activeSlot = slot
                    viewModel.dismissBanner()
                    navController.navigate(Routes.PICKER)
                },
                onSwapApps = { viewModel.swapApps() },
                onClearSelection = { viewModel.clearSelection() },
                onOpenBoth = { viewModel.openBoth(activity = activity) },
                onOpenAllApps = {
                    activeSlot = SelectionSlot.BROWSE_ONLY
                    viewModel.dismissBanner()
                    navController.navigate(Routes.PICKER)
                },
                onOpenRecentApp = { app ->
                    viewModel.selectApp(app, SelectionSlot.BROWSE_ONLY)
                },
                onOpenCompatibility = {
                    viewModel.refreshCompatibility(activity)
                    navController.navigate(Routes.COMPATIBILITY)
                },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
                onOpenHelp = { navController.navigate(Routes.HELP) }
            )
        }

        composable(Routes.PICKER) {
            AppPickerScreen(
                slot = activeSlot,
                uiState = uiState,
                iconMap = iconMap,
                onRequestIcon = { viewModel.ensureIconLoaded(it) },
                onSearchChanged = { viewModel.onSearchQueryChanged(it) },
                onRefreshApps = { viewModel.refreshInstalledApps() },
                onAppPicked = { app ->
                    val accepted = viewModel.selectApp(app, activeSlot)
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
