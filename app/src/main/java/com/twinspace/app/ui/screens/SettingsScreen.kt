package com.twinspace.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twinspace.app.model.DeviceCompatibility
import com.twinspace.app.model.TwinSpaceSettings
import com.twinspace.app.ui.theme.TwinBlue
import com.twinspace.app.ui.theme.TwinOutline
import com.twinspace.app.ui.theme.TwinViolet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    settings: TwinSpaceSettings,
    compatibility: DeviceCompatibility?,
    onToggleAllowSameApp: (Boolean) -> Unit,
    onTogglePreferFreeform: (Boolean) -> Unit,
    onShowPrivacyDisclosure: () -> Unit,
    onClearRecentApps: () -> Unit,
    onBack: () -> Unit
) {
    val freeformSupported = compatibility?.supportsFreeformWindows == true

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Settings", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TwinOutline, RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Allow Same App Twice", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Adds FLAG_ACTIVITY_MULTIPLE_TASK when pairing the same app in both panes. Target apps with singleInstance still allow only one window.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.allowSameAppTwice,
                            onCheckedChange = onToggleAllowSameApp,
                            colors = SwitchDefaults.colors(checkedTrackColor = TwinBlue)
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TwinOutline, RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use Freeform Windows (When Supported)", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (freeformSupported) {
                                    "Your device declares android.software.freeform_window_management. Launches left/right bounds."
                                } else {
                                    "Unavailable on this device. TwinSpace uses native Split Screen instead of faking floating windows."
                                },
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = settings.preferFreeformWhenSupported && freeformSupported,
                            enabled = freeformSupported,
                            onCheckedChange = onTogglePreferFreeform,
                            colors = SwitchDefaults.colors(checkedTrackColor = TwinViolet)
                        )
                    }
                }
            }

            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, TwinOutline, RoundedCornerShape(20.dp)),
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Privacy & Local Data", style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = "TwinSpace declares zero INTERNET permission. Installed app lists and recent selections stay strictly on your device.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(onClick = onShowPrivacyDisclosure) {
                                Text("View Privacy Disclosure")
                            }
                            OutlinedButton(onClick = onClearRecentApps) {
                                Text("Clear Recent Apps")
                            }
                        }
                    }
                }
            }
        }
    }
}
