package app.test.myapplication.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF4F46E5),        // Indigo 600
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE0E7FF),// Indigo 100
    onPrimaryContainer = Color(0xFF312E81),
    secondary = Color(0xFF0EA5E9),      // Sky 500
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFE0F2FE),
    onSecondaryContainer = Color(0xFF0369A1),
    tertiary = Color(0xFF10B981),       // Emerald 500
    onTertiary = Color.White,
    background = Color(0xFFF8FAFC),     // Slate 50
    onBackground = Color(0xFF0F172A),   // Slate 900
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),  // Slate 100
    onSurfaceVariant = Color(0xFF475569) // Slate 600
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF818CF8),        // Indigo 400
    onPrimary = Color(0xFF1E1B4B),
    primaryContainer = Color(0xFF3730A3),
    onPrimaryContainer = Color(0xFFE0E7FF),
    secondary = Color(0xFF38BDF8),
    onSecondary = Color(0xFF0C4A6E),
    secondaryContainer = Color(0xFF0369A1),
    onSecondaryContainer = Color(0xFFE0F2FE),
    tertiary = Color(0xFF34D399),
    onTertiary = Color(0xFF064E3B),
    background = Color(0xFF0F172A),     // Slate 900
    onBackground = Color(0xFFF8FAFC),   // Slate 50
    surface = Color(0xFF1E293B),        // Slate 800
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),  // Slate 700
    onSurfaceVariant = Color(0xFF94A3B8)
)

@Composable
fun ReqResTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
