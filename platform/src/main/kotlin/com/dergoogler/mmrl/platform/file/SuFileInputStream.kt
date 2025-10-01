@file:Suppress("unused")

package com.dergoogler.mmrl.platform.file

import com.dergoogler.mmrl.platform.file.SuFile.Companion.toSuFile
import java.io.File
import java.io.InputStream

/**
 * An [InputStream] for reading data from a [SuFile].
 *
 * This class provides a way to read files that require superuser (root) privileges,
 * by wrapping the stream obtained from [SuFile.newInputStream]. It offers constructors
 * that accept a file path, a [SuFile] object, or a standard [java.io.File] object,
 * making it a flexible replacement for [java.io.FileInputStream] when root access is needed.
 *
 * @see SuFile
 * @see java.io.InputStream
 */
class SuFileInputStream : InputStream {
    private var fis: InputStream

    constructor(path: String) : this(path.toSuFile())
    constructor(file: SuFile) {
        fis = file.newInputStream()
    }

    constructor(file: File) : this(file.path.toSuFile())

    override fun read(): Int = fis.read()
}