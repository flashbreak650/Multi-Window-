package com.twinspace.app.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

val TwinBlue = Color(0xFF2563EB)
val TwinBlueSoft = Color(0xFFEFF6FF)
val TwinViolet = Color(0xFF7C3AED)
val TwinVioletSoft = Color(0xFFF5F3FF)
val TwinBackground = Color(0xFFF8FAFC)
val TwinSurface = Color(0xFFFFFFFF)
val TwinSurfaceVariant = Color(0xFFF1F5F9)
val TwinOnSurface = Color(0xFF0F172A)
val TwinOnSurfaceMuted = Color(0xFF475569)
val TwinOutline = Color(0xFFE2E8F0)
val TwinSuccess = Color(0xFF16A34A)
val TwinWarning = Color(0xFFD97706)
val TwinError = Color(0xFFDC2626)

private val TwinSpaceLightColorScheme = lightColorScheme(
    primary = TwinBlue,
    onPrimary = Color.White,
    primaryContainer = TwinBlueSoft,
    onPrimaryContainer = Color(0xFF1E3A8A),
    secondary = TwinViolet,
    onSecondary = Color.White,
    secondaryContainer = TwinVioletSoft,
    onSecondaryContainer = Color(0xFF4C1D95),
    background = TwinBackground,
    onBackground = TwinOnSurface,
    surface = TwinSurface,
    onSurface = TwinOnSurface,
    surfaceVariant = TwinSurfaceVariant,
    onSurfaceVariant = TwinOnSurfaceMuted,
    outline = TwinOutline,
    error = TwinError,
    onError = Color.White
)

private val TwinSpaceTypography = Typography(
    headlineMedium = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 32.sp,
        letterSpacing = (-0.5).sp
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 26.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 15.sp,
        lineHeight = 22.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 13.sp,
        lineHeight = 19.sp
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
)

private val TwinSpaceShapes = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp)
)

@Composable
fun TwinSpaceTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = TwinSpaceLightColorScheme,
        typography = TwinSpaceTypography,
        shapes = TwinSpaceShapes,
        content = content
    )
}
