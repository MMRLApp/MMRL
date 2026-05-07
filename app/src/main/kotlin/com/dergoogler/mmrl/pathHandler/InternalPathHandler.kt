package com.dergoogler.mmrl.pathHandler


import android.content.Context
import android.util.Log
import android.webkit.WebResourceResponse
import androidx.compose.material3.ColorScheme
import com.dergoogler.mmrl.hybridwebui.HybridWebUI
import com.dergoogler.mmrl.hybridwebui.HybridWebUIInsets
import com.dergoogler.mmrl.hybridwebui.HybridWebUIResourceRequest
import com.dergoogler.mmrl.model.WebColors
import java.io.IOException

class InternalPathHandler(
    private val context: Context,
    private val colorScheme: ColorScheme,
    private val insets: HybridWebUIInsets,
) : HybridWebUI.PathHandler() {
    val webColors get() = WebColors(colorScheme)
    val assetsPathHandler = AssetsPathHandler(context)

    override fun handle(
        view: HybridWebUI,
        request: HybridWebUIResourceRequest,
    ): WebResourceResponse {
        val path = request.path

        try {
            if (path.matches(Regex("^assets(/.*)?$"))) {
                return assetsPathHandler.handle(
                    view,
                    HybridWebUIResourceRequest(
                        method = request.method,
                        isForMainFrame = request.isForMainFrame,
                        url = request.url,
                        path = path.removePrefix("assets/"),
                        requestHeaders = request.requestHeaders,
                        isRedirect = request.isRedirect,
                        hasGesture = request.hasGesture
                    )
                )
            }

            if (path.matches(Regex("insets\\.css"))) {
                return insets.css.asStyleResponse()
            }

            if (path.matches(Regex("colors\\.css"))) {
                return webColors.allCssColors.asStyleResponse()
            }

            return notFoundResponse
        } catch (e: IOException) {
            Log.e("InternalPathHandler", "Error opening mmrl asset path: $path", e)
            return notFoundResponse
        }
    }
}