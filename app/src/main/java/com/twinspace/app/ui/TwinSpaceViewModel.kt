package com.twinspace.app.ui

import android.app.Activity
import android.app.Application
import androidx.compose.ui.graphics.ImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.twinspace.app.data.AppDiscoveryResult
import com.twinspace.app.data.InstalledAppsRepository
import com.twinspace.app.data.TwinSpacePreferences
import com.twinspace.app.engine.DeviceCompatibilityInspector
import com.twinspace.app.engine.MultiWindowLauncher
import com.twinspace.app.model.DeviceCompatibility
import com.twinspace.app.model.InstalledApp
import com.twinspace.app.model.LaunchResult
import com.twinspace.app.model.TwinSpaceSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SelectionSlot {
    FIRST,
    SECOND,
    BROWSE_ONLY;

    companion object {
        fun fromRouteArg(value: String?): SelectionSlot {
            return entries.firstOrNull { it.name == value } ?: FIRST
        }
    }
}

data class TwinSpaceUiState(
    val isLoadingApps: Boolean = true,
    val allApps: List<InstalledApp> = emptyList(),
    val appsLoadError: String? = null,
    val recentApps: List<InstalledApp> = emptyList(),
    val searchQuery: String = "",
    val firstApp: InstalledApp? = null,
    val secondApp: InstalledApp? = null,
    val settings: TwinSpaceSettings = TwinSpaceSettings(),
    val compatibility: DeviceCompatibility? = null,
    val lastLaunchResult: LaunchResult? = null,
    val statusBannerMessage: String? = null
) {
    val filteredApps: List<InstalledApp>
        get() = InstalledAppsRepository.filterApps(allApps, searchQuery)

    val canLaunchBoth: Boolean
        get() = firstApp != null && secondApp != null
}

class TwinSpaceViewModel(application: Application) : AndroidViewModel(application) {

    private val appsRepository = InstalledAppsRepository(application)
    private val preferences = TwinSpacePreferences(application)
    private val launcher = MultiWindowLauncher()

    private val _apps = MutableStateFlow<List<InstalledApp>>(emptyList())
    private val _isLoading = MutableStateFlow(true)
    private val _appsLoadError = MutableStateFlow<String?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _firstApp = MutableStateFlow<InstalledApp?>(null)
    private val _secondApp = MutableStateFlow<InstalledApp?>(null)
    private val _recentPackageNames = MutableStateFlow(preferences.getRecentPackageNames())
    private val _settings = MutableStateFlow(preferences.getSettings())
    private val _compatibility = MutableStateFlow<DeviceCompatibility?>(null)
    private val _launchResult = MutableStateFlow<LaunchResult?>(null)
    private val _bannerMessage = MutableStateFlow<String?>(null)

    private val _iconMap = MutableStateFlow<Map<String, ImageBitmap>>(emptyMap())
    val iconMap: StateFlow<Map<String, ImageBitmap>> = _iconMap

    val uiState: StateFlow<TwinSpaceUiState> = combine(
        _isLoading,
        _apps,
        _appsLoadError,
        _recentPackageNames,
        _searchQuery,
        _firstApp,
        _secondApp,
        _settings,
        _compatibility,
        _launchResult,
        _bannerMessage
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val isLoading = values[0] as Boolean
        @Suppress("UNCHECKED_CAST")
        val allApps = values[1] as List<InstalledApp>
        val appsLoadError = values[2] as String?
        @Suppress("UNCHECKED_CAST")
        val recentPkgs = values[3] as List<String>
        val query = values[4] as String
        val first = values[5] as InstalledApp?
        val second = values[6] as InstalledApp?
        val settings = values[7] as TwinSpaceSettings
        val compatibility = values[8] as DeviceCompatibility?
        val launchResult = values[9] as LaunchResult?
        val banner = values[10] as String?

        val recentModels = recentPkgs.mapNotNull { pkg ->
            allApps.firstOrNull { it.packageName == pkg }
        }

        TwinSpaceUiState(
            isLoadingApps = isLoading,
            allApps = allApps,
            appsLoadError = appsLoadError,
            recentApps = recentModels,
            searchQuery = query,
            firstApp = first,
            secondApp = second,
            settings = settings,
            compatibility = compatibility,
            lastLaunchResult = launchResult,
            statusBannerMessage = banner
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TwinSpaceUiState()
    )

    init {
        refreshInstalledApps()
    }

    fun refreshInstalledApps() {
        viewModelScope.launch {
            _isLoading.value = true
            _appsLoadError.value = null
            when (val result = appsRepository.loadLaunchableApps()) {
                is AppDiscoveryResult.Success -> {
                    _apps.value = result.apps
                    _appsLoadError.value = null
                }
                is AppDiscoveryResult.Empty -> {
                    _apps.value = emptyList()
                    _appsLoadError.value = result.message
                }
                is AppDiscoveryResult.Failure -> {
                    _apps.value = emptyList()
                    _appsLoadError.value = result.message
                }
            }
            _isLoading.value = false
        }
    }

    fun refreshCompatibility(activity: Activity) {
        _compatibility.value = DeviceCompatibilityInspector.inspect(activity)
    }

    fun ensureIconLoaded(app: InstalledApp) {
        val key = "${app.packageName}/${app.activityName}"
        if (_iconMap.value.containsKey(key)) return
        viewModelScope.launch {
            val bitmap = appsRepository.loadAppIcon(app) ?: return@launch
            _iconMap.update { current ->
                if (current.containsKey(key)) current else current + (key to bitmap)
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun selectApp(app: InstalledApp, slot: SelectionSlot): Boolean {
        val allowDuplicate = _settings.value.allowSameAppTwice
        when (slot) {
            SelectionSlot.FIRST -> {
                if (!allowDuplicate && _secondApp.value?.packageName == app.packageName &&
                    _secondApp.value?.activityName == app.activityName
                ) {
                    _bannerMessage.value = "${app.label} is already selected as Second App. Choose a different app or enable duplicate apps in Settings."
                    return false
                }
                _firstApp.value = app
            }

            SelectionSlot.SECOND -> {
                if (!allowDuplicate && _firstApp.value?.packageName == app.packageName &&
                    _firstApp.value?.activityName == app.activityName
                ) {
                    _bannerMessage.value = "${app.label} is already selected as First App. Choose a different app or enable duplicate apps in Settings."
                    return false
                }
                _secondApp.value = app
            }

            SelectionSlot.BROWSE_ONLY -> {
                if (_firstApp.value == null) {
                    _firstApp.value = app
                } else if (_secondApp.value == null) {
                    if (!allowDuplicate && _firstApp.value?.packageName == app.packageName &&
                        _firstApp.value?.activityName == app.activityName
                    ) {
                        _bannerMessage.value = "${app.label} is already in First App. Pick another app for Second App."
                        return false
                    }
                    _secondApp.value = app
                } else {
                    if (!allowDuplicate && _firstApp.value?.packageName == app.packageName &&
                        _firstApp.value?.activityName == app.activityName
                    ) {
                        _bannerMessage.value = "${app.label} is already in First App."
                        return false
                    }
                    _secondApp.value = app
                }
            }
        }

        preferences.recordRecentApps(listOf(app.packageName))
        _recentPackageNames.value = preferences.getRecentPackageNames()
        _bannerMessage.value = null
        return true
    }

    fun swapApps() {
        val first = _firstApp.value
        val second = _secondApp.value
        if (first == null && second == null) return
        _firstApp.value = second
        _secondApp.value = first
    }

    fun clearSelection() {
        _firstApp.value = null
        _secondApp.value = null
        _bannerMessage.value = null
        _launchResult.value = null
    }

    fun dismissBanner() {
        _bannerMessage.value = null
    }

    fun dismissLaunchResult() {
        _launchResult.value = null
    }

    fun openBoth(activity: Activity, forceDirectAttempt: Boolean = false) {
        val compat = DeviceCompatibilityInspector.inspect(activity)
        _compatibility.value = compat

        val first = _firstApp.value
        val second = _secondApp.value
        if (first == null || second == null) {
            _launchResult.value = LaunchResult.Failed(
                title = "Select Two Apps",
                reason = "Pick both a First App and a Second App before tapping Open Both."
            )
            return
        }

        val result = launcher.launchBoth(
            activity = activity,
            firstApp = first,
            secondApp = second,
            compatibility = compat,
            settings = _settings.value,
            forceDirectAttempt = forceDirectAttempt
        )
        _launchResult.value = result

        if (result is LaunchResult.LaunchedSplitPair || result is LaunchResult.PartialOrWarning) {
            preferences.recordRecentApps(listOf(first.packageName, second.packageName))
            _recentPackageNames.value = preferences.getRecentPackageNames()
        }
    }

    fun updateAllowSameAppTwice(enabled: Boolean) {
        val updated = _settings.value.copy(allowSameAppTwice = enabled)
        _settings.value = updated
        preferences.saveSettings(updated)
    }

    fun updatePreferFreeform(enabled: Boolean) {
        val updated = _settings.value.copy(preferFreeformWhenSupported = enabled)
        _settings.value = updated
        preferences.saveSettings(updated)
    }

    fun acknowledgePrivacyDisclosure() {
        val updated = _settings.value.copy(showPrivacyDisclosureOnStart = false)
        _settings.value = updated
        preferences.saveSettings(updated)
    }

    fun reopenPrivacyDisclosure() {
        val updated = _settings.value.copy(showPrivacyDisclosureOnStart = true)
        _settings.value = updated
        preferences.saveSettings(updated)
    }

    fun clearRecentHistory() {
        preferences.clearRecentApps()
        _recentPackageNames.value = emptyList()
    }
}
