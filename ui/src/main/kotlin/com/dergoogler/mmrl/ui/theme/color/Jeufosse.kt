/*
 * Monet, Claude; Autumn at Jeufosse; 1884
 */

package com.dergoogler.mmrl.ui.theme.color

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val primaryLight = Color(0xFF6B5E10)
private val onPrimaryLight = Color(0xFFFFFFFF)
private val primaryContainerLight = Color(0xFFF6E388)
private val onPrimaryContainerLight = Color(0xFF211B00)
private val secondaryLight = Color(0xFF655F40)
private val onSecondaryLight = Color(0xFFFFFFFF)
private val secondaryContainerLight = Color(0xFFEDE3BC)
private val onSecondaryContainerLight = Color(0xFF201C04)
private val tertiaryLight = Color(0xFF426650)
private val onTertiaryLight = Color(0xFFFFFFFF)
private val tertiaryContainerLight = Color(0xFFC4ECD0)
private val onTertiaryContainerLight = Color(0xFF002111)
private val errorLight = Color(0xFFBA1A1A)
private val onErrorLight = Color(0xFFFFFFFF)
private val errorContainerLight = Color(0xFFFFDAD6)
private val onErrorContainerLight = Color(0xFF410002)
private val backgroundLight = Color(0xFFFFF9EC)
private val onBackgroundLight = Color(0xFF1E1C13)
private val surfaceLight = Color(0xFFFFF9EC)
private val onSurfaceLight = Color(0xFF1E1C13)
private val surfaceVariantLight = Color(0xFFE9E2D0)
private val onSurfaceVariantLight = Color(0xFF4A4739)
private val outlineLight = Color(0xFF7C7768)
private val outlineVariantLight = Color(0xFFCCC6B4)
private val scrimLight = Color(0xFF000000)
private val inverseSurfaceLight = Color(0xFF333027)
private val inverseOnSurfaceLight = Color(0xFFF7F0E2)
private val inversePrimaryLight = Color(0xFFD9C76F)
private val surfaceDimLight = Color(0xFFDFDACC)
private val surfaceBrightLight = Color(0xFFFFF9EC)
private val surfaceContainerLowestLight = Color(0xFFFFFFFF)
private val surfaceContainerLowLight = Color(0xFFF9F3E5)
private val surfaceContainerLight = Color(0xFFF4EDDF)
private val surfaceContainerHighLight = Color(0xFFEEE8DA)
private val surfaceContainerHighestLight = Color(0xFFE8E2D4)

private val primaryDark = Color(0xFFD9C76F)
private val onPrimaryDark = Color(0xFF383000)
private val primaryContainerDark = Color(0xFF524700)
private val onPrimaryContainerDark = Color(0xFFF6E388)
private val secondaryDark = Color(0xFFD0C7A2)
private val onSecondaryDark = Color(0xFF363016)
private val secondaryContainerDark = Color(0xFF4D472B)
private val onSecondaryContainerDark = Color(0xFFEDE3BC)
private val tertiaryDark = Color(0xFFA8D0B5)
private val onTertiaryDark = Color(0xFF133724)
private val tertiaryContainerDark = Color(0xFF2B4E39)
private val onTertiaryContainerDark = Color(0xFFC4ECD0)
private val errorDark = Color(0xFFFFB4AB)
private val onErrorDark = Color(0xFF690005)
private val errorContainerDark = Color(0xFF93000A)
private val onErrorContainerDark = Color(0xFFFFDAD6)
private val backgroundDark = Color(0xFF15130C)
private val onBackgroundDark = Color(0xFFE8E2D4)
private val surfaceDark = Color(0xFF15130C)
private val onSurfaceDark = Color(0xFFE8E2D4)
private val surfaceVariantDark = Color(0xFF4A4739)
private val onSurfaceVariantDark = Color(0xFFCCC6B4)
private val outlineDark = Color(0xFF969180)
private val outlineVariantDark = Color(0xFF4A4739)
private val scrimDark = Color(0xFF000000)
private val inverseSurfaceDark = Color(0xFFE8E2D4)
private val inverseOnSurfaceDark = Color(0xFF333027)
private val inversePrimaryDark = Color(0xFF6B5E10)
private val surfaceDimDark = Color(0xFF15130C)
private val surfaceBrightDark = Color(0xFF3C3930)
private val surfaceContainerLowestDark = Color(0xFF100E07)
private val surfaceContainerLowDark = Color(0xFF1E1C13)
private val surfaceContainerDark = Color(0xFF222017)
private val surfaceContainerHighDark = Color(0xFF2C2A21)
private val surfaceContainerHighestDark = Color(0xFF37352B)

val JeufosseLightScheme =
    lightColorScheme(
        primary = primaryLight,
        onPrimary = onPrimaryLight,
        primaryContainer = primaryContainerLight,
        onPrimaryContainer = onPrimaryContainerLight,
        secondary = secondaryLight,
        onSecondary = onSecondaryLight,
        secondaryContainer = secondaryContainerLight,
        onSecondaryContainer = onSecondaryContainerLight,
        tertiary = tertiaryLight,
        onTertiary = onTertiaryLight,
        tertiaryContainer = tertiaryContainerLight,
        onTertiaryContainer = onTertiaryContainerLight,
        error = errorLight,
        onError = onErrorLight,
        errorContainer = errorContainerLight,
        onErrorContainer = onErrorContainerLight,
        background = backgroundLight,
        onBackground = onBackgroundLight,
        surface = surfaceLight,
        onSurface = onSurfaceLight,
        surfaceVariant = surfaceVariantLight,
        onSurfaceVariant = onSurfaceVariantLight,
        outline = outlineLight,
        outlineVariant = outlineVariantLight,
        scrim = scrimLight,
        inverseSurface = inverseSurfaceLight,
        inverseOnSurface = inverseOnSurfaceLight,
        inversePrimary = inversePrimaryLight,
        surfaceDim = surfaceDimLight,
        surfaceBright = surfaceBrightLight,
        surfaceContainerLowest = surfaceContainerLowestLight,
        surfaceContainerLow = surfaceContainerLowLight,
        surfaceContainer = surfaceContainerLight,
        surfaceContainerHigh = surfaceContainerHighLight,
        surfaceContainerHighest = surfaceContainerHighestLight,
    )

val JeufosseDarkScheme =
    darkColorScheme(
        primary = primaryDark,
        onPrimary = onPrimaryDark,
        primaryContainer = primaryContainerDark,
        onPrimaryContainer = onPrimaryContainerDark,
        secondary = secondaryDark,
        onSecondary = onSecondaryDark,
        secondaryContainer = secondaryContainerDark,
        onSecondaryContainer = onSecondaryContainerDark,
        tertiary = tertiaryDark,
        onTertiary = onTertiaryDark,
        tertiaryContainer = tertiaryContainerDark,
        onTertiaryContainer = onTertiaryContainerDark,
        error = errorDark,
        onError = onErrorDark,
        errorContainer = errorContainerDark,
        onErrorContainer = onErrorContainerDark,
        background = backgroundDark,
        onBackground = onBackgroundDark,
        surface = surfaceDark,
        onSurface = onSurfaceDark,
        surfaceVariant = surfaceVariantDark,
        onSurfaceVariant = onSurfaceVariantDark,
        outline = outlineDark,
        outlineVariant = outlineVariantDark,
        scrim = scrimDark,
        inverseSurface = inverseSurfaceDark,
        inverseOnSurface = inverseOnSurfaceDark,
        inversePrimary = inversePrimaryDark,
        surfaceDim = surfaceDimDark,
        surfaceBright = surfaceBrightDark,
        surfaceContainerLowest = surfaceContainerLowestDark,
        surfaceContainerLow = surfaceContainerLowDark,
        surfaceContainer = surfaceContainerDark,
        surfaceContainerHigh = surfaceContainerHighDark,
        surfaceContainerHighest = surfaceContainerHighestDark,
    )
