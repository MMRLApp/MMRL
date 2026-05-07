@file:Suppress("unused")

package com.dergoogler.mmrl.platform.file

import android.os.ParcelFileDescriptor
import android.system.OsConstants.O_RDONLY
import android.util.Log
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * An implementation of [FileInputStream] designed to read files with superuser (root) privileges.
 *
 * This class facilitates reading from protected system files by utilizing a [ParcelFileDescriptor]
 * obtained through a root-level pipe. It allows standard stream-based operations on files
 * that are normally inaccessible to the application process.
 *
 * @see SuFile
 * @see FileInputStream
 */
class SuFileInputStream : FileInputStream {
    private var _pfd: ParcelFileDescriptor

    @Throws(IOException::class)
    constructor(file: SuFile, flags: Int, mode: Int) : this(FileUtils.openReadPipe(file, flags, mode))

    @Throws(IOException::class)
    constructor(file: SuFile) : this(file, O_RDONLY, 0)

    @Throws(IOException::class)
    constructor(file: File) : this(SuFile(file))

    @Throws(IOException::class)
    constructor(path: String) : this(SuFile(path))

    private constructor(pfd: ParcelFileDescriptor) : super(pfd.fileDescriptor) {
        this._pfd = pfd
    }

    override fun close() {
        try {
            super.close()
        } catch (e: Exception) {
            Log.e("SuFileInputStream", "Error closing input stream", e)
            _pfd.close()
        }
    }
}
