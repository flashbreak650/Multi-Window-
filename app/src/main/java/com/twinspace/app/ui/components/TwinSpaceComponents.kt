package com.twinspace.app.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Splitscreen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.twinspace.app.model.InstalledApp
import com.twinspace.app.model.LaunchResult
import com.twinspace.app.ui.theme.TwinBlue
import com.twinspace.app.ui.theme.TwinError
import com.twinspace.app.ui.theme.TwinOutline
import com.twinspace.app.ui.theme.TwinSuccess
import com.twinspace.app.ui.theme.TwinSurfaceVariant
import com.twinspace.app.ui.theme.TwinViolet
import com.twinspace.app.ui.theme.TwinWarning

@Composable
fun TwinSpaceLogo(modifier: Modifier = Modifier, size: Dp = 44.dp) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(14.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(TwinBlue, TwinViolet)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.58f)) {
            val w = this.size.width
            val h = this.size.height
            val gap = w * 0.12f
            val paneW = (w - gap) / 2f
            val corner = CornerRadius(w * 0.12f, w * 0.12f)

            drawRoundRect(
                color = Color.White,
                topLeft = Offset(0f, 0f),
                size = Size(paneW, h),
                cornerRadius = corner
            )
            drawRoundRect(
                color = Color(0xFFE0E7FF),
                topLeft = Offset(paneW + gap, 0f),
                size = Size(paneW, h),
                cornerRadius = corner
            )
        }
    }
}

@Composable
fun RealAppIcon(
    app: InstalledApp,
    iconBitmap: ImageBitmap?,
    onRequestIcon: (InstalledApp) -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp
) {
    LaunchedEffect(app.packageName, app.activityName) {
        if (iconBitmap == null) {
            onRequestIcon(app)
        }
    }

    if (iconBitmap != null) {
        Image(
            bitmap = iconBitmap,
            contentDescription = "${app.label} icon",
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(14.dp))
        )
    } else {
        Box(
            modifier = modifier
                .size(size)
                .clip(RoundedCornerShape(14.dp))
                .background(TwinSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = app.label.take(1).uppercase(),
                style = MaterialTheme.typography.titleMedium,
                color = TwinBlue
            )
        }
    }
}

@Composable
fun SelectedAppSlotCard(
    slotTitle: String,
    accentColor: Color,
    softAccentColor: Color,
    selectedApp: InstalledApp?,
    iconBitmap: ImageBitmap?,
    onRequestIcon: (InstalledApp) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = 1.dp,
            color = if (selectedApp != null) accentColor.copy(alpha = 0.35f) else TwinOutline
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (selectedApp != null) {
                RealAppIcon(
                    app = selectedApp,
                    iconBitmap = iconBitmap,
                    onRequestIcon = onRequestIcon,
                    size = 52.dp
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(softAccentColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Select $slotTitle",
                        tint = accentColor
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = slotTitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = accentColor,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = selectedApp?.label ?: "Tap to choose installed app",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (selectedApp != null) {
                    Text(
                        text = selectedApp.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = "Open $slotTitle list",
                tint = accentColor
            )
        }
    }
}

@Composable
fun PrivacyDisclosureDialog(
    onAccept: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onAccept,
        icon = {
            Icon(
                imageVector = Icons.Rounded.Security,
                contentDescription = null,
                tint = TwinBlue,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = "App Discovery & Privacy",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "TwinSpace queries installed launchable apps on your device using Android's PackageManager so you can pair any two apps for Split Screen.",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "• Zero internet permission: TwinSpace does not request INTERNET access and cannot upload your app list.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "• No Accessibility tracking: TwinSpace never uses Accessibility Service to inspect or control other apps.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = TwinBlue)
            ) {
                Text("Continue")
            }
        }
    )
}

@Composable
fun LaunchOutcomeDialog(
    result: LaunchResult,
    onDismiss: () -> Unit,
    onForceDirectAttempt: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            val (icon, tint) = when (result) {
                is LaunchResult.LaunchedSplitPair -> Icons.Rounded.CheckCircle to TwinSuccess
                is LaunchResult.RequiresManualSplitSetup -> Icons.Rounded.Splitscreen to TwinViolet
                is LaunchResult.PartialOrWarning -> Icons.Rounded.Info to TwinWarning
                is LaunchResult.Failed -> Icons.Rounded.ErrorOutline to TwinError
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            val titleText = when (result) {
                is LaunchResult.LaunchedSplitPair -> "Split Pair Requested"
                is LaunchResult.RequiresManualSplitSetup -> "Enter Split Screen First"
                is LaunchResult.PartialOrWarning -> result.title
                is LaunchResult.Failed -> result.title
            }
            Text(text = titleText, style = MaterialTheme.typography.titleLarge)
        },
        text = {
            when (result) {
                is LaunchResult.LaunchedSplitPair -> {
                    Text(result.modeDescription, style = MaterialTheme.typography.bodyLarge)
                }

                is LaunchResult.RequiresManualSplitSetup -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = result.reason,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        result.steps.forEachIndexed { index, step ->
                            Text(
                                text = "${index + 1}. $step",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                is LaunchResult.PartialOrWarning -> {
                    Text(result.message, style = MaterialTheme.typography.bodyLarge)
                }

                is LaunchResult.Failed -> {
                    Text(result.reason, style = MaterialTheme.typography.bodyLarge)
                }
            }
        },
        confirmButton = {
            if (result is LaunchResult.RequiresManualSplitSetup) {
                Button(
                    onClick = {
                        onDismiss()
                        onForceDirectAttempt()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TwinBlue)
                ) {
                    Text("Try Direct Launch Anyway")
                }
            } else {
                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = TwinBlue)
                ) {
                    Text("OK")
                }
            }
        },
        dismissButton = {
            if (result is LaunchResult.RequiresManualSplitSetup) {
                OutlinedButton(onClick = onDismiss) {
                    Text("Got It")
                }
            }
        }
    )
}
