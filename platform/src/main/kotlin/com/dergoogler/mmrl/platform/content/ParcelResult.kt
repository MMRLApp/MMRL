package com.dergoogler.mmrl.platform.content

import android.os.Parcel
import android.os.Parcelable
import java.io.IOException

class ParcelResult : Parcelable {
    val `val`: Any?

    constructor() {
        `val` = null
    }

    constructor(v: Any?) {
        `val` = v
    }

    override fun writeToParcel(
        dest: Parcel,
        flags: Int,
    ) {
        dest.writeValue(`val`)
    }

    @Throws(IOException::class)
    fun checkException() {
        if (`val` is Throwable) throw IOException(REMOTE_ERR_MSG, `val` as Throwable?)
    }

    @Throws(IOException::class)
    inline fun <reified T> tryAndGet(): T? {
        checkException()
        return `val` as? T
    }

    override fun describeContents(): Int = 0

    private constructor(`in`: Parcel) {
        `val` = `in`.readValue(cl)
    }

    companion object {
        private const val REMOTE_ERR_MSG = "Exception thrown on remote process"
        private val cl: ClassLoader? = ParcelResult::class.java.classLoader

        @JvmField
        val CREATOR: Parcelable.Creator<ParcelResult?> =
            object : Parcelable.Creator<ParcelResult?> {
                override fun createFromParcel(`in`: Parcel): ParcelResult = ParcelResult(`in`)

                override fun newArray(size: Int): Array<ParcelResult?> = arrayOfNulls(size)
            }
    }
}
