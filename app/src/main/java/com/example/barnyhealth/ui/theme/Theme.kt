package com.example.barnyhealth.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val MintPrimary = Color(0xFF5EC6B3)
private val MintDark = Color(0xFF49A999)
private val BackgroundLight = Color(0xFFE8F7F4)
private val SurfaceLight = Color(0xFFFFFFFF)
private val TextDark = Color(0xFF2D3142)

private val DarkColorScheme = darkColorScheme(
    primary = MintPrimary,
    secondary = MintDark,
    tertiary = MintPrimary,
    background = Color(0xFF1E2A28),
    surface = Color(0xFF243330),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = MintPrimary,
    secondary = MintDark,
    tertiary = MintPrimary,
    background = BackgroundLight,
    surface = SurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = TextDark,
    onSurface = TextDark
)

@Composable
fun BarnyHealthTrackerTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val systemBarColor = MintPrimary.toArgb()

            window.statusBarColor = systemBarColor
            window.navigationBarColor = systemBarColor

            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}