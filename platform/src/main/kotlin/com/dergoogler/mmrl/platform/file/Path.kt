package com.dergoogler.mmrl.platform.file

import android.net.Uri
import java.io.File

object Path {
    private fun assertPath(path: String?) {
        if (path == null) {
            throw IllegalArgumentException("Path must be a string. Received null")
        }
    }

    fun resolve(vararg paths: String): String {
        var resolvedPath = ""
        var resolvedAbsolute = false

        for (i in paths.indices.reversed()) {
            val path = paths[i]
            assertPath(path)

            if (path.isEmpty()) continue

            resolvedPath = "$path/$resolvedPath"
            resolvedAbsolute = path[0] == '/'

            if (resolvedAbsolute) break
        }

        resolvedPath = normalizeStringPosix(resolvedPath, !resolvedAbsolute)

        return when {
            resolvedAbsolute -> if (resolvedPath.isNotEmpty()) "/$resolvedPath" else "/"
            resolvedPath.isNotEmpty() -> resolvedPath
            else -> "."
        }
    }

    fun normalizeStringPosix(
        path: String,
        allowAboveRoot: Boolean,
    ): String {
        var res = ""
        var lastSegmentLength = 0
        var lastSlash = -1
        var dots = 0
        var code: Char

        for (i in 0..path.length) {
            code = if (i < path.length) path[i] else '/'

            if (code == '/') {
                if (lastSlash == i - 1 || dots == 1) {
                    // NOOP
                } else if (lastSlash != i - 1 && dots == 2) {
                    if (res.length < 2 || lastSegmentLength != 2 || res.takeLast(2) != "..") {
                        if (res.length > 2) {
                            val lastSlashIndex = res.lastIndexOf('/')
                            if (lastSlashIndex != res.length - 1) {
                                res =
                                    if (lastSlashIndex == -1) {
                                        ""
                                    } else {
                                        res.substring(
                                            0,
                                            lastSlashIndex,
                                        )
                                    }
                                lastSegmentLength = res.length - 1 - res.lastIndexOf('/')
                                lastSlash = i
                                dots = 0
                                continue
                            }
                        } else if (res.length in 1..2) {
                            res = ""
                            lastSegmentLength = 0
                            lastSlash = i
                            dots = 0
                            continue
                        }
                    }
                    if (allowAboveRoot) {
                        res = if (res.isNotEmpty()) "$res/.." else ".."
                        lastSegmentLength = 2
                    }
                } else {
                    res =
                        if (res.isNotEmpty()) {
                            "$res/${
                                path.substring(
                                    lastSlash + 1,
                                    i,
                                )
                            }"
                        } else {
                            path.substring(lastSlash + 1, i)
                        }
                    lastSegmentLength = i - lastSlash - 1
                }
                lastSlash = i
                dots = 0
            } else if (code == '.' && dots != -1) {
                dots++
            } else {
                dots = -1
            }
        }
        return res
    }

    fun parse(vararg paths: Any): String =
        resolve(
            *paths
                .map {
                    when (it) {
                        is ExtFile,
                        is File,
                        -> it.path

                        is String -> it
                        is Uri -> it.toString()
                        else -> throw IllegalArgumentException("Unsupported type: ${it::class}")
                    }
                }.toTypedArray(),
        )
}
