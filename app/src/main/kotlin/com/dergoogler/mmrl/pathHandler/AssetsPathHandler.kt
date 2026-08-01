package com.dergoogler.mmrl.pathHandler

import android.util.Log
import android.webkit.WebResourceResponse
import com.dergoogler.mmrl.hybridwebui.HybridWebUI
import dev.mmrlx.webui.PathHandler
import dev.mmrlx.webui.WebUI
import dev.mmrlx.webui.WebUIResourceRequest
import java.io.IOException

class AssetsPathHandler(
    webui: WebUI,
) : PathHandler(webui) {
    private val assetHelper get() = kontext.assets

    override fun handle(request: WebUIResourceRequest): WebResourceResponse {
        val path = request.path

        try {
            val inputStream = assetHelper.open(path.removePrefix("/"))
            val mimeType = HybridWebUI.MimeType.getMimeFromFileName(path)
            return WebResourceResponse(mimeType, null, inputStream)
        } catch (e: IOException) {
            Log.e("assetsPathHandler", "Error opening asset path: $path", e)
            return notFoundResponse
        }
    }
}
