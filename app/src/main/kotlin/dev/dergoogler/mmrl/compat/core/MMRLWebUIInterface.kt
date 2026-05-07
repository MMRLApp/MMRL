package dev.dergoogler.mmrl.compat.core

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.webkit.WebView

interface WebUIConsole {
    fun error(message: String)

    fun info(message: String)

    fun log(message: String)

    fun warn(message: String)
}

open class MMRLWebUIInterface(
    internal val webView: WebView,
    internal val context: Context,
) {
    internal val activity = context as Activity

    internal fun runOnUiThread(block: () -> Unit) = (context as Activity).runOnUiThread(block)

    internal fun runJs(script: String) = runOnUiThread { webView.evaluateJavascript(script, null) }

    internal fun runPost(action: WebView.() -> Unit) {
        webView.post { action(webView) }
    }

    internal fun runJsCatching(block: () -> Unit) {
        try {
            block()
        } catch (e: Throwable) {
            runJs("new Error('${e.message}', { cause: '${e.message}' })")
        }
    }

    internal fun runMainLooperPost(action: () -> Unit) {
        if (context is Activity) {
            Handler(Looper.getMainLooper()).post(action)
        }
    }

    internal val console =
        object : WebUIConsole {
            override fun error(message: String) = runJs("console.error('$message')")

            override fun info(message: String) = runJs("console.info('$message')")

            override fun log(message: String) = runJs("console.log('$message')")

            override fun warn(message: String) = runJs("console.warn('$message')")
        }

    internal fun <R> runTry(
        message: String = "Unknown Error",
        default: R,
        block: () -> R,
    ): R =
        try {
            block()
        } catch (e: Throwable) {
            runJs("new Error('$message', { cause: \"${e.message}\" })")
            default
        }

    internal fun <R> runTry(
        message: String = "Unknown Error",
        block: () -> R,
    ): R? = runTry(message, null, block)

    internal fun <R, T> runTryJsWith(
        with: T,
        message: String = "Unknown Error",
        block: T.() -> R,
    ): R? = runTryJsWith(with, message, null, block)

    internal fun <R, T> runTryJsWith(
        with: T,
        message: String = "Unknown Error",
        default: R,
        block: T.() -> R,
    ): R {
        return try {
            with(with, block)
        } catch (e: Throwable) {
            runJs("new Error('$message', { cause: \"${e.message}\" })")
            return default
        }
    }
}
