package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = EmeraldWin,
    onPrimary = Color.Black,
    primaryContainer = StadiumCard,
    onPrimaryContainer = EmeraldWinLight,
    secondary = CyanOdds,
    onSecondary = Color.Black,
    secondaryContainer = StadiumSurface,
    onSecondaryContainer = CyanOddsLight,
    tertiary = AmberVIP,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = StadiumBorder,
    error = CrimsonLoss,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EmeraldWinDark,
    onPrimary = Color.White,
    primaryContainer = EmeraldWinLight,
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = CyanOdds,
    onSecondary = Color.White,
    secondaryContainer = CyanOddsLight,
    onSecondaryContainer = Color(0xFF075985),
    tertiary = AmberVIPDark,
    onTertiary = Color.White,
    background = LightBackground,
    onBackground = LightTextPrimary,
    surface = LightSurface,
    onSurface = LightTextPrimary,
    surfaceVariant = LightSurfaceVariant,
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFCBD5E1),
    error = CrimsonLoss,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our tailored sports analytics palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

