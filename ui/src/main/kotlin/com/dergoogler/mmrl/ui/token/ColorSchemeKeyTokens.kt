package com.dergoogler.mmrl.ui.token

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.LocalTonalElevationEnabled
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.ln

enum class ColorSchemeKeyTokens {
    Background,
    Error,
    ErrorContainer,
    InverseOnSurface,
    InversePrimary,
    InverseSurface,
    OnBackground,
    OnError,
    OnErrorContainer,
    OnPrimary,
    OnPrimaryContainer,
    OnPrimaryFixed,
    OnPrimaryFixedVariant,
    OnSecondary,
    OnSecondaryContainer,
    OnSecondaryFixed,
    OnSecondaryFixedVariant,
    OnSurface,
    OnSurfaceVariant,
    OnTertiary,
    OnTertiaryContainer,
    OnTertiaryFixed,
    OnTertiaryFixedVariant,
    Outline,
    OutlineVariant,
    Primary,
    PrimaryContainer,
    PrimaryFixed,
    PrimaryFixedDim,
    Scrim,
    Secondary,
    SecondaryContainer,
    SecondaryFixed,
    SecondaryFixedDim,
    Surface,
    SurfaceBright,
    SurfaceContainer,
    SurfaceContainerHigh,
    SurfaceContainerHighest,
    SurfaceContainerLow,
    SurfaceContainerLowest,
    SurfaceDim,
    SurfaceTint,
    SurfaceVariant,
    Tertiary,
    TertiaryContainer,
    TertiaryFixed,
    TertiaryFixedDim,
}

val ColorSchemeKeyTokens.value: Color
    @Composable @ReadOnlyComposable
    get() = MaterialTheme.colorScheme.fromToken(this)

fun ColorScheme.fromToken(value: ColorSchemeKeyTokens): Color =
    when (value) {
        ColorSchemeKeyTokens.Background -> background
        ColorSchemeKeyTokens.Error -> error
        ColorSchemeKeyTokens.ErrorContainer -> errorContainer
        ColorSchemeKeyTokens.InverseOnSurface -> inverseOnSurface
        ColorSchemeKeyTokens.InversePrimary -> inversePrimary
        ColorSchemeKeyTokens.InverseSurface -> inverseSurface
        ColorSchemeKeyTokens.OnBackground -> onBackground
        ColorSchemeKeyTokens.OnError -> onError
        ColorSchemeKeyTokens.OnErrorContainer -> onErrorContainer
        ColorSchemeKeyTokens.OnPrimary -> onPrimary
        ColorSchemeKeyTokens.OnPrimaryContainer -> onPrimaryContainer
        ColorSchemeKeyTokens.OnSecondary -> onSecondary
        ColorSchemeKeyTokens.OnSecondaryContainer -> onSecondaryContainer
        ColorSchemeKeyTokens.OnSurface -> onSurface
        ColorSchemeKeyTokens.OnSurfaceVariant -> onSurfaceVariant
        ColorSchemeKeyTokens.SurfaceTint -> surfaceTint
        ColorSchemeKeyTokens.OnTertiary -> onTertiary
        ColorSchemeKeyTokens.OnTertiaryContainer -> onTertiaryContainer
        ColorSchemeKeyTokens.Outline -> outline
        ColorSchemeKeyTokens.OutlineVariant -> outlineVariant
        ColorSchemeKeyTokens.Primary -> primary
        ColorSchemeKeyTokens.PrimaryContainer -> primaryContainer
        ColorSchemeKeyTokens.Scrim -> scrim
        ColorSchemeKeyTokens.Secondary -> secondary
        ColorSchemeKeyTokens.SecondaryContainer -> secondaryContainer
        ColorSchemeKeyTokens.Surface -> surface
        ColorSchemeKeyTokens.SurfaceVariant -> surfaceVariant
        ColorSchemeKeyTokens.SurfaceBright -> surfaceBright
        ColorSchemeKeyTokens.SurfaceContainer -> surfaceContainer
        ColorSchemeKeyTokens.SurfaceContainerHigh -> surfaceContainerHigh
        ColorSchemeKeyTokens.SurfaceContainerHighest -> surfaceContainerHighest
        ColorSchemeKeyTokens.SurfaceContainerLow -> surfaceContainerLow
        ColorSchemeKeyTokens.SurfaceContainerLowest -> surfaceContainerLowest
        ColorSchemeKeyTokens.SurfaceDim -> surfaceDim
        ColorSchemeKeyTokens.Tertiary -> tertiary
        ColorSchemeKeyTokens.TertiaryContainer -> tertiaryContainer
        else -> Color.Unspecified
    }

@Composable
fun ColorScheme.applyTonalElevation(
    backgroundColor: Color,
    elevation: Dp,
): Color {
    val tonalElevationEnabled = LocalTonalElevationEnabled.current
    return if (backgroundColor == surface && tonalElevationEnabled) {
        surfaceColorAtElevation(elevation)
    } else {
        backgroundColor
    }
}

fun ColorScheme.surfaceColorAtElevation(elevation: Dp): Color {
    if (elevation == 0.dp) return surface
    val alpha = ((4.5f * ln(elevation.value + 1)) + 2f) / 100f
    return surfaceTint.copy(alpha = alpha).compositeOver(surface)
}
