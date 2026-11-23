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
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.twinspace.app.model.DeviceCompatibility
import com.twinspace.app.ui.theme.TwinBlue
import com.twinspace.app.ui.theme.TwinError
import com.twinspace.app.ui.theme.TwinOutline
import com.twinspace.app.ui.theme.TwinSuccess
import com.twinspace.app.ui.theme.TwinWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompatibilityScreen(
    compatibility: DeviceCompatibility?,
    onRefresh: () -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Device Compatibility", style = MaterialTheme.typography.titleLarge) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Rounded.Refresh, contentDescription = "Re-inspect device")
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (compatibility != null) {
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
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text("System Hardware & OS", style = MaterialTheme.typography.titleMedium)
                            CompatRow("Android Version", compatibility.androidVersionLabel, TwinBlue)
                            HorizontalDivider(color = TwinOutline)
                            CompatRow("Device", "${compatibility.manufacturer} ${compatibility.model}", MaterialTheme.colorScheme.onSurface)
                            HorizontalDivider(color = TwinOutline)
                            CompatRow(
                                "Native Split Screen",
                                if (compatibility.supportsSplitScreen) "Supported" else "Unavailable (Low-RAM)",
                                if (compatibility.supportsSplitScreen) TwinSuccess else TwinError
                            )
                            HorizontalDivider(color = TwinOutline)
                            CompatRow(
                                "Current Window State",
                                if (compatibility.isCurrentlyInMultiWindowMode) "In Multi-Window Mode" else "Full Screen",
                                if (compatibility.isCurrentlyInMultiWindowMode) TwinSuccess else TwinWarning
                            )
                            HorizontalDivider(color = TwinOutline)
                            CompatRow(
                                "Freeform Windows",
                                if (compatibility.supportsFreeformWindows) "Hardware Feature Present" else "Not Advertised by OS",
                                if (compatibility.supportsFreeformWindows) TwinSuccess else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            HorizontalDivider(color = TwinOutline)
                            CompatRow(
                                "Smallest Screen Width",
                                "${compatibility.smallestScreenWidthDp} dp (${if (compatibility.isLargeScreen) "Tablet / Foldable" else "Phone"})",
                                MaterialTheme.colorScheme.onSurface
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Android Version Behavior", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = compatibility.osVersionGuidance,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        Column(modifier = Modifier.padding(18.dp)) {
                            Text("Manufacturer Notes", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = compatibility.oemNotes,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CompatRow(label: String, value: String, valueColor: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.SemiBold,
            color = valueColor
        )
    }
}
