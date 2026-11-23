package com.twinspace.app.ui.screens

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
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.twinspace.app.model.InstalledApp
import com.twinspace.app.ui.SelectionSlot
import com.twinspace.app.ui.TwinSpaceUiState
import com.twinspace.app.ui.components.RealAppIcon
import com.twinspace.app.ui.theme.TwinBlue
import com.twinspace.app.ui.theme.TwinBlueSoft
import com.twinspace.app.ui.theme.TwinOutline
import com.twinspace.app.ui.theme.TwinViolet
import com.twinspace.app.ui.theme.TwinVioletSoft
import com.twinspace.app.ui.theme.TwinWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    slot: SelectionSlot,
    uiState: TwinSpaceUiState,
    iconMap: Map<String, ImageBitmap>,
    onRequestIcon: (InstalledApp) -> Unit,
    onSearchChanged: (String) -> Unit,
    onRefreshApps: () -> Unit,
    onAppPicked: (InstalledApp) -> Unit,
    onBack: () -> Unit
) {
    val screenTitle = when (slot) {
        SelectionSlot.FIRST -> "Select First App"
        SelectionSlot.SECOND -> "Select Second App"
        SelectionSlot.BROWSE_ONLY -> "All Installed Apps"
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(screenTitle, style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    IconButton(onClick = onRefreshApps) {
                        Icon(
                            imageVector = Icons.Rounded.Refresh,
                            contentDescription = "Refresh installed apps"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
        ) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchChanged,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search installed apps or packages...") },
                leadingIcon = {
                    Icon(imageVector = Icons.Rounded.Search, contentDescription = null)
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChanged("") }) {
                            Icon(imageVector = Icons.Rounded.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = TwinBlue,
                    unfocusedBorderColor = TwinOutline,
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            if (uiState.statusBannerMessage != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    color = TwinWarning.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = uiState.statusBannerMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TwinWarning,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (uiState.isLoadingApps) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = TwinBlue)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Discovering installed apps...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val filtered = uiState.filteredApps
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    if (uiState.searchQuery.isBlank() && uiState.recentApps.isNotEmpty()) {
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(
                                    text = "Recently Selected",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                    items(uiState.recentApps, key = { "recent_${it.packageName}/${it.activityName}" }) { app ->
                                        val key = "${app.packageName}/${app.activityName}"
                                        Surface(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(16.dp))
                                                .border(1.dp, TwinOutline, RoundedCornerShape(16.dp))
                                                .clickable { onAppPicked(app) },
                                            color = MaterialTheme.colorScheme.surface,
                                            shape = RoundedCornerShape(16.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                RealAppIcon(
                                                    app = app,
                                                    iconBitmap = iconMap[key],
                                                    onRequestIcon = onRequestIcon,
                                                    size = 28.dp
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = app.label,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }

                    item {
                        Text(
                            text = "All Apps A–Z (${filtered.size})",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }

                    if (filtered.isEmpty()) {
                        item {
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, TwinOutline, RoundedCornerShape(18.dp)),
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Text(
                                    text = "No installed apps matched '${uiState.searchQuery}'.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(20.dp)
                                )
                            }
                        }
                    } else {
                        items(filtered, key = { "${it.packageName}/${it.activityName}" }) { app ->
                            val key = "${app.packageName}/${app.activityName}"
                            val isFirst = uiState.firstApp?.packageName == app.packageName &&
                                uiState.firstApp?.activityName == app.activityName
                            val isSecond = uiState.secondApp?.packageName == app.packageName &&
                                uiState.secondApp?.activityName == app.activityName

                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(18.dp))
                                    .border(
                                        width = 1.dp,
                                        color = when {
                                            isFirst -> TwinBlue
                                            isSecond -> TwinViolet
                                            else -> TwinOutline
                                        },
                                        shape = RoundedCornerShape(18.dp)
                                    )
                                    .clickable { onAppPicked(app) },
                                color = MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(18.dp)
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RealAppIcon(
                                        app = app,
                                        iconBitmap = iconMap[key],
                                        onRequestIcon = onRequestIcon,
                                        size = 46.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = app.label,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = app.packageName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        if (!app.supportsStandardMultiTask) {
                                            Text(
                                                text = "SingleInstance launch mode — OS may restrict split",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = TwinWarning
                                            )
                                        }
                                    }
                                    if (isFirst || isSecond) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Surface(
                                            color = if (isFirst) TwinBlueSoft else TwinVioletSoft,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.CheckCircle,
                                                    contentDescription = null,
                                                    tint = if (isFirst) TwinBlue else TwinViolet,
                                                    modifier = Modifier.size(14.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (isFirst) "1st" else "2nd",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    color = if (isFirst) TwinBlue else TwinViolet,
                                                    fontWeight = FontWeight.SemiBold
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
