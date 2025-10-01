package com.dergoogler.mmrl.platform.content

import android.os.IBinder
import android.os.Parcelable
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import com.dergoogler.mmrl.platform.stub.IServiceManager

interface IService {
    val name: String
    fun create(manager: IServiceManager): IBinder
}

@Parcelize
class Service<T : IService>(
    private val className: String
) : Parcelable, IService {
    @IgnoredOnParcel
    private val cls: Class<T> by lazy {
        @Suppress("UNCHECKED_CAST")
        try {
            Class.forName(className, false, Service::class.java.classLoader) as Class<T>
        } catch (_: ClassNotFoundException) {
            // fallback to context class loader if needed
            val cl = Thread.currentThread().contextClassLoader
            Class.forName(className, false, cl) as Class<T>
        }
    }

    @IgnoredOnParcel
    private val original: T by lazy {
        cls.getDeclaredConstructor().let {
            it.isAccessible = true
            it.newInstance()
        }
    }

    override val name: String get() = original.name

    override fun create(manager: IServiceManager): IBinder {
        return original.create(manager)
    }
}