package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

enum class StoreThemePalette(val labelArabic: String, val primaryHex: String) {
  GOLD("الذهبي الملكي", "#D4AF37"),
  EMERALD("الزمردي النيون", "#10B981"),
  CRIMSON("أحمر السباقات", "#E11D48"),
  CARBON("الكربوني الفضي", "#94A3B8")
}

private fun getDarkScheme(palette: StoreThemePalette): ColorScheme {
  val primary = when (palette) {
    StoreThemePalette.GOLD -> GoldPrimary
    StoreThemePalette.EMERALD -> EmeraldPrimary
    StoreThemePalette.CRIMSON -> CrimsonPrimary
    StoreThemePalette.CARBON -> Color(0xFFE2E8F0)
  }
  val secondary = when (palette) {
    StoreThemePalette.GOLD -> GoldSecondary
    StoreThemePalette.EMERALD -> EmeraldSecondary
    StoreThemePalette.CRIMSON -> CrimsonSecondary
    StoreThemePalette.CARBON -> Color(0xFFCBD5E1)
  }
  val bg = when (palette) {
    StoreThemePalette.EMERALD -> EmeraldBg
    StoreThemePalette.CRIMSON -> CarbonBg
    StoreThemePalette.CARBON -> Color(0xFF0F172A)
    StoreThemePalette.GOLD -> DarkNavyBg
  }
  val surface = when (palette) {
    StoreThemePalette.EMERALD -> EmeraldSurface
    StoreThemePalette.CRIMSON -> CarbonSurface
    StoreThemePalette.CARBON -> Color(0xFF1E293B)
    StoreThemePalette.GOLD -> DarkNavySurface
  }

  return darkColorScheme(
    primary = primary,
    onPrimary = Color.Black,
    primaryContainer = primary.copy(alpha = 0.2f),
    onPrimaryContainer = primary,
    secondary = secondary,
    onSecondary = Color.Black,
    background = bg,
    onBackground = TextLightPrimary,
    surface = surface,
    onSurface = TextLightPrimary,
    surfaceVariant = surface.copy(alpha = 0.85f),
    onSurfaceVariant = TextLightSecondary,
    outline = TextLightSecondary.copy(alpha = 0.3f)
  )
}

@Composable
fun SmartStoreTheme(
  themePalette: StoreThemePalette = StoreThemePalette.GOLD,
  darkTheme: Boolean = true,
  content: @Composable () -> Unit,
) {
  val colorScheme = getDarkScheme(themePalette)

  MaterialTheme(
    colorScheme = colorScheme,
    typography = Typography,
    content = content
  )
}
