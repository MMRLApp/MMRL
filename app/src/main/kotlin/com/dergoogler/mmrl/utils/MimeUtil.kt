package com.dergoogler.mmrl.utils

import java.net.URLConnection
import java.util.Locale

object MimeUtil {
    fun getMimeFromFileName(fileName: String?): String? {
        if (fileName == null) {
            return null
        }

        val mimeType = URLConnection.guessContentTypeFromName(fileName)

        if (mimeType != null) {
            return mimeType
        }

        return guessHardcodedMime(fileName)
    }

    private fun guessHardcodedMime(fileName: String): String? {
        val finalFullStop = fileName.lastIndexOf('.')
        if (finalFullStop == -1) {
            return null
        }

        val extension = fileName.substring(finalFullStop + 1).lowercase(Locale.getDefault())

        return when (extension) {
            "webm" -> "video/webm"
            "mpeg", "mpg" -> "video/mpeg"
            "mp3" -> "audio/mpeg"
            "wasm" -> "application/wasm"
            "xhtml", "xht", "xhtm" -> "application/xhtml+xml"
            "flac" -> "audio/flac"
            "ogg", "oga", "opus" -> "audio/ogg"
            "wav" -> "audio/wav"
            "m4a" -> "audio/x-m4a"
            "gif" -> "image/gif"
            "jpeg", "jpg", "jfif", "pjpeg", "pjp" -> "image/jpeg"
            "png" -> "image/png"
            "apng" -> "image/apng"
            "svg", "svgz" -> "image/svg+xml"
            "webp" -> "image/webp"
            "mht", "mhtml" -> "multipart/related"
            "css" -> "text/css"
            "html", "htm", "shtml", "shtm", "ehtml" -> "text/html"
            "js", "mjs" -> "application/javascript"
            "xml" -> "text/xml"
            "mp4", "m4v" -> "video/mp4"
            "ogv", "ogm" -> "video/ogg"
            "ico" -> "image/x-icon"
            "woff" -> "application/font-woff"
            "gz", "tgz" -> "application/gzip"
            "json" -> "application/json"
            "pdf" -> "application/pdf"
            "zip" -> "application/zip"
            "bmp" -> "image/bmp"
            "tiff", "tif" -> "image/tiff"
            else -> null
        }
    }
}