@file:Suppress("unused")

package com.dergoogler.mmrl.platform.file

import com.dergoogler.mmrl.platform.file.SuFile.Companion.toSuFile
import java.io.File
import java.io.OutputStream

/**
 * An [OutputStream] for writing to a file using root (superuser) permissions.
 * This class mirrors the functionality of [java.io.FileOutputStream] but operates
 * on files that may require elevated privileges to access.
 *
 * It provides constructors to create a stream from a file path string, a [SuFile] object,
 * or a standard [java.io.File] object.
 *
 * @see SuFile
 * @see OutputStream
 */
class SuFileOutputStream : OutputStream {
    private var ops: OutputStream

    constructor(path: String, append: Boolean = false) : this(path.toSuFile(), append)
    constructor(file: SuFile, append: Boolean = false) {
        ops = file.newOutputStream(append)
    }

    constructor(file: File, append: Boolean = false) : this(file.path, append)

    override fun write(b: Int) = ops.write(b)
}

