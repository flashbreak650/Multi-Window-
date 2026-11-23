package com.twinspace.app.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.HelpOutline
import androidx.compose.material.icons.rounded.Apps
import androidx.compose.material.icons.rounded.ClearAll
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Splitscreen
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twinspace.app.model.InstalledApp
import com.twinspace.app.ui.SelectionSlot
import com.twinspace.app.ui.TwinSpaceUiState
import com.twinspace.app.ui.components.RealAppIcon
import com.twinspace.app.ui.components.SelectedAppSlotCard
import com.twinspace.app.ui.components.TwinSpaceLogo
import com.twinspace.app.ui.theme.TwinBlue
import com.twinspace.app.ui.theme.TwinBlueSoft
import com.twinspace.app.ui.theme.TwinOutline
import com.twinspace.app.ui.theme.TwinSuccess
import com.twinspace.app.ui.theme.TwinViolet
import com.twinspace.app.ui.theme.TwinVioletSoft

@Composable
fun DashboardScreen(
    uiState: TwinSpaceUiState,
    iconMap: Map<String, ImageBitmap>,
    onRequestIcon: (InstalledApp) -> Unit,
    onSelectSlot: (SelectionSlot) -> Unit,
    onSwapApps: () -> Unit,
    onClearSelection: () -> Unit,
    onOpenBoth: () -> Unit,
    onOpenAllApps: () -> Unit,
    onOpenRecentApp: (InstalledApp) -> Unit,
    onOpenCompatibility: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenHelp: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header with TwinSpace Logo
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TwinSpaceLogo(size = 46.dp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "TwinSpace",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Text(
                            text = "Multi-Window Manager",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val inMultiWindow = uiState.compatibility?.isCurrentlyInMultiWindowMode == true
                Surface(
                    color = if (inMultiWindow) TwinSuccess.copy(alpha = 0.12f) else TwinBlueSoft,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = if (inMultiWindow) "Split Active" else "Single Pane",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (inMultiWindow) TwinSuccess else TwinBlue,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        // Two-App Selection Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
                    .border(1.dp, TwinOutline, RoundedCornerShape(24.dp)),
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Pair Two Installed Apps",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    val firstKey = uiState.firstApp?.let { "${it.packageName}/${it.activityName}" }
                    SelectedAppSlotCard(
                        slotTitle = "First App",
                        accentColor = TwinBlue,
                        softAccentColor = TwinBlueSoft,
                        selectedApp = uiState.firstApp,
                        iconBitmap = firstKey?.let { iconMap[it] },
                        onRequestIcon = onRequestIcon,
                        onClick = { onSelectSlot(SelectionSlot.FIRST) }
                    )

                    val secondKey = uiState.secondApp?.let { "${it.packageName}/${it.activityName}" }
                    SelectedAppSlotCard(
                        slotTitle = "Second App",
                        accentColor = TwinViolet,
                        softAccentColor = TwinVioletSoft,
                        selectedApp = uiState.secondApp,
                        iconBitmap = secondKey?.let { iconMap[it] },
                        onRequestIcon = onRequestIcon,
                        onClick = { onSelectSlot(SelectionSlot.SECOND) }
                    )

                    // Swap & Clear Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onSwapApps,
                            enabled = uiState.firstApp != null || uiState.secondApp != null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.SwapVert,
                                contentDescription = "Swap Apps",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Swap Apps", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }

                        OutlinedButton(
                            onClick = onClearSelection,
                            enabled = uiState.firstApp != null || uiState.secondApp != null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.ClearAll,
                                contentDescription = "Clear Selection",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Clear Selection", maxLines = 1, overflow = TextOverflow.Ellipsis)
                        }
                    }

                    // Primary OPEN BOTH CTA
                    Button(
                        onClick = onOpenBoth,
                        enabled = uiState.canLaunchBoth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Transparent,
                            disabledContainerColor = TwinOutline
                        ),
                        contentPadding = PaddingValues(0.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    brush = if (uiState.canLaunchBoth) {
                                        Brush.horizontalGradient(listOf(TwinBlue, TwinViolet))
                                    } else {
                                        Brush.horizontalGradient(listOf(TwinOutline, TwinOutline))
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Rounded.Splitscreen,
                                    contentDescription = null,
                                    tint = if (uiState.canLaunchBoth) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "OPEN BOTH",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (uiState.canLaunchBoth) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Recent Apps Section
        item {
            AnimatedVisibility(
                visible = uiState.recentApps.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Recent Apps",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(uiState.recentApps, key = { "${it.packageName}/${it.activityName}" }) { app ->
                            val key = "${app.packageName}/${app.activityName}"
                            Surface(
                                modifier = Modifier
                                    .width(98.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(1.dp, TwinOutline, RoundedCornerShape(18.dp))
                                    .clickable { onOpenRecentApp(app) },
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    RealAppIcon(
                                        app = app,
                                        iconBitmap = iconMap[key],
                                        onRequestIcon = onRequestIcon,
                                        size = 44.dp
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = app.label,
                                        style = MaterialTheme.typography.bodyMedium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Navigation Grid: All Apps, Device Compatibility, Settings, Help & Tutorial
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Tools & System",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardTile(
                        title = "All Apps",
                        subtitle = "${uiState.allApps.size} installed",
                        icon = Icons.Rounded.Apps,
                        accent = TwinBlue,
                        softAccent = TwinBlueSoft,
                        onClick = onOpenAllApps,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardTile(
                        title = "Compatibility",
                        subtitle = uiState.compatibility?.androidVersionLabel ?: "Inspect SDK",
                        icon = Icons.Rounded.Devices,
                        accent = TwinViolet,
                        softAccent = TwinVioletSoft,
                        onClick = onOpenCompatibility,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DashboardTile(
                        title = "Settings",
                        subtitle = "Launch & privacy",
                        icon = Icons.Rounded.Settings,
                        accent = TwinBlue,
                        softAccent = TwinBlueSoft,
                        onClick = onOpenSettings,
                        modifier = Modifier.weight(1f)
                    )
                    DashboardTile(
                        title = "Help & Tutorial",
                        subtitle = "Split-screen guide",
                        icon = Icons.AutoMirrored.Rounded.HelpOutline,
                        accent = TwinViolet,
                        softAccent = TwinVioletSoft,
                        onClick = onOpenHelp,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun DashboardTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    softAccent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, TwinOutline, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(softAccent),
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = accent)
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
