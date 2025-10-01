package com.dergoogler.mmrl.ui.screens.moduleView.sections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dergoogler.mmrl.model.online.hasScreenshots
import com.dergoogler.mmrl.ui.activity.ScreenshotsPreviewActivity
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule

@Composable
internal fun Screenshots() {
    val context = LocalContext.current
    val module = LocalOnlineModule.current

    val screenshotsLazyListState = rememberLazyListState()

    module.hasScreenshots { screens ->
        Spacer(modifier = Modifier.height(16.dp))

        LazyRow(
            state = screenshotsLazyListState,
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp)

        ) {
            itemsIndexed(
                items = screens,
            ) { index, screen ->
                val interactionSource = remember { MutableInteractionSource() }

                AsyncImage(
                    model = screen,
                    contentDescription = null,
                    modifier = Modifier
                        .height(160.dp)
                        .clickable(
                            interactionSource = interactionSource,
                            indication = ripple(),
                            onClick = {
                                ScreenshotsPreviewActivity.start(
                                    context,
                                    screens,
                                    index
                                )
                            }
                        )
                        .aspectRatio(9f / 16f)
                        .clip(RoundedCornerShape(10.dp)),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}