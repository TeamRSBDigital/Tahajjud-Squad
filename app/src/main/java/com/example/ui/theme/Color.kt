package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ============================================================================
// Tahajjud Squad - Minimal Clean White & Vibrant Islamic Accents Design System
// Strictly complies with:
// - Primary: Emerald Green #123C2D
// - Secondary / Accent: Royal Gold #D4AF57
// - Surface: Clean White #FFFFFF / Warm White #FFFDF5
// - Text / Contrast: High-Contrast Deep Charcoal #141F1A
// ============================================================================

// Primary Emerald Green Palette
val EmeraldDeep = Color(0xFF0A261D)          // Deep Emerald Banner
val EmeraldDark = Color(0xFF123C2D)          // Primary Emerald Dark #123C2D
val EmeraldPrimary = Color(0xFF14532D)       // Vibrant Islamic Emerald #14532D
val EmeraldAccent = Color(0xFF16A34A)        // Fresh Emerald Green #16A34A
val EmeraldLight = Color(0xFF22C55E)         // Bright Green Highlight
val EmeraldSoftBg = Color(0xFFDCFCE7)        // Soft Mint/Emerald Container Wash
val EmeraldContainer = Color(0xFF14532D)     // Deep Emerald Button Container
val EmeraldBorder = Color(0xFF86EFAC)        // Crisp Emerald Border

// Royal Gold Secondary & Accent Palette #D4AF57
val GoldPrimary = Color(0xFFD4AF37)         // Royal Gold Accent #D4AF37
val GoldAccent = Color(0xFFEAB308)          // Radiant Gold Accent
val GoldDark = Color(0xFFB45309)            // Deep Accessible Amber/Gold Text
val GoldMuted = Color(0xFFCA8A04)           // Muted Gold
val GoldSoft = Color(0xFFFEF9C3)            // Warm Gold Container Wash
val GoldBorder = Color(0xFFFDE047)          // Crisp Gold Border

// Clean Minimal White Surfaces
val MinimalWhiteBackground = Color(0xFFF8FAF8) // Airy, clean soft-white page background
val MinimalWhiteSurface = Color(0xFFFFFFFF)    // Pure Crisp White Surface
val MinimalWhiteSurfaceVariant = Color(0xFFF3F5F4) // Subtle elevated card surface
val MinimalWhiteCardBorder = Color(0xFFE5EBE7) // Crisp delicate border

// Soft Lavender Aura & Multi-Metric Palette (Inspired by Reference UI)
val LavenderAuraStart = Color(0xFFEDE9FE)      // Soft Lavender glow
val LavenderAuraMid = Color(0xFFF5F3FF)        // Light Lilac mist
val LavenderAuraEnd = Color(0xFFFBFBFE)        // Near white transition
val LavenderAccent = Color(0xFF8B5CF6)         // Subtle purple accent for charts
val LavenderSoftBg = Color(0xFFF3F0FF)         // Soft lavender pill background
val LavenderBorder = Color(0xFFDDD6FE)         // Delicate lavender border

// Multi-metric circular arc colors (Matching reference image rings)
val MetricOrange = Color(0xFFF97316)           // Amber/Orange for Salah
val MetricOrangeSoft = Color(0xFFFED7AA)
val MetricPurple = Color(0xFF8B5CF6)           // Purple for Tahajjud
val MetricPurpleSoft = Color(0xFFDDD6FE)
val MetricGreen = Color(0xFF10B981)            // Green for Dhikr/Amal
val MetricGreenSoft = Color(0xFFA7F3D0)

// Aliases for compatibility
val WarmWhiteBackground = MinimalWhiteBackground
val WarmWhiteSurface = MinimalWhiteSurface
val WarmWhiteSurfaceVariant = MinimalWhiteSurfaceVariant
val WarmWhiteCardBorder = MinimalWhiteCardBorder
val IvoryBackground = MinimalWhiteBackground
val IvorySurface = MinimalWhiteSurface
val IvorySurfaceVariant = MinimalWhiteSurfaceVariant
val CardBorderLight = MinimalWhiteCardBorder

// High-Contrast Typography (Crisp, highly legible on white surfaces)
val TextDarkPrimary = Color(0xFF111815)      // Jet Dark Charcoal (Ultra high contrast)
val TextDarkSecondary = Color(0xFF374151)    // Clearly readable slate charcoal
val TextDarkMuted = Color(0xFF6B7280)        // Readable muted gray

// Text on Dark Emerald surfaces
val TextLightPrimary = Color(0xFFFFFFFF)     // Crisp Pure White text
val TextLightSecondary = Color(0xFFE2E8F0)   // Light Slate text
val TextLightMuted = Color(0xFF94A3B8)

// Status Colors (Vibrant, colorful & clear)
val StatusPerformed = Color(0xFF16A34A)      // Vibrant Green (Completed)
val StatusPerformedLight = Color(0xFFDCFCE7) // Fresh Mint Green Pill
val StatusMissed = Color(0xFFDC2626)         // Clear Semantic Red (Missed)
val StatusMissedLight = Color(0xFFFEE2E2)    // Light Red Pill
val StatusQaza = Color(0xFFD97706)           // Vibrant Amber/Gold (Qaza)
val StatusQazaLight = Color(0xFFFEF3C7)      // Light Amber Pill
val StatusUnrecorded = Color(0xFF64748B)     // Cool Slate (Unrecorded)
val StatusUnrecordedLight = Color(0xFFF1F5F9)// Light Slate Pill

// Dark Theme Fallbacks (High contrast if dark mode is active)
val CharcoalBackground = Color(0xFF0D1612)
val CharcoalSurface = Color(0xFF16251E)
val CharcoalSurfaceVariant = Color(0xFF1E332A)
val CharcoalCard = Color(0xFF16251E)
val CharcoalCardBorder = Color(0xFF28483B)
