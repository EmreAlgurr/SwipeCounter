package com.emre.swipecounter.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Force Dark Theme with Midnight Palette
private val DarkColorScheme = darkColorScheme(
    primary = VividAccent,
    onPrimary = BlackBackground,
    primaryContainer = BlackSurface,
    onPrimaryContainer = TextPrimary,
    
    secondary = VividAccent,
    onSecondary = BlackBackground,
    secondaryContainer = BlackSurface,
    onSecondaryContainer = TextSecondary,
    
    tertiary = VividError,
    onTertiary = BlackBackground,
    tertiaryContainer = BlackSurface,
    onTertiaryContainer = TextPrimary,
    
    background = BlackBackground,
    onBackground = TextPrimary,
    
    surface = BlackBackground, // Default surface is black
    onSurface = TextPrimary,
    surfaceVariant = BlackSurface, // Cards/Containers use this
    onSurfaceVariant = TextSecondary,
    
    error = VividError,
    onError = BlackBackground
)

// We don't really support light theme anymore, but just incase
private val LightColorScheme = DarkColorScheme

@Composable
fun SwipeCounterTheme(
    darkTheme: Boolean = true, // Force dark theme by default
    // We ignore dynamic color to enforce our branding
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            // Ensure icons are light on dark background
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
