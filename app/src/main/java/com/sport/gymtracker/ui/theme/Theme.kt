package com.sport.gymtracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/** Fond très sombre vs surfaces relevées et textes / contours plus francs pour la lisibilité. */
private val DarkBackground = Color(0xFF080B0A)
private val DarkSurface = Color(0xFF151B18)
private val DarkSurfaceVariant = Color(0xFF28332F)
private val DarkOnSurface = Color(0xFFF2F7F4)
private val DarkOnSurfaceVariant = Color(0xFFB8C9C0)
private val DarkOutline = Color(0xFF7A8A82)
private val DarkOutlineVariant = Color(0xFF3D4A44)

/**
 * Thème sombre à contraste renforcé (écart fond / surfaces / texte secondaire / contours).
 */
private val DarkColors = darkColorScheme(
    primary = Color(0xFF8BE08D),
    onPrimary = Color(0xFF00320A),
    primaryContainer = Color(0xFF1B5E20),
    onPrimaryContainer = Color(0xFFD8F8DC),
    secondary = Color(0xFFB9E6B0),
    onSecondary = Color(0xFF08200C),
    secondaryContainer = Color(0xFF2E4A2A),
    onSecondaryContainer = Color(0xFFE8F5E9),
    tertiary = Color(0xFFD5E8D4),
    onTertiary = Color(0xFF0D200D),
    tertiaryContainer = Color(0xFF1E3A1E),
    onTertiaryContainer = Color(0xFFE8F5E9),
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    inverseSurface = Color(0xFFE8F5E9),
    inverseOnSurface = Color(0xFF151B18),
    inversePrimary = Color(0xFF2E7D32),
    surfaceTint = Color(0xFF8BE08D),
)

/** Même logique sur le thème dynamique (API 31+) : couleurs d’accent conservées, surfaces et textes renforcés. */
private fun boostDarkContrast(base: ColorScheme) = base.copy(
    background = DarkBackground,
    onBackground = DarkOnSurface,
    surface = DarkSurface,
    onSurface = DarkOnSurface,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkOnSurfaceVariant,
    outline = DarkOutline,
    outlineVariant = DarkOutlineVariant,
    inverseOnSurface = Color(0xFF151B18),
)

private val LightColors = lightColorScheme(
    primary = Color(0xFF2E7D32),
    secondary = Color(0xFF43A047),
    tertiary = Color(0xFF66BB6A),
    background = Color.White,
    surface = Color.White,
)

@Composable
fun GymTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) {
                boostDarkContrast(dynamicDarkColorScheme(ctx))
            } else {
                dynamicLightColorScheme(ctx).copy(
                    background = Color.White,
                    surface = Color.White,
                )
            }
        }
        darkTheme -> DarkColors
        else -> LightColors
    }
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content,
    )
}
