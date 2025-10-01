package com.dergoogler.mmrl.platform.file

import android.annotation.SuppressLint
import android.os.ParcelFileDescriptor
import android.os.RemoteException
import android.system.ErrnoException
import android.system.Os
import android.system.OsConstants
import android.system.OsConstants.O_APPEND
import android.system.OsConstants.O_CREAT
import android.system.OsConstants.O_RDONLY
import android.system.OsConstants.O_TRUNC
import android.system.OsConstants.O_WRONLY
import android.util.LruCache
import com.dergoogler.mmrl.platform.content.ParcelResult
import com.dergoogler.mmrl.platform.stub.IFileManager
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class FileManager : IFileManager.Stub() {
    init {
        System.loadLibrary("mmrl-file-manager")
    }

    private val mCache: LruCache<String, File> = object : LruCache<String, File>(100) {
        override fun create(key: String): File {
            return File(key)
        }
    }

    private external fun nativeSetOwner(path: String, owner: Int, group: Int): Boolean
    private external fun nativeSetPermissions(path: String, mode: Int): Boolean

    override fun deleteOnExit(path: String) = with(File(path)) {
        when {
            !exists() -> false
            isFile -> delete()
            isDirectory -> deleteRecursively()
            else -> false
        }
    }

    override fun list(path: String): Array<String>? = mCache.get(path).list()

    override fun length(path: String): Long = mCache.get(path).length()

    override fun stat(path: String): Long = mCache.get(path).lastModified()
    override fun delete(path: String): Boolean {
        val f = mCache.get(path)

        return when {
            !f.exists() -> false
            f.isFile -> f.delete()
            f.isDirectory -> f.deleteRecursively()
            else -> false
        }
    }

    override fun exists(path: String): Boolean = mCache.get(path).exists()
    override fun isDirectory(path: String): Boolean = mCache.get(path).isDirectory
    override fun isFile(path: String): Boolean = mCache.get(path).isFile

    override fun isBlock(path: String): Boolean {
        return try {
            OsConstants.S_ISBLK(getMode(path))
        } catch (e: RemoteException) {
            false
        }
    }

    override fun isCharacter(path: String): Boolean {
        return try {
            OsConstants.S_ISCHR(getMode(path))
        } catch (e: RemoteException) {
            false
        }
    }

    override fun isSymlink(path: String): Boolean {
        return try {
            OsConstants.S_ISLNK(getMode(path))
        } catch (e: RemoteException) {
            false
        }
    }

    override fun isNamedPipe(path: String): Boolean {
        return try {
            OsConstants.S_ISFIFO(getMode(path))
        } catch (e: RemoteException) {
            false
        }
    }

    override fun isSocket(path: String): Boolean {
        return try {
            OsConstants.S_ISSOCK(getMode(path))
        } catch (e: RemoteException) {
            false
        }
    }

    override fun mkdir(path: String): Boolean = mCache.get(path).mkdir()
    override fun mkdirs(path: String): Boolean = mCache.get(path).mkdirs()
    override fun createNewFile(path: String): Boolean = mCache.get(path).createNewFile()
    override fun renameTo(target: String, dest: String): Boolean =
        mCache.get(target).renameTo(mCache.get(dest))

    override fun copyTo(
        path: String,
        target: String,
        overwrite: Boolean,
    ) {
        mCache.get(path).copyTo(mCache.get(target), overwrite)
    }

    override fun canExecute(path: String): Boolean = mCache.get(path).canExecute()
    override fun canWrite(path: String): Boolean = mCache.get(path).canWrite()
    override fun canRead(path: String): Boolean = mCache.get(path).canRead()
    override fun isHidden(path: String): Boolean = mCache.get(path).isHidden
    override fun setPermissions(path: String, mode: Int): Boolean =
        nativeSetPermissions(path, mode)

    override fun setOwner(path: String, owner: Int, group: Int): Boolean =
        nativeSetOwner(path, owner, group)

    override fun parcelFile(filePath: String): ParcelFileDescriptor {
        return ParcelFileDescriptor.open(File(filePath), ParcelFileDescriptor.MODE_READ_ONLY)
    }

    private val streamPool: ExecutorService = Executors.newCachedThreadPool()

    override fun openReadStream(path: String, fd: ParcelFileDescriptor): ParcelResult {
        val f = OpenFile()
        try {
            f.fd = Os.open(path, O_RDONLY, 0)
            streamPool.execute {
                runCatching {
                    f.use { of ->
                        of.write = FileUtils.createFileDescriptor(fd.detachFd())
                        while (of.pread(SuFile.PIPE_CAPACITY, -1) > 0);
                    }
                }
            }
            return ParcelResult()
        } catch (e: ErrnoException) {
            f.close()
            return ParcelResult(e)
        }
    }

    override fun openWriteStream(
        path: String,
        fd: ParcelFileDescriptor,
        append: Boolean,
    ): ParcelResult {
        val f = OpenFile()
        try {
            val mode = O_CREAT or O_WRONLY or (if (append) O_APPEND else O_TRUNC)
            f.fd = Os.open(path, mode, 438)
            streamPool.execute {
                runCatching {
                    f.use { of ->
                        of.read = FileUtils.createFileDescriptor(fd.detachFd())
                        while (of.pwrite(SuFile.PIPE_CAPACITY.toLong(), -1, false) > 0);
                    }
                }
            }
            return ParcelResult()
        } catch (e: ErrnoException) {
            f.close()
            return ParcelResult(e)
        }
    }

    override fun getMode(path: String?): Int {
        return try {
            Os.lstat(path).st_mode
        } catch (e: ErrnoException) {
            0
        }
    }

    @SuppressLint("DiscouragedPrivateApi")
    override fun loadSharedObjects(path: Array<String>): Boolean = nativeLoadSharedObjects(path)
    private external fun nativeLoadSharedObjects(path: Array<String>): Boolean
}
