package com.example.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat

private val HighContrastNavyColorScheme = darkColorScheme(
    primary = SunYellow,
    onPrimary = NavyBackground,
    primaryContainer = SunYellowContainer,
    onPrimaryContainer = SunYellow,
    secondary = PureWhite,
    onSecondary = NavyBackground,
    secondaryContainer = NavySurfaceElevated,
    onSecondaryContainer = PureWhite,
    tertiary = AmberSignal,
    onTertiary = NavyBackground,
    background = NavyBackground,
    onBackground = PureWhite,
    surface = NavySurface,
    onSurface = PureWhite,
    surfaceVariant = NavySurfaceElevated,
    onSurfaceVariant = IceWhite,
    outline = NavyBorder,
    outlineVariant = NavyBorder,
    error = ErrorCoral,
    onError = PureWhite,
    errorContainer = ErrorCoralContainer,
    onErrorContainer = ErrorCoral,
)

private val ParkFinderShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(10.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(22.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(28.dp),
)

@Composable
fun ParkFinderTheme(
    content: @Composable () -> Unit,
) {
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as? Activity)?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
                controller.isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = HighContrastNavyColorScheme,
        typography = Typography,
        shapes = ParkFinderShapes,
        content = content,
    )
}
