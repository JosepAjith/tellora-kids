package com.joseph.tellorakids.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    onPrimaryContainer = OnPrimaryContainerDark,
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    onSecondaryContainer = OnSecondaryContainerDark,
    background = BackgroundDark,
    onBackground = OnBackgroundDark,
    surface = SurfaceDark,
    onSurface = OnSurfaceDark,
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryVibrant,
    onPrimary = OnPrimaryVibrant,
    primaryContainer = PrimaryContainerVibrant,
    onPrimaryContainer = OnPrimaryContainerVibrant,
    secondary = SecondaryVibrant,
    onSecondary = OnSecondaryVibrant,
    secondaryContainer = SecondaryContainerVibrant,
    onSecondaryContainer = OnSecondaryContainerVibrant,
    tertiary = TertiaryVibrant,
    onTertiary = OnTertiaryVibrant,
    tertiaryContainer = TertiaryContainerVibrant,
    onTertiaryContainer = OnTertiaryContainerVibrant,
    background = BackgroundVibrant,
    onBackground = OnBackgroundVibrant,
    surface = SurfaceVibrant,
    onSurface = OnSurfaceVibrant,
)

@Composable
fun TelloraKidsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // With enableEdgeToEdge(), we don't set status bar color manually here, 
            // but we ensure the status bar icons are readable.
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
