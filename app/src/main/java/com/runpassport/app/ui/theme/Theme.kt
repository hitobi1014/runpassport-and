package com.runpassport.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Blue,
    secondary = Khaki,
    tertiary = LightKhaki,
    background = White,
    surface = LightGray,
    onPrimary = White,
    onSecondary = White,
    onTertiary = Black,
    onBackground = Black,
    onSurface = Black,
)

@Composable
fun RunpassportTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
