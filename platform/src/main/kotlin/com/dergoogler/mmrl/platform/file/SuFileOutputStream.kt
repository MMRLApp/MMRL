@file:Suppress("unused")

package com.dergoogler.mmrl.platform.file

import android.os.ParcelFileDescriptor
import android.system.OsConstants.O_APPEND
import android.system.OsConstants.O_CREAT
import android.system.OsConstants.O_TRUNC
import android.system.OsConstants.O_WRONLY
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream

/**
 * An [OutputStream] implementation for writing to files with root (superuser) permissions.
 *
 * This class mirrors the functionality of [FileOutputStream] but utilizes [SuFile]
 * to perform operations with elevated privileges. It allows for creating a stream from
 * a [String] path, a [File], or an [SuFile] instance, with optional support for
 * appending to existing files.
 *
 * @see SuFile
 * @see java.io.FileOutputStream
 */
class SuFileOutputStream : FileOutputStream {
    private var _pfd: ParcelFileDescriptor

    @Throws(IOException::class)
    constructor(file: SuFile, flags: Int, mode: Int) : this(
        FileUtils.openWritePipe(
            file,
            flags,
            mode
        )
    )

    @Throws(IOException::class)
    constructor(file: SuFile, append: Boolean = false) : this(
        file,
        O_CREAT or O_WRONLY or (if (append) O_APPEND else O_TRUNC),
        438
    )

    @Throws(IOException::class)
    constructor(path: String, append: Boolean = false) : this(SuFile(path), append)


    @Throws(IOException::class)
    constructor(file: File, append: Boolean = false) : this(file.path, append)


    private constructor(pfd: ParcelFileDescriptor) : super(pfd.fileDescriptor) {
        this._pfd = pfd
    }

    override fun close() {
        try {
            super.close()
        } catch (e: Exception) {
            Log.e("SuFileOutputStream", "Error closing output stream", e)
            _pfd.close()
        }
    }
}
