package com.dergoogler.mmrl.utils

object Utils {
    fun getVersionDisplay(
        version: String,
        versionCode: Int,
    ): String {
        val included =
            "\\(.*?$versionCode.*?\\)"
                .toRegex()
                .containsMatchIn(version)

        return if (included) {
            version
        } else {
            "$version ($versionCode)"
        }
    }

    fun getFilename(
        name: String,
        version: String,
        versionCode: Int,
        extension: String,
    ): String {
        val versionNew = version.replace("\\([^)]*\\)".toRegex(), "")
        return "$name-$versionNew-$versionCode.$extension"
            .replace("[\\\\/:*?\"<>|]".toRegex(), "")
    }
}
