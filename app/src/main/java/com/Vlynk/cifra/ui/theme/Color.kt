package com.Vlynk.cifra.ui.theme

import androidx.compose.ui.graphics.Color

// Cifra Design System Palette (from Theme.css)
val ColorVoid = Color(0xFF08090A)
val ColorCarbon = Color(0xFF0F1011)
val ColorObsidian = Color(0xFF161718)
val ColorGraphite = Color(0xFF23252A)
val ColorSmoke = Color(0xFF383B3F)
val ColorAsh = Color(0xFF62666D)
val ColorFog = Color(0xFF8A8F98)
val ColorMist = Color(0xFFD0D6E0)
val ColorBone = Color(0xFFE5E5E6)
val ColorPaper = Color(0xFFFFFFFF)

// Accent Colors
val ColorAcidLime = Color(0xFFE4F222)
val ColorPulseGreen = Color(0xFF27A644)
val ColorCoralRed = Color(0xFFEB5757)
val ColorSignalTeal = Color(0xFF02B8CC)
val ColorIrisViolet = Color(0xFF6366F1)
val ColorLavender = Color(0xFF8B5CF6)

// Semantic Theme Mappings
val PrimaryDark = ColorAcidLime
val OnPrimaryDark = ColorVoid
val PrimaryContainerDark = ColorGraphite
val OnPrimaryContainerDark = ColorBone

val SecondaryDark = ColorMist
val OnSecondaryDark = ColorVoid
val SecondaryContainerDark = ColorObsidian
val OnSecondaryContainerDark = ColorBone

val TertiaryDark = ColorSignalTeal
val OnTertiaryDark = ColorVoid
val TertiaryContainerDark = ColorGraphite
val OnTertiaryContainerDark = ColorMist

val BackgroundDark = ColorVoid
val OnBackgroundDark = ColorPaper
val SurfaceDark = ColorObsidian
val OnSurfaceDark = ColorBone
val SurfaceVariantDark = ColorCarbon
val OnSurfaceVariantDark = ColorFog

val ErrorDark = ColorCoralRed
val OnErrorDark = ColorPaper

// Keypad Colors
val KeypadNumberBg = ColorObsidian
val KeypadNumberText = ColorPaper
val KeypadOperatorBg = ColorGraphite
val KeypadOperatorText = ColorMist
val KeypadActionBg = ColorObsidian
val KeypadActionText = ColorMist
val KeypadEqualsBg = ColorAcidLime
val KeypadEqualsText = ColorVoid
