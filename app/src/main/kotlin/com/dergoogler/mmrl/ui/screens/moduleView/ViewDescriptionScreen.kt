package com.dergoogler.mmrl.ui.screens.moduleView

import android.annotation.SuppressLint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.viewinterop.AndroidView
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Event.Companion.isFailed
import com.dergoogler.mmrl.app.Event.Companion.isLoading
import com.dergoogler.mmrl.app.Event.Companion.isSucceeded
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.network.compose.requestString
import com.dergoogler.mmrl.ui.activity.webui.interfaces.MarkdownInterface
import com.dergoogler.mmrl.ui.component.Failed
import com.dergoogler.mmrl.ui.component.Loading
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurToolbar
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.webui.client.WXClient
import com.dergoogler.mmrl.webui.handler.internalPathHandler
import com.dergoogler.mmrl.webui.model.Insets
import com.dergoogler.mmrl.webui.util.WebUIOptions
import com.dergoogler.mmrl.webui.view.WebUIView
import com.dergoogler.mmrl.webui.wxAssetLoader
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.chrisbanes.haze.hazeSource

const val launchUrl = "https://mui.kernelsu.org/internal/assets/markdown.html"

@SuppressLint("SetJavaScriptEnabled")
@Composable
@Destination<RootGraph>
fun ViewDescriptionScreen(
    readmeUrl: String,
) = LocalScreenProvider {
    val density = LocalDensity.current
    val navigator = LocalDestinationsNavigator.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val userPrefs = LocalUserPreferences.current

    var readme by remember { mutableStateOf("") }
    val event = requestString(
        url = readmeUrl,
        onSuccess = { readme = it }
    )

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                scrollBehavior = scrollBehavior,
                navigator = navigator
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        val bottomBarPaddingValues = LocalMainScreenInnerPaddings.current

        Column(
            modifier = Modifier
                .fillMaxSize()
                .hazeSource(LocalHazeState.current)
        ) {
            AnimatedVisibility(
                modifier = Modifier.fillMaxSize(),
                visible = event.isLoading,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Loading()
            }

            AnimatedVisibility(
                visible = event.isFailed,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Failed()
            }

            AnimatedVisibility(
                visible = event.isSucceeded,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                this@Scaffold.ResponsiveContent {
                    AndroidView(
                        factory = { context ->
                            val options = WebUIOptions(
                                context = context,
                                isDarkMode = userPrefs.isDarkMode(),
                                colorScheme = userPrefs.colorScheme(context),
                            )

                            val assetsLoader = wxAssetLoader(
                                handlers = listOf(
                                    "/internal/" to internalPathHandler(
                                        options,
                                        Insets(
                                            top = with(density) {
                                                val pad = innerPadding.calculateTopPadding()
                                                val px = with(density) { pad.toPx() }.toInt()
                                                (px / this.density).toInt()
                                            },
                                            bottom = with(density) {
                                                val pad =
                                                    bottomBarPaddingValues.calculateBottomPadding()
                                                val px = with(density) { pad.toPx() }.toInt()
                                                (px / this.density).toInt()
                                            },
                                            left = 0,
                                            right = 0
                                        )
                                    )
                                )
                            )

                            WebUIView(options).apply {
                                webViewClient = WXClient(options, assetsLoader)
                            }
                        },
                        update = { webView ->
                            webView.addJavascriptInterface<MarkdownInterface>(
                                arrayOf(readme),
                                arrayOf(String::class.java)
                            )

                            webView.loadUrl(launchUrl)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    navigator: DestinationsNavigator,
    scrollBehavior: TopAppBarScrollBehavior,
) = BlurToolbar(
    navigationIcon = {
        IconButton(onClick = { navigator.popBackStack() }) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_left), contentDescription = null
            )
        }
    },
    title = { Text(text = stringResource(id = R.string.view_module_about_this_module)) },
    scrollBehavior = scrollBehavior
)