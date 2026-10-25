package com.dergoogler.mmrl.ui.screens.moduleView

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.BuildConfig
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.model.WebColors
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.MimeUtil
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.mmrlx.compose.ui.ext.with
import dev.mmrlx.compose.ui.icon.Icon
import dev.mmrlx.compose.ui.icon.IconButton
import dev.mmrlx.compose.ui.scaffold.Scaffold
import dev.mmrlx.compose.ui.theme.MMRLXTheme
import dev.mmrlx.compose.ui.toolbar.Toolbar
import dev.mmrlx.compose.ui.toolbar.ToolbarDefaults
import dev.mmrlx.compose.ui.toolbar.ToolbarScrollBehavior
import dev.mmrlx.compose.ui.toolbar.ToolbarTitle
import dev.mmrlx.compose.webui.WebUIView
import dev.mmrlx.compose.webui.insets
import dev.mmrlx.compose.webui.rememberWebUIState
import dev.mmrlx.webui.RouteRegistry
import dev.mmrlx.webui.RouteScope
import dev.mmrlx.webui.WebResourceResponse
import dev.mmrlx.webui.WebUIOptions
import java.net.HttpURLConnection
import java.net.URL

@Composable
@Destination<RootGraph>
fun DescriptionScreen(readmeUrl: String) {
    val navigator = LocalDestinationsNavigator.current
    val scrollBehavior = ToolbarDefaults.pinnedScrollBehavior()
    val userPrefs = LocalUserPreferences.current

    val colors = MMRLXTheme.colors
    val scheme = MaterialTheme.colorScheme.copy(
        background = colors.background,
        onBackground = colors.foreground,
        surface = colors.code,
        surfaceTint = colors.muted
    )

    val wstate =
        rememberWebUIState(
            "https://desc.mmrl.dev",
            WebUIOptions("internal/assets/markdown.html")
        ) {
            it
                .settings {
                    debug = BuildConfig.DEBUG
                    forceKillProcess = false
                    useConsoleInterceptor = false
                    darkMode = userPrefs.isDarkMode()
                }
                .client {
                    // not related to client
                    it.webview.setBackgroundColor(scheme.background.toArgb())
                }
                .chromeClient { }
                .registerRoutes {
                    val webColors = WebColors(scheme)

                    internalRoute("readme.md") {
                        val connection = URL(readmeUrl).openConnection() as HttpURLConnection
                        connection.connect()
                        htmlResponse(connection.getInputStream())
                    }

                    internalRoute("insets.css") {
                        insets.css.asStyleResponse()
                    }

                    internalRoute("colors.css") {
                        webColors.allCssColors.asStyleResponse()
                    }

                    internalRoute("assets/*asset") {
                        val filePath =
                            request.params["asset"] ?: return@internalRoute notFoundResponse

                        try {
                            val inputStream = kontext.assets.open(filePath)
                            val mimeType = MimeUtil.getMimeFromFileName(filePath)
                            response(mimeType = mimeType, data = inputStream)
                        } catch (e: Exception) {
                            console.error("Failed to open $filePath from the assets: $e")
                            notFoundResponse
                        }
                    }
                }
        }

    DisposableEffect(Unit) {
        onDispose {
            wstate.destroy()
        }
    }

    // We have to override otherwise the WebUI will close the entire app
    BackHandler {
        if (wstate.canGoBack()) {
            wstate.goBack()
            return@BackHandler
        }

        navigator.popBackStack()
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        toolbar = {
            TopBar0(
                scrollBehavior = scrollBehavior,
                navigator = navigator,
            )
        },
        contentWindowInsets = WindowInsets.none,
    ) {
        wstate.insets(
            PaddingValues(
                top = contentPadding.calculateTopPadding(),
                bottom = contentPadding.calculateBottomPadding(),
            )
        )

        WebUIView(
            modifier = Modifier
                .with(this@Scaffold) {
                    it.scaffoldHazeSource()
                }
                .fillMaxSize(),
            state = wstate
        )
    }
}

@Composable
private fun TopBar0(
    navigator: DestinationsNavigator,
    scrollBehavior: ToolbarScrollBehavior,
) = Toolbar(
    navigationIcon = {
        IconButton(onClick = { navigator.popBackStack() }) {
            Icon(
                painter = painterResource(id = R.drawable.arrow_left),
                contentDescription = null,
            )
        }
    },
    title = { ToolbarTitle(title = stringResource(id = R.string.view_module_about_this_module)) },
    scrollBehavior = scrollBehavior,
)

private fun RouteRegistry.internalRoute(
    name: String,
    content: RouteScope.() -> WebResourceResponse,
) = route("internal/$name", content)