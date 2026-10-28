package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val IslamicDarkColorScheme = darkColorScheme(
    primary = GoldAccent,
    onPrimary = EmeraldDeep,
    primaryContainer = EmeraldContainer,
    onPrimaryContainer = GoldSoft,
    secondary = EmeraldAccent,
    onSecondary = Color.White,
    secondaryContainer = CharcoalCard,
    onSecondaryContainer = TextLightPrimary,
    tertiary = GoldPrimary,
    onTertiary = EmeraldDeep,
    background = CharcoalBackground,
    onBackground = TextLightPrimary,
    surface = CharcoalSurface,
    onSurface = TextLightPrimary,
    surfaceVariant = CharcoalSurfaceVariant,
    onSurfaceVariant = TextLightSecondary,
    outline = CharcoalCardBorder
)

private val IslamicLightColorScheme = lightColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color.White,
    primaryContainer = EmeraldSoftBg,
    onPrimaryContainer = EmeraldDark,
    secondary = GoldPrimary,
    onSecondary = Color.White,
    secondaryContainer = GoldSoft,
    onSecondaryContainer = EmeraldDark,
    tertiary = EmeraldAccent,
    onTertiary = Color.White,
    background = MinimalWhiteBackground,
    onBackground = TextDarkPrimary,
    surface = MinimalWhiteSurface,
    onSurface = TextDarkPrimary,
    surfaceVariant = MinimalWhiteSurfaceVariant,
    onSurfaceVariant = TextDarkSecondary,
    outline = MinimalWhiteCardBorder
)

@Composable
fun AmalTrackerTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) IslamicDarkColorScheme else IslamicLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

