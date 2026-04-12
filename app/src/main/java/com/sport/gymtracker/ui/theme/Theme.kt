package com.sport.gymtracker.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColors = darkColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF81C784),
    secondary = androidx.compose.ui.graphics.Color(0xFFA5D6A7),
    tertiary = androidx.compose.ui.graphics.Color(0xFFC8E6C9),
)

private val LightColors = lightColorScheme(
    primary = androidx.compose.ui.graphics.Color(0xFF2E7D32),
    secondary = androidx.compose.ui.graphics.Color(0xFF43A047),
    tertiary = androidx.compose.ui.graphics.Color(0xFF66BB6A),
)

@Composable
fun GymTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val ctx = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(ctx) else dynamicLightColorScheme(ctx)
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
