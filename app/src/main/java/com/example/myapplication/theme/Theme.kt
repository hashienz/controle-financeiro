package com.example.myapplication.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val LumeColorScheme = darkColorScheme(
    primary = LumeAccent,
    onPrimary = LumeBg,
    background = LumeBg,
    onBackground = LumeTextPrimary,
    surface = LumeSurface,
    onSurface = LumeTextPrimary,
    surfaceVariant = LumeSurface,
    onSurfaceVariant = LumeTextSecondary,
    outline = LumeBorder,
    error = LumeError
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LumeColorScheme,
        typography = Typography,
        content = content
    )
}
