@file:Suppress("unused")

package com.dergoogler.mmrl.platform.file

import android.net.Uri
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.system.ErrnoException
import android.system.Os
import android.util.Log
import com.dergoogler.mmrl.ext.isNotNullOrBlank
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.stub.IFileManager
import java.io.File
import java.io.FileFilter
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import java.nio.charset.StandardCharsets
import java.util.Locale
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


/**
 * A [File] abstraction that utilizes root privileges for file operations if available,
 * otherwise falling back to standard [java.io.File] operations.
 *
 * This class extends [ExtFile] (which itself extends [java.io.File]) and overrides
 * its methods. If a root-enabled [IFileManager] is available through [Platform.fileManagerOrNull],
 * operations are attempted using root privileges. If the root operation fails or if no
 * root file manager is available, the corresponding `super` (i.e., [ExtFile]'s or [java.io.File]'s)
 * method is called as a fallback.
 *
 * This allows for seamless interaction with the file system, attempting privileged
 * operations when possible and gracefully degrading to non-privileged operations otherwise.
 *
 * It provides constructors to create `SuFile` objects from various path representations
 * (String, File, Uri) and offers methods for common file operations such as
 * reading, writing, listing directories, checking existence, and managing permissions.
 *
 * @param paths Vararg parameter representing the path components. Can be of type [String], [File], or [Uri].
 *              These components will be resolved into a single absolute path.
 *
 * @see java.io.File
 * @see ExtFile
 * @see com.dergoogler.mmrl.platform.Platform.fileManagerOrNull
 * @see com.dergoogler.mmrl.platform.stub.IFileManager
 */
class SuFile(
    vararg paths: Any,
) : ExtFile(*paths) {
    fun readText(): String {
        try {
            val bytes = newInputStream().use { it.readBytes() }
            val data = ByteBuffer.wrap(bytes)
            val content = StandardCharsets.UTF_8.decode(data).toString()

            if (content.isNotNullOrBlank()) {
                return content
            }

            return readText(Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Error reading text file: ${e.message}")
            e.printStackTrace()
            return readText(Charsets.UTF_8)
        }
    }

    fun fromPaths(vararg paths: Any): SuFile? {
        val files = paths.map { SuFile(path, it) }
        for (f in files) {
            if (f.exists()) {
                return f
            }
        }

        return null
    }

    fun readBytes(): ByteArray = newInputStream().use { it.readBytes() }

    fun writeText(data: String) = newOutputStream(false).use { it.write(data.toByteArray()) }

    fun writeBytes(data: ByteArray) = newOutputStream(false).use { it.write(data) }

    override fun list(): Array<String>? = fallback(
        { this.list(path) },
        { super.list() }
    )

    override fun length(): Long = fallback(
        { this.length(path) },
        { super.length() }
    )

    fun stat(): Long {
        return this.lastModified()
    }

    override fun lastModified(): Long = fallback(
        { this.stat(path) },
        { super.lastModified() }
    )

    override fun exists(): Boolean = fallback(
        { this.exists(path) },
        { super.exists() }
    )

    @OptIn(ExperimentalContracts::class)
    inline fun <R> exists(block: (SuFile) -> R): R? {
        contract {
            callsInPlace(block, InvocationKind.AT_MOST_ONCE)
        }

        return if (exists()) block(this) else null
    }

    override fun isDirectory(): Boolean = fallback(
        { this.isDirectory(path) },
        { super.isDirectory() }
    )

    override fun isFile(): Boolean = fallback(
        { this.isFile(path) },
        { super.isFile() }
    )

    override fun isBlock(): Boolean = fallback(
        { this.isBlock(path) },
        { super.isBlock() }
    )

    override fun isCharacter(): Boolean = fallback(
        { this.isCharacter(path) },
        { super.isCharacter() }
    )

    override fun isSymlink(): Boolean = fallback(
        { this.isSymlink(path) },
        { super.isSymlink() }
    )

    override fun isNamedPipe(): Boolean = fallback(
        { this.isNamedPipe(path) },
        { super.isNamedPipe() }
    )

    override fun isSocket(): Boolean = fallback(
        { this.isSocket(path) },
        { super.isSocket() }
    )

    override fun mkdir(): Boolean = fallback(
        { this.mkdir(path) },
        { super.mkdir() }
    )

    override fun mkdirs(): Boolean = fallback(
        { this.mkdirs(path) },
        { super.mkdirs() }
    )

    override fun createNewFile(): Boolean = fallback(
        { this.createNewFile(path) },
        { super.createNewFile() }
    )


    override fun renameTo(dest: File): Boolean = fallback(
        { this.renameTo(path, dest.path) },
        { super.renameTo(dest) }
    )

    fun copyTo(dest: File, overwrite: Boolean = false) = fallback(
        { this.copyTo(path, dest.path, overwrite) },
        { File(path).copyTo(dest, overwrite) }
    )

    override fun canExecute(): Boolean = fallback(
        { this.canExecute(path) },
        { super.canExecute() }
    )

    override fun canRead(): Boolean = fallback(
        { this.canRead(path) },
        { super.canRead() }
    )

    override fun canWrite(): Boolean = fallback(
        { this.canWrite(path) },
        { super.canWrite() }
    )

    override fun delete(): Boolean = fallback(
        { this.delete(path) },
        { super.delete() }
    )

    override fun deleteOnExit() {
        fallback(
            {
                this.deleteOnExit(path)
            },
            { super.deleteOnExit() }
        )
    }

    override fun isHidden(): Boolean = fallback(
        { this.isHidden(path) },
        { super.isHidden() }
    )

    override fun setReadOnly(): Boolean {
        return setPermissions(
            SuFilePermissions.combine(
                SuFilePermissions.OWNER_READ,
                SuFilePermissions.GROUP_READ,
                SuFilePermissions.OTHERS_READ,
            )
        )
    }

    override fun setExecutable(executable: Boolean): Boolean {
        return setPermissions(SuFilePermissions.PERMISSION_755)
    }

    fun setPermissions(permissions: SuFilePermissions): Boolean {
        return this.setPermissions(permissions.value)
    }

    fun setPermissions(permissions: Int): Boolean = fallback(
        { this.setPermissions(path, permissions) },
        { false }
    )

    val parentSuFile: SuFile?
        get() = try {
            val parent = this.parentFile
            if (parent != null && parent.path != this.path) {
                SuFile(parent)
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }

    fun setOwner(uid: Int, gid: Int): Boolean = fallback(
        {
            this.setOwner(path, uid, gid)
        },
        {
            try {
                Os.chown(path, uid, gid)
                true
            } catch (e: ErrnoException) {
                false
            }
        }
    )

    override fun listFiles(): Array<SuFile>? {
        return this.list()?.map { SuFile(it, this) }?.toTypedArray()
    }

    override fun listFiles(filter: FileFilter?): Array<SuFile>? {
        val ss = list()
        val files = ArrayList<SuFile>()
        if (ss == null) return null;
        for (s in ss) {
            val f = SuFile(s, this)
            if ((filter == null) || filter.accept(f)) files.add(f)
        }
        return files.toArray(arrayOfNulls<SuFile>(files.size))
    }

    @Throws(IOException::class)
    fun newInputStream(): InputStream = fallback(
        {
            val pipe = ParcelFileDescriptor.createPipe()
            try {
                this.openReadStream(path, pipe[1]).checkException()
            } catch (e: RemoteException) {
                pipe[0].close()
                throw IOException(e)
            } finally {
                pipe[1].close()
            }

            return@fallback ParcelFileDescriptor.AutoCloseInputStream(pipe[0])
        },
        {
            FileInputStream(this)
        }
    )

    @Throws(IOException::class)
    fun newOutputStream(append: Boolean): OutputStream = fallback(
        {
            val pipe = ParcelFileDescriptor.createPipe()
            try {
                this.openWriteStream(path, pipe[0], append).checkException()
            } catch (e: RemoteException) {
                pipe[1].close()
                throw IOException(e)
            } finally {
                pipe[0].close()
            }
            return@fallback ParcelFileDescriptor.AutoCloseOutputStream(pipe[1])
        },
        {
            FileOutputStream(this)
        }
    )

    @Throws(IOException::class)
    fun getCanonicalDirPath(): String {
        var canonicalPath = this.canonicalPath
        if (!canonicalPath.endsWith("/")) canonicalPath += "/"
        return canonicalPath
    }

    @Throws(IOException::class)
    fun getCanonicalFileIfChild(child: String): SuFile? {
        val parentCanonicalPath = getCanonicalDirPath()
        val childCanonicalPath = SuFile(this, child).canonicalPath
        if (childCanonicalPath.startsWith(parentCanonicalPath)) {
            return SuFile(childCanonicalPath)
        }
        return null
    }

    /**
     * Converts this [SuFile] instance to an [ExtFile] instance.
     *
     * This can be useful when you need to work with a file without requiring root privileges
     * or when interfacing with APIs that expect an [ExtFile].
     *
     * @return An [ExtFile] object representing the same file path as this [SuFile].
     */
    fun toExtFile(): ExtFile {
        return ExtFile(this)
    }

    companion object {
        const val TAG = "SuFile"
        const val PIPE_CAPACITY = 16 * 4096

        fun loadSharedObjects(vararg paths: String): Boolean = fallback(
            {
                this.loadSharedObjects(paths)
            },
            { false }
        )

        fun String.toSuFile(): SuFile {
            return SuFile(this)
        }

        fun createDirectories(vararg file: SuFile): Boolean {
            for (f in file) {
                if (!f.mkdirs()) return false
            }
            return true
        }

        fun Int.toFormattedFileSize(): String = toDouble().toFormattedFileSize()

        fun Long.toFormattedFileSize(): String = toDouble().toFormattedFileSize()

        fun Float.toFormattedFileSize(): String = toDouble().toFormattedFileSize()

        fun Double.toFormattedFileSize(): String {
            if (this < 1024) return "$this B"

            val units = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
            var size = this
            val sizeRepresentation = SuFileSizeRepresentation.getRepresentation(size)
            val base = sizeRepresentation.base.toDouble()
            var unitIndex = 0

            while (size >= base && unitIndex < units.size - 1) {
                size /= base
                unitIndex++
            }

            return if (size == size.toLong().toDouble()) {
                String.format(Locale.getDefault(), "%.0f %s", size, units[unitIndex])
            } else {
                String.format(Locale.getDefault(), "%.2f %s", size, units[unitIndex])
            }
        }

        private fun <R> fallback(
            root: IFileManager.() -> R,
            nonRoot: () -> R,
        ): R {
            val platform = PlatformManager.platform
            val fileManager = PlatformManager.fileManagerOrNull
            try {
                if (fileManager != null && platform.isNotNonRoot) {
                    return root(fileManager)
                }
                return nonRoot()
            } catch (e: Exception) {
                return nonRoot()
            }
        }
    }
}

