package dev.dergoogler.mmrl.compat

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.os.IBinder
import com.dergoogler.mmrl.datastore.model.WorkingMode
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import dev.dergoogler.mmrl.compat.impl.ServiceManagerImpl
import dev.dergoogler.mmrl.compat.stub.IServiceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.lsposed.hiddenapibypass.HiddenApiBypass
import rikka.shizuku.Shizuku
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class ServiceManagerCompat(
    private val context: Context
) {
    interface IProvider {
        val name: String
        fun isAvailable(): Boolean
        suspend fun isAuthorized(): Boolean
        fun bind(connection: ServiceConnection)
        fun unbind(connection: ServiceConnection)
    }

    private suspend fun get(
        provider: IProvider,
    ) = withTimeout(TIMEOUT_MILLIS) {
        suspendCancellableCoroutine { continuation ->
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    val service = IServiceManager.Stub.asInterface(binder)
                    continuation.resume(service)
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    continuation.resumeWithException(
                        IllegalStateException("IServiceManager destroyed")
                    )
                }

                override fun onBindingDied(name: ComponentName?) {
                    continuation.resumeWithException(
                        IllegalStateException("IServiceManager destroyed")
                    )
                }
            }

            provider.bind(connection)
            continuation.invokeOnCancellation {
                provider.unbind(connection)
            }
        }
    }

    suspend fun from(provider: IProvider): IServiceManager = withContext(Dispatchers.Main) {
        when {
            !provider.isAvailable() -> throw IllegalStateException("${provider.name} not available")
            !provider.isAuthorized() -> throw IllegalStateException("${provider.name} not authorized")
            else -> get(provider)
        }
    }

    private class ShizukuService(context: Context) : Shizuku.UserServiceArgs(
        ComponentName(
            context.packageName,
            ServiceManagerImpl::class.java.name
        )
    ) {
        init {
            daemon(false)
            debuggable(false)
            version(VERSION_CODE)
            processNameSuffix("shizuku")
        }
    }

    private class ShizukuProvider(private val context: Context) : IProvider {
        override val name = "Shizuku"

        override fun isAvailable(): Boolean {
            return Shizuku.pingBinder() && Shizuku.getUid() == 0
        }

        override suspend fun isAuthorized() = when {
            isGranted -> true
            else -> suspendCancellableCoroutine { continuation ->
                val listener = object : Shizuku.OnRequestPermissionResultListener {
                    override fun onRequestPermissionResult(
                        requestCode: Int,
                        grantResult: Int,
                    ) {
                        Shizuku.removeRequestPermissionResultListener(this)
                        continuation.resume(isGranted)
                    }
                }

                Shizuku.addRequestPermissionResultListener(listener)
                continuation.invokeOnCancellation {
                    Shizuku.removeRequestPermissionResultListener(listener)
                }
                Shizuku.requestPermission(listener.hashCode())
            }
        }

        override fun bind(connection: ServiceConnection) {
            Shizuku.bindUserService(ShizukuService(context), connection)
        }

        override fun unbind(connection: ServiceConnection) {
            Shizuku.unbindUserService(ShizukuService(context), connection, true)
        }

        private val isGranted get() = Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED
    }

    suspend fun fromShizuku() = from(ShizukuProvider(context))

    private class SuService : RootService() {
        override fun onBind(intent: Intent): IBinder {
            val mode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getSerializableExtra(WORKING_MODE_KEY, WorkingMode::class.java)
                    ?: WorkingMode.MODE_NON_ROOT
            } else {
                @Suppress("DEPRECATION")
                intent.getSerializableExtra(WORKING_MODE_KEY) as WorkingMode
            }

            return ServiceManagerImpl(mode)
        }

    }

    private class LibSuProvider(
        private val context: Context,
        private val mode: WorkingMode
    ) : IProvider {
        override val name = "LibSu"

        init {
            Shell.enableVerboseLogging = true
            Shell.setDefaultBuilder(
                Shell.Builder.create()
                    .setInitializers(SuShellInitializer::class.java)
                    .setTimeout(10)
            )
        }

        override fun isAvailable() = true

        override suspend fun isAuthorized() = suspendCancellableCoroutine { continuation ->
            Shell.EXECUTOR.submit {
                runCatching {
                    Shell.getShell()
                }.onSuccess {
                    continuation.resume(true)
                }.onFailure {
                    continuation.resume(false)
                }
            }
        }

        override fun bind(connection: ServiceConnection) {
            RootService.bind(getWorkingModeIntent(context, mode), connection)
        }

        override fun unbind(connection: ServiceConnection) {
            RootService.stop(getWorkingModeIntent(context, mode))
        }

        private class SuShellInitializer : Shell.Initializer() {
            override fun onInit(context: Context, shell: Shell) = shell.isRoot
        }
    }

    suspend fun fromLibSu(mode: WorkingMode) = from(LibSuProvider(context, mode))

    companion object {
        internal const val VERSION_CODE = 1
        private const val TIMEOUT_MILLIS = 15_000L

        private const val WORKING_MODE_KEY = "WORKING_MODE"

        fun getWorkingModeIntent(context: Context, mode: WorkingMode) = Intent().apply {
            component = ComponentName(
                context.packageName,
                SuService::class.java.name
            )
            putExtra(WORKING_MODE_KEY, mode)
        }

        fun setHiddenApiExemptions() = when {
            BuildCompat.atLeastP -> HiddenApiBypass.addHiddenApiExemptions("")
            else -> true
        }
    }
}