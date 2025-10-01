package com.dergoogler.mmrl.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImagePainter.State.Empty.painter

/**
 * A composable that displays a logo with a background.
 *
 * @param icon The drawable resource ID for the logo icon.
 * @param modifier The modifier to be applied to the logo.
 * @param shape The shape of the logo background. Defaults to [CircleShape].
 * @param contentColor The color of the logo icon. Defaults to the onPrimary color from the MaterialTheme.
 * @param containerColor The color of the logo background. Defaults to the primary color from the MaterialTheme.
 * @param fraction The fraction of the container size that the icon should occupy. Defaults to 0.6f.
 */
@Composable
fun Logo(
    @DrawableRes icon: Int,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    fraction: Float = 0.6f,
) = Logo(
    icon = {
        Icon(
            modifier = it,
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = LocalContentColor.current
        )
    },
    modifier = modifier,
    shape = shape,
    contentColor = contentColor,
    containerColor = containerColor,
    fraction = fraction
)

/**
 * A composable function that displays a logo with a customizable icon, shape, colors, and size.
 *
 * @param icon A composable lambda that renders the icon. It receives a [Modifier] that should be applied to the icon.
 * @param modifier The modifier to be applied to the logo container.
 * @param shape The shape of the logo container. Defaults to [CircleShape].
 * @param contentColor The color of the icon. Defaults to the onPrimary color from the current [MaterialTheme].
 * @param containerColor The background color of the logo container. Defaults to the primary color from the current [MaterialTheme].
 * @param fraction The fraction of the container size that the icon should occupy. Defaults to 0.6f (60%).
 */
@Composable
fun Logo(
    icon: @Composable (Modifier) -> Unit,
    modifier: Modifier = Modifier,
    shape: Shape = CircleShape,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    fraction: Float = 0.6f,
) = Surface(
    modifier = modifier,
    shape = shape,
    color = containerColor,
    contentColor = contentColor
) {
    Box(
        contentAlignment = Alignment.Center
    ) {
        icon(Modifier.fillMaxSize(fraction))
    }
}