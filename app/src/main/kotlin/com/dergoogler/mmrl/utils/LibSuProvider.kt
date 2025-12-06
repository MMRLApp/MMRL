package com.dergoogler.mmrl.utils

import android.content.Context
import android.content.ServiceConnection
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.Platform.Companion.createPlatformIntent
import com.dergoogler.mmrl.platform.PlatformManager
import com.dergoogler.mmrl.platform.model.IProvider
import com.dergoogler.mmrl.platform.stub.IServiceManager
import com.topjohnwu.superuser.Shell
import com.topjohnwu.superuser.ipc.RootService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

class LibSuProvider(
    private val context: Context,
    private val platform: Platform,
) : IProvider {
    override val name = "LibSu"

    override fun isAvailable() = true

    override suspend fun isAuthorized() =
        suspendCancellableCoroutine { continuation ->
            Shell.EXECUTOR.execute {
                runCatching {
                    Shell.getShell()
                }.onSuccess {
                    continuation.resume(true)
                }.onFailure {
                    continuation.resume(false)
                }
            }
        }

    private val serviceIntent
        get() = context.createPlatformIntent<SuService>(platform)

    override fun bind(connection: ServiceConnection) {
        RootService.bind(serviceIntent, connection)
    }

    override fun unbind(connection: ServiceConnection) {
        RootService.stop(serviceIntent)
    }
}

private suspend fun init(
    platform: Platform,
    context: Context,
    self: PlatformManager,
): IServiceManager? {
    val provider = LibSuProvider(context, platform)

    if (platform.isNonRoot) {
        return null
    }

    return self.from(provider)
}

suspend fun initPlatform(
    context: Context,
    platform: Platform,
) = PlatformManager.init {
    init(platform, context, this)
}

suspend fun initPlatform(
    scope: CoroutineScope,
    context: Context,
    platform: Platform,
) = PlatformManager.init(scope) {
    init(platform, context, this)
}
