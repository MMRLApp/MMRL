package com.dergoogler.mmrl.ui.component

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.graphics.asImageBitmap
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.dergoogler.mmrl.ui.R
import java.io.InputStream

private const val DefaultAspectRatio = 2.048f
private val DefaultShape: RoundedCornerShape = RoundedCornerShape(0.dp)

@Composable
fun Cover(
    modifier: Modifier = Modifier,
    url: String,
    shape: RoundedCornerShape = DefaultShape,
    errorIcon: Int = R.drawable.alert_triangle,
    aspectRatio: Float = DefaultAspectRatio,
) {
    val context = LocalContext.current

    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(context)
            .data(url)
            .memoryCacheKey(url)
            .diskCacheKey(url)
            .diskCachePolicy(CachePolicy.ENABLED)
            .memoryCachePolicy(CachePolicy.ENABLED)
            .build(),
    )

    if (painter.state !is AsyncImagePainter.State.Error) {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape)
                .aspectRatio(aspectRatio)
                .then(modifier)
        )
    } else {
        Logo(
            icon = errorIcon,
            shape = shape,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(aspectRatio)
                .then(modifier)
        )
    }
}

@Composable
fun LocalCover(
    modifier: Modifier = Modifier,
    inputStream: InputStream,
    shape: RoundedCornerShape = DefaultShape,
    aspectRatio: Float = DefaultAspectRatio,
) {
    val bitmap = remember {
        inputStream.use { input ->
            BitmapFactory.decodeStream(input).asImageBitmap()
        }
    }

    Image(
        painter = BitmapPainter(bitmap),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .aspectRatio(aspectRatio)
            .then(modifier)
    )
}