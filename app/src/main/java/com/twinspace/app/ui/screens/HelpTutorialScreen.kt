package com.twinspace.app.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.twinspace.app.ui.theme.TwinOutline

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpTutorialScreen(onBack: () -> Unit) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("Help & Tutorial", style = MaterialTheme.typography.titleLarge) },
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
                HelpSectionCard(
                    title = "1. How Real Android Split Screen Works",
                    body = "Android manages window resizing and the split divider at the system level. On Android 10–15+, FLAG_ACTIVITY_LAUNCH_ADJACENT opens your second app in the neighboring pane when TwinSpace is already pinned in Split Screen (or on tablets/foldables)."
                )
            }
            item {
                HelpSectionCard(
                    title = "2. Step-by-Step on Android Phones",
                    body = "1) Select First App and Second App in TwinSpace.\n2) Open Android Recents (swipe up from the bottom bar and hold).\n3) Tap the TwinSpace app icon above its preview card and pick 'Split screen' or 'Split top'.\n4) Return to TwinSpace in the split pane and tap OPEN BOTH.\n5) Drag the system divider bar anytime to resize either app."
                )
            }
            item {
                HelpSectionCard(
                    title = "3. Why Some Apps Refuse Split Screen",
                    body = "Certain apps (for example, games, DRM video players, or banking apps) set android:resizeableActivity=\"false\" or singleInstance in their manifest. TwinSpace respects Android OS security rules and reports when an app cannot stack."
                )
            }
            item {
                HelpSectionCard(
                    title = "4. Why TwinSpace Never Uses Accessibility Shortcuts",
                    body = "Some utilities request Accessibility Service to inject simulated taps on Recents. TwinSpace deliberately avoids Accessibility Service and network permissions to keep your device private and Play-policy compliant."
                )
            }
        }
    }
}

@Composable
private fun HelpSectionCard(title: String, body: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, TwinOutline, RoundedCornerShape(20.dp)),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
