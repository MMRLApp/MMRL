package com.dergoogler.mmrl.platform.file

import android.os.Build
import android.system.ErrnoException
import android.system.Int64Ref
import android.system.Os
import android.system.OsConstants
import android.system.StructStat
import android.util.MutableLong
import java.io.Closeable
import java.io.FileDescriptor
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import kotlin.math.min

internal class OpenFile : Closeable {
    var fd: FileDescriptor? = null
    var read: FileDescriptor? = null
    var write: FileDescriptor? = null

    private var buf: ByteBuffer? = null
    private var st: StructStat? = null

    private fun getBuf(): ByteBuffer? {
        if (buf == null) buf = ByteBuffer.allocateDirect(SuFile.PIPE_CAPACITY)
        buf!!.clear()
        return buf
    }

    @get:Throws(ErrnoException::class)
    private val stat: StructStat?
        get() {
            if (st == null) st = Os.fstat(fd)
            return st
        }

    @Throws(ClosedChannelException::class)
    private fun ensureOpen() {
        if (fd == null) throw ClosedChannelException()
    }

    @Synchronized
    override fun close() {
        if (fd != null) {
            try {
                Os.close(fd)
            } catch (ignored: ErrnoException) {
            }
            fd = null
        }
        if (read != null) {
            try {
                Os.close(read)
            } catch (ignored: ErrnoException) {
            }
            read = null
        }
        if (write != null) {
            try {
                Os.close(write)
            } catch (ignored: ErrnoException) {
            }
            write = null
        }
    }

    @Synchronized
    @Throws(ErrnoException::class, IOException::class)
    fun lseek(
        offset: Long,
        whence: Int,
    ): Long {
        ensureOpen()
        return Os.lseek(fd, offset, whence)
    }

    @Synchronized
    @Throws(ErrnoException::class, IOException::class)
    fun size(): Long {
        ensureOpen()
        val cur = Os.lseek(fd, 0, OsConstants.SEEK_CUR)
        Os.lseek(fd, 0, OsConstants.SEEK_END)
        val sz = Os.lseek(fd, 0, OsConstants.SEEK_CUR)
        Os.lseek(fd, cur, OsConstants.SEEK_SET)
        return sz
    }

    @Synchronized
    @Throws(ErrnoException::class, IOException::class)
    fun ftruncate(length: Long) {
        ensureOpen()
        Os.ftruncate(fd, length)
    }

    @Synchronized
    @Throws(ErrnoException::class, IOException::class)
    fun sync(metadata: Boolean) {
        ensureOpen()
        if (metadata) {
            Os.fsync(fd)
        } else {
            Os.fdatasync(fd)
        }
    }

    @Synchronized
    @Throws(ErrnoException::class, IOException::class)
    fun pread(
        len: Int,
        offset: Long,
    ): Int {
        if (fd == null || write == null) throw ClosedChannelException()
        val result: Long
        if (!FORCE_NO_SPLICE && Build.VERSION.SDK_INT >= 28) {
            val inOff = if (offset < 0) null else Int64Ref(offset)
            result = FileUtils.splice(fd, inOff, write, null, len.toLong(), 0)
        } else {
            val st = stat
            if (OsConstants.S_ISREG(st!!.st_mode) ||
                OsConstants.S_ISBLK(
                    st.st_mode,
                )
            ) {
                // sendfile only supports reading from mmap-able files
                val inOff = if (offset < 0) null else MutableLong(offset)
                result = FileUtils.sendfile(write, fd, inOff, len.toLong())
            } else {
                // Fallback to copy into internal buffer
                val buf = getBuf()
                buf!!.limit(min(len.toDouble(), buf.capacity().toDouble()).toInt())
                if (offset < 0) {
                    Os.read(fd, buf)
                } else {
                    Os.pread(fd, buf, offset)
                }
                buf.flip()
                result = buf.remaining().toLong()
                // Need to write all bytes
                var sz = result.toInt()
                while (sz > 0) {
                    sz -= Os.write(write, buf)
                }
            }
        }
        return result.toInt()
    }

    @Synchronized
    @Throws(ErrnoException::class, IOException::class)
    fun pwrite(
        len: Long,
        offset: Long,
        exact: Boolean,
    ): Long {
        var len = len
        var offset = offset
        if (fd == null || read == null) throw ClosedChannelException()
        if (!FORCE_NO_SPLICE && Build.VERSION.SDK_INT >= 28) {
            val outOff = if (offset < 0) null else Int64Ref(offset)
            if (exact) {
                var sz = len
                while (sz > 0) {
                    sz -= FileUtils.splice(read, null, fd, outOff, sz, 0)
                }
                return len
            } else {
                return FileUtils.splice(read, null, fd, outOff, len, 0)
            }
        } else {
            // Unfortunately, sendfile does not allow reading from pipes.
            // Manually read into an internal buffer then write to output.
            val buf = getBuf()
            var sz = 0
            buf!!.limit(len.toInt())
            if (exact) {
                while (len > sz) {
                    sz += Os.read(read, buf)
                }
            } else {
                sz = Os.read(read, buf)
            }
            len = sz.toLong()
            buf.flip()
            while (sz > 0) {
                if (offset < 0) {
                    sz -= Os.write(fd, buf)
                } else {
                    val w = Os.pwrite(fd, buf, offset)
                    sz -= w
                    offset += w.toLong()
                }
            }
            return len
        }
    }

    companion object {
        // This is only for testing purpose
        private const val FORCE_NO_SPLICE = false
    }
}
