package com.dergoogler.mmrl.platform.service

import android.os.Binder
import android.os.IBinder
import android.os.Parcel
import android.os.SELinux
import android.system.Os
import android.util.Log
import com.dergoogler.mmrl.ext.exception.BrickException
import com.dergoogler.mmrl.platform.BINDER_TRANSACTION
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.content.Service
import com.dergoogler.mmrl.platform.file.FileManager
import com.dergoogler.mmrl.platform.manager.APatchModuleManager
import com.dergoogler.mmrl.platform.manager.KernelSUModuleManager
import com.dergoogler.mmrl.platform.manager.KsuNextModuleManager
import com.dergoogler.mmrl.platform.manager.MagiskModuleManager
import com.dergoogler.mmrl.platform.manager.SukiSUModuleManager
import com.dergoogler.mmrl.platform.stub.IFileManager
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.platform.stub.IServiceManager

open class ServiceManager(
    protected val platform: Platform,
) : IServiceManager.Stub() {
    protected val services = hashMapOf<String, IBinder>()

    protected val fileManager by lazy {
        FileManager()
    }

    protected val moduleManager by lazy {
        when (platform) {
            Platform.Magisk -> MagiskModuleManager()

            Platform.RKSU,
            Platform.MKSU,
            Platform.KernelSU,
                -> KernelSUModuleManager()

            Platform.SukiSU,
                -> SukiSUModuleManager()

            Platform.KsuNext,
                -> KsuNextModuleManager()

            Platform.APatch -> APatchModuleManager()

            else -> throw BrickException(
                message = "Unknown platform: $platform",
                helpMessage = "Contact the support",
                helpLink = "https://t.me/MMRLGroup"
            )
        }
    }

    override fun getUid(): Int {
        return Os.getuid()
    }

    override fun getPid(): Int {
        return Os.getpid()
    }

    override fun isSELinuxEnforced(): Boolean = SELinux.isSELinuxEnforced()
    override fun isSELinuxEnabled(): Boolean = SELinux.isSELinuxEnabled()
    override fun getSELinuxContext(): String = SELinux.getContext()

    override fun currentPlatform(): String {
        return platform.name.lowercase()
    }

    override fun getModuleManager(): IModuleManager {
        return moduleManager
    }

    override fun getFileManager(): IFileManager {
        return fileManager
    }

    override fun addService(service: Service<*>): IBinder? =
        runCatching {
            service.create(this).apply {
                services[service.name] = this
            }

        }.onFailure {
            Log.e(TAG, Log.getStackTraceString(it))

        }.getOrNull()

    override fun addServiceBinder(name: String, binder: IBinder) {
        runCatching {
            services[name] = binder
        }.onFailure {
            Log.e(TAG, Log.getStackTraceString(it))

        }.getOrNull()
    }

    override fun getService(name: String): IBinder? = services[name]

    override fun onTransact(code: Int, data: Parcel, reply: Parcel?, flags: Int) =
        if (code == BINDER_TRANSACTION) {
            data.enforceInterface(DESCRIPTOR)
            val targetBinder = data.readStrongBinder()
            val targetCode = data.readInt()
            val targetFlags = data.readInt()
            val newData = Parcel.obtain()

            try {
                newData.appendFrom(data, data.dataPosition(), data.dataAvail())

                val id = Binder.clearCallingIdentity()
                targetBinder.transact(targetCode, newData, reply, targetFlags)
                Binder.restoreCallingIdentity(id)
            } finally {
                newData.recycle()
            }

            true
        } else {
            super.onTransact(code, data, reply, flags)
        }

    protected companion object Default {
        const val TAG = "Platform->ServiceManager"
    }
}
