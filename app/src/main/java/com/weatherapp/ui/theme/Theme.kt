package com.weatherapp.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val GrungeColors = darkColorScheme(
    primary = Color(0xFFD8A657),
    onPrimary = Color(0xFF211A12),
    secondary = Color(0xFFB7B09B),
    onSecondary = Color(0xFF1B1A17),
    tertiary = Color(0xFFB56B5D),
    background = Color(0xFF171614),
    onBackground = Color(0xFFE7E0D0),
    surface = Color(0xFF24221E),
    onSurface = Color(0xFFE7E0D0),
    surfaceVariant = Color(0xFF36322B),
    onSurfaceVariant = Color(0xFFBEB5A4),
    error = Color(0xFFE47B6B)
)

@Composable
fun WeatherAppTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(colorScheme = GrungeColors, typography = Typography, content = content)
}
