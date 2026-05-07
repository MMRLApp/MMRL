package dev.dergoogler.mmrl.compat.core

import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.UriHandler
import androidx.core.net.toUri
import timber.log.Timber

interface MMRLUriHandler : UriHandler {
    fun openUri(
        uri: String,
        onSuccess: ((customTabsIntent: CustomTabsIntent, url: String) -> Unit)? = null,
        onError: ((Exception) -> Unit)? = null,
    )

    override fun openUri(uri: String)
}

class MMRLUriHandlerImpl(
    private val context: Context,
    private val color: Color,
) : MMRLUriHandler {
    override fun openUri(
        uri: String,
        onSuccess: ((customTabsIntent: CustomTabsIntent, url: String) -> Unit)?,
        onError: ((Exception) -> Unit)?,
    ) {
        try {
            val colorSchemeParams =
                CustomTabColorSchemeParams
                    .Builder()
                    .setToolbarColor(color.toArgb())
                    .build()

            val customTabsIntent =
                CustomTabsIntent
                    .Builder()
                    .setDefaultColorSchemeParams(colorSchemeParams)
                    .build()

            customTabsIntent.apply {
                if (onSuccess != null) {
                    onSuccess(this, uri)
                } else {
                    launchUrl(context, uri.toUri())
                }
            }
        } catch (activityNotFoundException: Exception) {
            if (onError != null) {
                onError(activityNotFoundException)
            } else {
                Timber.e(activityNotFoundException, "Unable to launch custom tab")
                Intent.parseUri(uri, Intent.URI_INTENT_SCHEME).apply {
                    context.startActivity(this)
                }
            }
        }
    }

    override fun openUri(uri: String) {
        openUri(
            uri = uri,
            onError = {
                Timber.e(it, "Unable to open $uri")
                Toast.makeText(context, "Unable to open $uri", Toast.LENGTH_SHORT).show()
            },
        )
    }
}

val LocalUriHandler =
    staticCompositionLocalOf<MMRLUriHandler> {
        error("CompositionLocal MMRLUriHandlerImpl not present")
    }
