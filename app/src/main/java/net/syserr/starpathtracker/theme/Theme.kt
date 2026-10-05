package net.syserr.starpathtracker.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = StarlightGold,
    onPrimary = Color(0xFF261900),
    primaryContainer = Color(0xFF422E00),
    onPrimaryContainer = StarlightGoldLight,

    secondary = DreamlightCyan,
    onSecondary = Color(0xFF00363D),
    secondaryContainer = Color(0xFF004F58),
    onSecondaryContainer = DreamlightCyanLight,

    tertiary = DreamlightPurple,
    onTertiary = Color(0xFF2E0066),
    tertiaryContainer = Color(0xFF450099),
    onTertiaryContainer = Color(0xFFE8DDFF),

    background = CelestialBackground,
    onBackground = TextPrimary,

    surface = CelestialSurface,
    onSurface = TextPrimary,
    surfaceVariant = CelestialCard,
    onSurfaceVariant = TextSecondary,

    outline = CelestialCardBorder,
    outlineVariant = Color(0xFF1E2849),

    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainer,
    onErrorContainer = Color(0xFFFFDAD6)
)

private val LightColorScheme = DarkColorScheme // Star Path is naturally a celestial nighttime experience

@Composable
fun StarPathTrackerTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}
