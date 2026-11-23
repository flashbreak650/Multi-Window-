package com.twinspace.app.data

import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.LruCache
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import com.twinspace.app.model.InstalledApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Discovers real launchable applications using Android's PackageManager
 * and caches decoded application icons in memory.
 */
class InstalledAppsRepository(context: Context) {

    private val appContext = context.applicationContext
    private val packageManager: PackageManager = appContext.packageManager
    private val selfPackageName: String = appContext.packageName

    private val iconCache = object : LruCache<String, ImageBitmap>(120) {}

    suspend fun loadLaunchableApps(): List<InstalledApp> = withContext(Dispatchers.IO) {
        val launcherIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val resolveInfos: List<ResolveInfo> = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                launcherIntent,
                PackageManager.ResolveInfoFlags.of(PackageManager.MATCH_ALL.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.queryIntentActivities(launcherIntent, PackageManager.MATCH_ALL)
        }

        val seenComponents = HashSet<String>()
        val apps = ArrayList<InstalledApp>(resolveInfos.size)

        for (info in resolveInfos) {
            val activityInfo = info.activityInfo ?: continue
            val pkg = activityInfo.packageName ?: continue
            if (pkg == selfPackageName) continue

            val componentKey = "$pkg/${activityInfo.name}"
            if (!seenComponents.add(componentKey)) continue

            val label = info.loadLabel(packageManager)?.toString()?.trim().orEmpty()
            if (label.isEmpty()) continue

            val isSystem = (activityInfo.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val supportsMultiTask = activityInfo.launchMode != ActivityInfo.LAUNCH_SINGLE_INSTANCE

            apps.add(
                InstalledApp(
                    packageName = pkg,
                    activityName = activityInfo.name,
                    label = label,
                    isSystemApp = isSystem,
                    launchMode = activityInfo.launchMode,
                    supportsStandardMultiTask = supportsMultiTask
                )
            )
        }

        sortAlphabetically(apps)
    }

    suspend fun loadAppIcon(app: InstalledApp): ImageBitmap? = withContext(Dispatchers.IO) {
        val cacheKey = "${app.packageName}/${app.activityName}"
        iconCache.get(cacheKey)?.let { return@withContext it }

        val drawable: Drawable = try {
            packageManager.getActivityIcon(app.componentName)
        } catch (_: PackageManager.NameNotFoundException) {
            try {
                packageManager.getApplicationIcon(app.packageName)
            } catch (_: PackageManager.NameNotFoundException) {
                return@withContext null
            }
        }

        val bitmap = drawableToBitmap(drawable, targetSizePx = 128)
        val imageBitmap = bitmap.asImageBitmap()
        iconCache.put(cacheKey, imageBitmap)
        imageBitmap
    }

    private fun drawableToBitmap(drawable: Drawable, targetSizePx: Int): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            val src = drawable.bitmap
            if (src.width <= targetSizePx && src.height <= targetSizePx) {
                return src
            }
            return Bitmap.createScaledBitmap(src, targetSizePx, targetSizePx, true)
        }

        val width = drawable.intrinsicWidth.takeIf { it > 0 }?.coerceAtMost(targetSizePx) ?: targetSizePx
        val height = drawable.intrinsicHeight.takeIf { it > 0 }?.coerceAtMost(targetSizePx) ?: targetSizePx
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        fun sortAlphabetically(apps: List<InstalledApp>): List<InstalledApp> {
            return apps.sortedWith(
                compareBy<InstalledApp> { it.label.lowercase() }
                    .thenBy { it.packageName.lowercase() }
            )
        }

        fun filterApps(apps: List<InstalledApp>, query: String): List<InstalledApp> {
            val trimmed = query.trim().lowercase()
            if (trimmed.isEmpty()) return apps
            return apps.filter { app ->
                app.label.lowercase().contains(trimmed) ||
                    app.packageName.lowercase().contains(trimmed)
            }
        }
    }
}
