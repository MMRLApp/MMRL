package com.dergoogler.mmrl.platform.file

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import com.dergoogler.mmrl.compat.MediaStoreCompat.getPathForUri

class SuContentResolver : ContentResolver {
    private val mContext: Context

    internal constructor(context: Context) : super(context) {
        mContext = context
    }

    /**
     * Opens a [SuFileInputStream] for a given [Uri].
     *
     * This function attempts to resolve the a file path from the provided [Uri]
     * using [getPathForUri] and then opens an input stream with superuser privileges
     * if necessary. It is designed to handle URIs that point to files requiring
     * root access.
     *
     * @param uri The [Uri] of the file to open.
     * @return A [SuFileInputStream] for reading the file, or `null` if the [Uri] is null
     *         or its path cannot be resolved.
     * @see SuFileInputStream
     */
    fun openSuInputStream(uri: Uri?): SuFileInputStream? {
        if (uri == null) return null
        val path = mContext.getPathForUri(uri)
        if (path == null) return null
        return SuFileInputStream(path)
    }

    /**
     * Opens a file descriptor to write to the content represented by a content URI,
     * using superuser (root) privileges. This is useful for writing to files that
     * are normally protected by the system.
     *
     * It first resolves the content URI to a real file path and then attempts
     * to open a [SuFileOutputStream] for that path.
     *
     * @param uri The URI of the content to open.
     * @return A [SuFileOutputStream] for writing to the file, or `null` if the
     *         URI is invalid, the path cannot be resolved, or an error occurs.
     * @see SuFileOutputStream
     * @see ContentResolver.openOutputStream
     */
    fun openSuOutputStream(uri: Uri?): SuFileOutputStream? {
        if (uri == null) return null
        val path = mContext.getPathForUri(uri)
        if (path == null) return null
        return SuFileOutputStream(path)
    }
}

val Context.suContentResolver: SuContentResolver get() = SuContentResolver(this)