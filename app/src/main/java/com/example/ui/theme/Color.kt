package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Primary brand colors for SUYAIB SOCIAL
val IndigoPrimary = Color(0xFF5B4DFF)
val IndigoDark = Color(0xFF4338CA)
val VioletAccent = Color(0xFF8B5CF6)
val PinkAccent = Color(0xFFEC4899)
val AmberAccent = Color(0xFFF59E0B)
val EmeraldSuccess = Color(0xFF10B981)
val CrimsonError = Color(0xFFEF4444)

// Dark Palette
val DarkBackground = Color(0xFF0C0E17)
val DarkSurface = Color(0xFF141824)
val DarkSurfaceVariant = Color(0xFF1D2336)
val DarkBorder = Color(0xFF28314A)
val DarkTextPrimary = Color(0xFFF1F5F9)
val DarkTextSecondary = Color(0xFF94A3B8)
val DarkTextMuted = Color(0xFF64748B)

// Light Palette
val LightBackground = Color(0xFFF8FAFC)
val LightSurface = Color(0xFFFFFFFF)
val LightSurfaceVariant = Color(0xFFF1F5F9)
val LightBorder = Color(0xFFE2E8F0)
val LightTextPrimary = Color(0xFF0F172A)
val LightTextSecondary = Color(0xFF475569)
val LightTextMuted = Color(0xFF94A3B8)

// Vibrant gradients for Stories and Badges
val SuyaibGradient = Brush.horizontalGradient(
    colors = listOf(
        Color(0xFF5B4DFF),
        Color(0xFF8B5CF6),
        Color(0xFFEC4899)
    )
)

val StoryRingGradient = Brush.sweepGradient(
    colors = listOf(
        Color(0xFFEC4899),
        Color(0xFF8B5CF6),
        Color(0xFF3B82F6),
        Color(0xFF10B981),
        Color(0xFFEC4899)
    )
)

val CardGlowGradient = Brush.verticalGradient(
    colors = listOf(
        Color(0xFF5B4DFF).copy(alpha = 0.15f),
        Color.Transparent
    )
)
