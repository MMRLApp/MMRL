package com.dergoogler.mmrl.ui.activity

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import coil.compose.AsyncImage
import com.dergoogler.mmrl.ext.findActivity
import com.dergoogler.mmrl.ui.component.NavigateUpTopBar
import net.engawapg.lib.zoomable.rememberZoomState
import net.engawapg.lib.zoomable.zoomable

@Composable
fun rememberInsetsController(): WindowInsetsControllerCompat? {
    val context = LocalContext.current
    return remember(Unit) {
        val window = context.findActivity()?.window ?: return@remember null
        return@remember WindowCompat.getInsetsController(window, window.decorView)
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun ScreenshotsPreviewScreen(
    index: Int,
    urls: List<String>,
) {
    val pagerState =
        rememberPagerState(
            initialPage = index,
            pageCount = { urls.size },
        )

    val insetsController = rememberInsetsController()
    var isVisible by remember {
        mutableStateOf(false)
    }

    DisposableEffect(Unit) {
        insetsController ?: return@DisposableEffect onDispose {}

        insetsController.apply {
            hide(WindowInsetsCompat.Type.statusBars())
            hide(WindowInsetsCompat.Type.navigationBars())
            isVisible = false
            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        onDispose {
            insetsController.apply {
                show(WindowInsetsCompat.Type.statusBars())
                show(WindowInsetsCompat.Type.navigationBars())
                isVisible = true
                systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
            }
        }
    }

    Scaffold(
        topBar = {
            AnimatedVisibility(
                visible = isVisible,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                NavigateUpTopBar(
                    title = "",
                    colors =
                        TopAppBarDefaults.topAppBarColors().copy(
                            containerColor = Color.Transparent,
                        ),
                )
            }
        },
    ) {
        HorizontalPager(
            state = pagerState,
            modifier =
                Modifier
                    .fillMaxSize(),
        ) { page ->
            Screenshot(
                url = urls[page],
                onTap = {
                    insetsController ?: return@Screenshot

                    insetsController.apply {
                        if (!isVisible) {
                            show(WindowInsetsCompat.Type.statusBars())
                            show(WindowInsetsCompat.Type.navigationBars())
                            isVisible = true
                            systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_DEFAULT
                        } else {
                            hide(WindowInsetsCompat.Type.statusBars())
                            hide(WindowInsetsCompat.Type.navigationBars())
                            isVisible = false
                            systemBarsBehavior =
                                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
                        }
                    }
                },
            )
        }
    }
}

@Composable
fun Screenshot(
    url: String,
    onTap: (position: Offset) -> Unit,
) {
    val zoomState = rememberZoomState()

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        AsyncImage(
            model = url,
            modifier =
                Modifier
                    .zoomable(
                        zoomState = zoomState,
                        onTap = onTap,
                    ).fillMaxWidth(),
            onSuccess = { state ->
                zoomState.setContentSize(state.painter.intrinsicSize)
            },
            contentDescription = null,
        )
    }
}
