package com.dergoogler.mmrl.platform

import android.app.ActivityThread
import android.content.ComponentName
import android.content.Context
import android.content.ContextWrapper
import android.content.ServiceConnection
import android.os.Build
import android.os.IBinder
import android.os.IInterface
import android.os.Parcel
import android.util.Log
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.platform.PlatformManager.isAlive
import com.dergoogler.mmrl.platform.PlatformManager.isAliveDeferred
import com.dergoogler.mmrl.platform.PlatformManager.isAliveFlow
import com.dergoogler.mmrl.platform.PlatformManager.mService
import com.dergoogler.mmrl.platform.PlatformManager.proxyBy
import com.dergoogler.mmrl.platform.PlatformManager.serviceOrNull
import com.dergoogler.mmrl.platform.PlatformManager.state
import com.dergoogler.mmrl.platform.content.IService
import com.dergoogler.mmrl.platform.content.Service
import com.dergoogler.mmrl.platform.hiddenApi.HiddenPackageManager
import com.dergoogler.mmrl.platform.hiddenApi.HiddenUserManager
import com.dergoogler.mmrl.platform.model.IProvider
import com.dergoogler.mmrl.platform.stub.IFileManager
import com.dergoogler.mmrl.platform.stub.IModuleManager
import com.dergoogler.mmrl.platform.stub.IServiceManager
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.lsposed.hiddenapibypass.HiddenApiBypass
import java.io.FileDescriptor
import kotlin.coroutines.resumeWithException

/**
 * Manages the connection to the underlying platform service (`IServiceManager`).
 *
 * This object provides a centralized way to initialize, access, and interact with
 * the core platform services. It handles both synchronous and asynchronous
 * initialization and provides convenient accessors for various managers
 * (e.g., `IModuleManager`, `IFileManager`).
 *
 * Key functionalities include:
 * - **Initialization**: Offers `init` methods for both suspending (synchronous-like)
 *   and asynchronous (returning a `Deferred`) initialization of the `IServiceManager`.
 *   This allows flexibility in how the service is obtained.
 * - **Service Access**: Provides `mService` (non-null, throws if not initialized) and
 *   `mServiceOrNull` (nullable) properties to access the `IServiceManager`.
 * - **Liveness Tracking**:
 *     - `isAlive`: A `Boolean` property (backed by `mutableStateOf` for Compose UI updates)
 *       indicating if the `IServiceManager` is currently initialized and available.
 *     - `isAliveDeferred`: A `CompletableDeferred<Boolean>` that completes when the service
 *       becomes alive. Useful for waiting for initialization.
 *     - `isAliveFlow`: A `StateFlow<Boolean>` emitting the liveness state, suitable for
 *       reactive programming.
 * - **Service Retrieval from Providers**:
 *     - `get(IProvider)`: Suspends until the `IServiceManager` is obtained from the given
 *       `IProvider` via a `ServiceConnection`, with a timeout.
 *     - `from(IProvider)`: Checks provider availability and authorization before attempting
 *       to get the service using `get(IProvider)`.
 * - **Manager Accessors**: Provides direct access to sub-managers like `moduleManager`,
 *   `fileManager`, `packageManager`, and `userManager` if the `IServiceManager` is alive.
 * - **Platform Information**: Exposes `platform` type (e.g., Root, NonRoot) and `seLinuxContext`.
 * - **Utility Functions**:
 *     - `state()`: Updates and returns the current liveness state.
 *     - `get(fallback, block)`: Executes a block if alive, otherwise returns a fallback.
 */
object PlatformManager {
    const val TAG = "PlatformManager"
    const val TIMEOUT_MILLIS = 15000L

    @Volatile
    var mServiceOrNull: IServiceManager? = null
    val mService
        get() = checkNotNull(mServiceOrNull) {
            "IServiceManager has not been initialized or has been released."
        }

    /**
     * Indicates whether the [IServiceManager] is currently initialized and considered "alive".
     * This property is observable by Compose and will trigger recomposition when its value changes.
     * It is updated by the [state] function.
     *
     * @see state
     * @see isAliveFlow
     * @see isAliveDeferred
     */
    var isAlive by mutableStateOf(false)
        private set

    /**
     * A [CompletableDeferred] that completes with `true` when the [PlatformManager]
     * successfully initializes and establishes a connection with its underlying service.
     * This can be awaited to ensure the manager is ready before performing operations
     * that depend on it.
     *
     * If initialization fails or the service is not available, this deferred
     * might not complete, or it might complete exceptionally if such logic is added
     * in the future.
     */
    val isAliveDeferred = CompletableDeferred<Boolean>()

    private val _isAliveFlow = MutableStateFlow(false)

    /**
     * A [StateFlow] that emits `true` if the [IServiceManager] is initialized and alive,
     * `false` otherwise. This is useful for observing the liveness state in a reactive way.
     */
    val isAliveFlow get() = _isAliveFlow.asStateFlow()

    /**
     * Initializes the [PlatformManager] synchronously with the provided [IServiceManager] instance.
     * This function attempts to set the internal service manager instance (`mServiceOrNull`)
     * using the result of the [provider] lambda.
     *
     * If the [PlatformManager] is already alive (i.e., `isAlive` is true), this function
     * returns `true` immediately without re-initializing.
     *
     * The [provider] lambda is executed within the context of [PlatformManager], allowing
     * access to its members. It should return an instance of [IServiceManager] or `null`.
     *
     * After attempting to initialize, it calls [state] to update the alive status
     * of the [PlatformManager].
     *
     * Catches any [Exception] during the provider execution, sets `mServiceOrNull` to `null`,
     * logs the error, and then updates the state.
     *
     * @param provider A suspendable lambda function that, when invoked, returns an instance
     *                 of [IServiceManager] or `null`. This lambda is executed to obtain the
     *                 service manager.
     * @return `true` if the initialization was successful (i.e., `mServiceOrNull` is not null after
     *         the provider execution) or if the manager was already alive. Returns `false` if
     *         the provider returns `null` or if an exception occurs during initialization.
     */
    suspend inline fun init(
        crossinline provider: suspend PlatformManager.() -> IServiceManager?,
    ): Boolean {
        if (isAlive) {
            return true
        }
        return try {
            Log.d(TAG, "Starting synchronous initialization.")
            mServiceOrNull = provider()
            Log.d(
                TAG,
                "Sync provider executed. mServiceOrNull is: ${if (mServiceOrNull == null) "null" else "not null"}"
            )
            state()
        } catch (e: Exception) {
            mServiceOrNull = null
            Log.e(TAG, "Failed to init service manager (synchronous)", e)
            state()
        }
    }

    /**
     * Asynchronously initializes the [PlatformManager] with the given [provider].
     *
     * This function launches a coroutine in the provided [scope] on the [Dispatchers.IO] dispatcher
     * to perform the initialization. It allows the caller to continue execution without blocking
     * while the initialization happens in the background.
     *
     * If the [PlatformManager] is already alive (initialized), this function returns a
     * [CompletableDeferred] that is already completed with `true`.
     *
     * The [provider] is a suspend lambda function that will be executed to obtain the
     * [IServiceManager] instance. If the provider successfully returns an [IServiceManager],
     * `mServiceOrNull` will be set, and the internal state will be updated.
     * If the provider throws an exception or returns null, `mServiceOrNull` will be set to null,
     * and the initialization will be considered failed.
     *
     * @param scope The [CoroutineScope] in which to launch the asynchronous initialization.
     * @param provider A suspend lambda function that, when executed, returns an [IServiceManager]
     *                 instance or null if initialization fails. This lambda has [PlatformManager]
     *                 as its receiver.
     * @return A [Deferred] of [Boolean] that will complete with `true` if the initialization
     *         was successful (or already initialized), and `false` otherwise. The Deferred
     *         allows the caller to await the result of the asynchronous initialization.
     */
    inline fun init(
        scope: CoroutineScope,
        crossinline provider: suspend PlatformManager.() -> IServiceManager?,
    ): Deferred<Boolean> {
        if (isAlive) {
            Log.d(TAG, "Service manager already alive (async init check).")
            return CompletableDeferred(true)
        }

        return scope.async(Dispatchers.IO) {
            try {
                Log.d(
                    TAG,
                    "Starting background initialization on thread: ${Thread.currentThread().name}"
                )
                mServiceOrNull = provider()
                Log.d(
                    TAG,
                    "Async provider executed. mServiceOrNull is: ${if (mServiceOrNull == null) "null" else "not null"}"
                )
                state()
            } catch (e: Exception) {
                mServiceOrNull = null
                Log.e(TAG, "Failed to init service manager (asynchronous)", e)
                state()
            }
        }
    }

    /**
     * Asynchronously retrieves an [IServiceManager] instance from the given [provider].
     *
     * This function attempts to bind to the service provided by the [IProvider].
     * It uses a [suspendCancellableCoroutine] to bridge the callback-based service connection
     * with coroutine-based asynchronous programming.
     *
     * The binding process is subject to a [timeoutMillis]. If the connection is not established
     * within this timeout, or if any other error occurs during binding (e.g., service disconnected,
     * binding died), the coroutine will resume with an exception.
     *
     * If the coroutine is cancelled while waiting for the service to connect, it will attempt
     * to unbind from the provider.
     *
     * @param provider The [IProvider] implementation that will be used to bind to the service.
     * @param timeoutMillis The maximum time in milliseconds to wait for the service to connect.
     *                      Defaults to [TIMEOUT_MILLIS].
     * @return An instance of [IServiceManager] if the connection is successful.
     * @throws TimeoutCancellationException if the binding process times out.
     * @throws IllegalStateException if the service disconnects or the binding dies.
     * @throws Exception for other errors that might occur during the binding process.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    suspend fun get(
        provider: IProvider,
        timeoutMillis: Long = TIMEOUT_MILLIS,
    ): IServiceManager = withTimeout(timeoutMillis) {
        suspendCancellableCoroutine { continuation ->
            val connection = object : ServiceConnection {
                override fun onServiceConnected(name: ComponentName, binder: IBinder) {
                    Log.d(TAG, "Service connected: $name")
                    val service = IServiceManager.Stub.asInterface(binder)
                    if (continuation.isActive) {
                        continuation.resume(service) {
                            Log.w(
                                TAG,
                                "Failed to resume onServiceConnected, coroutine likely cancelled for $name."
                            )
                        }
                    }
                }

                override fun onServiceDisconnected(name: ComponentName) {
                    Log.w(TAG, "Service disconnected: $name")
                    if (continuation.isActive) {
                        continuation.resumeWithException(IllegalStateException("IServiceManager ($name) disconnected"))
                    }
                }

                override fun onBindingDied(name: ComponentName?) {
                    Log.e(TAG, "Binding died for service: $name")
                    if (continuation.isActive) {
                        continuation.resumeWithException(IllegalStateException("IServiceManager ($name) binding died"))
                    }
                }
            }
            Log.d(TAG, "Binding to provider: ${provider.name}")
            provider.bind(connection)
            continuation.invokeOnCancellation {
                Log.d(TAG, "Coroutine cancelled, unbinding from provider: ${provider.name}")
                try {
                    provider.unbind(connection)
                } catch (e: Exception) {
                    Log.e(TAG, "Error unbinding provider '${provider.name}' on cancellation", e)
                }
            }
        }
    }

    /**
     * Attempts to retrieve an [IServiceManager] from the given [provider].
     *
     * This function checks if the provider is available and authorized before attempting to connect.
     * It operates on the [Dispatchers.Main] context.
     *
     * @param provider The [IProvider] to get the service from.
     * @param timeoutMillis The maximum time in milliseconds to wait for the service connection.
     *                      Defaults to [TIMEOUT_MILLIS].
     * @return The connected [IServiceManager].
     * @throws IllegalStateException if the provider is not available, not authorized,
     * or if the connection times out or fails for other reasons.
     */
    @Throws(IllegalStateException::class)
    suspend fun from(
        provider: IProvider,
        timeoutMillis: Long = TIMEOUT_MILLIS,
    ): IServiceManager = withContext(Dispatchers.Main) {
        Log.d(TAG, "Attempting to get service from provider: ${provider.name}")
        when {
            !provider.isAvailable() -> {
                Log.w(TAG, "Provider ${provider.name} not available.")
                throw IllegalStateException("${provider.name} not available")
            }

            !provider.isAuthorized() -> {
                Log.w(TAG, "Provider ${provider.name} not authorized.")
                throw IllegalStateException("${provider.name} not authorized")
            }

            else -> {
                Log.d(
                    TAG,
                    "Provider ${provider.name} is available and authorized. Getting service."
                )
                get(provider, timeoutMillis)
            }
        }
    }

    /**
     * Provides access to the module management functionalities.
     *
     * This property is a delegate to the `moduleManager` property of the underlying [IServiceManager].
     * It allows interaction with modules, such as listing, enabling, or disabling them.
     *
     * @throws IllegalStateException if [IServiceManager] has not been initialized or has been released.
     * @see IModuleManager
     * @see mService
     */
    val moduleManager: IModuleManager get() = mService.moduleManager

    /**
     * Provides access to the file management functionalities.
     * This property delegates to the `fileManager` of the underlying `IServiceManager`.
     *
     * @return An instance of [IFileManager] for interacting with the file system.
     * @throws IllegalStateException if `IServiceManager` has not been initialized.
     */
    val fileManager: IFileManager get() = mService.fileManager

    internal val fileManagerOrNull: IFileManager? get() = mServiceOrNull?.fileManager

    /**
     * Provides access to a [HiddenPackageManager] instance, which allows interaction
     * with package management functionalities that might otherwise be restricted
     * by Android's hidden API policies.
     *
     * This property leverages the underlying [mService] (an [IServiceManager])
     * to facilitate these operations.
     *
     * @throws IllegalStateException if the [mService] has not been initialized.
     * @return A [HiddenPackageManager] instance.
     */
    val packageManager: HiddenPackageManager get() = HiddenPackageManager(this.mService)

    /**
     * Provides access to user-related operations through a [HiddenUserManager].
     * This manager allows interaction with user management functionalities that
     * might be otherwise restricted by standard Android APIs.
     *
     * It requires the [mService] to be initialized and available.
     *
     * @return An instance of [HiddenUserManager] for interacting with user services.
     * @throws IllegalStateException if [mService] has not been initialized.
     */
    val userManager: HiddenUserManager get() = HiddenUserManager(this.mService)

    /**
     * Retrieves the SELinux context of the remote service.
     * This can be useful for diagnostics and security-related checks.
     *
     * @return The SELinux context string if the service is alive and provides it,
     * otherwise behavior might depend on the `mService` implementation (e.g., throw exception).
     * @throws IllegalStateException if `mService` is null (i.e., [isAlive] is false).
     */
    val seLinuxContext: String get() = mService.seLinuxContext

    /**
     * Checks if SELinux is enabled on the system.
     *
     * This property queries the underlying `IServiceManager` to determine the SELinux status.
     *
     * @return `true` if SELinux is enabled, `false` if it is disabled, or `null` if the
     *         status cannot be determined (e.g., if the service is not available or the
     *         method is not implemented by the service).
     * @throws IllegalStateException if `mService` is null (i.e., [isAlive] is false),
     *         depending on the `mService` implementation.
     */
    val isSELinuxEnabled: Boolean get() = mService.isSELinuxEnabled()

    /**
     * Indicates whether SELinux is currently in enforcing mode on the device,
     * as reported by the underlying service.
     *
     * This property delegates to the `isSELinuxEnforced()` method of the [mService].
     *
     * @return `true` if SELinux is enforced, `false` if it's permissive or disabled,
     *         or `null` if the state cannot be determined (e.g., service not connected
     *         or an error occurs during the call).
     * @throws IllegalStateException if `mService` is null (i.e., [isAlive] is false),
     *         unless the underlying `mService.isSELinuxEnforced()` implementation
     *         handles this gracefully (which is not guaranteed by the interface).
     */
    val isSELinuxEnforced: Boolean get() = mService.isSELinuxEnforced()

    /**
     * Gets the current platform information.
     * This property attempts to retrieve the platform details from the underlying service.
     * If the service is not available or an error occurs during the retrieval,
     * it defaults to [Platform.Unknown].
     *
     * @return The current [Platform], or [Platform.Unknown] if an error occurs or the service is unavailable.
     */
    val platform: Platform
        get() = serviceOrNull(Platform.Unknown) {
            try {
                Platform.from(currentPlatform())
            } catch (e: Exception) {
                Log.e(TAG, "Error getting current platform from service, defaulting to NonRoot.", e)
                Platform.Unknown
            }
        }

    val type get() = platform.type

    /**
     * Updates the internal state of the PlatformManager based on whether the service manager (`mServiceOrNull`) is initialized.
     * This function should be called after any operation that might change the service manager's status (e.g., initialization, release).
     *
     * - Sets the `_isAliveFlow` value, which is a StateFlow that external components can observe.
     * - Updates the `isAlive` mutable state, which is primarily used for Compose UI updates.
     * - Completes the `isAliveDeferred` if it's not already completed and the service is alive. This is useful for one-time await operations.
     *
     * @return `true` if the service manager is initialized (alive), `false` otherwise.
     */
    suspend fun state(): Boolean {
        val currentService = mServiceOrNull
        val aliveStatus = currentService != null
        _isAliveFlow.value = aliveStatus
        withContext(Dispatchers.Main.immediate) {
            isAlive = aliveStatus
        }
        if (aliveStatus && !isAliveDeferred.isCompleted) {
            isAliveDeferred.complete(true)
        }
        Log.d(TAG, "State updated. isAlive: $aliveStatus")
        return aliveStatus
    }

    /**
     * Executes a block of code with the PlatformManager as its receiver if the service is alive,
     * otherwise returns a fallback value.
     *
     * This function provides a safe way to interact with PlatformManager features that
     * depend on the underlying service being active. If `isAlive` is true, the `block`
     * is executed in the context of `PlatformManager` (i.e., `this` inside the block
     * refers to `PlatformManager`), and its result is returned. If `isAlive` is false,
     * the `fallback` value is returned directly without executing the block.
     *
     * @param T The type of the value to be returned.
     * @param fallback The value to return if the PlatformManager's service is not alive.
     * @param block A lambda function that takes `PlatformManager` as its receiver and returns a value of type `T`.
     *              This block will only be executed if the service is alive.
     * @return The result of the `block` if the service is alive, or the `fallback` value otherwise.
     */
    fun <T> get(fallback: T, block: PlatformManager.() -> T): T {
        return if (isAlive) block(this) else fallback
    }

    /**
     * Asynchronously executes a block of code if the [PlatformManager] is alive.
     *
     * This function launches a new coroutine in the provided [scope] using [Dispatchers.IO].
     * If [PlatformManager.isAlive] is true, the [block] is executed.
     * If the [block] throws an exception, it is caught, logged, and the [fallback] value is returned.
     * If [PlatformManager.isAlive] is false, the [fallback] value is returned directly.
     *
     * This function is useful for performing operations that depend on the [PlatformManager]
     * being initialized and available, without blocking the calling thread. The
     * `@DisallowComposableCalls` annotation ensures this function is not called from
     * a Composable context where suspension might not be handled correctly.
     *
     * @param T The type of the value returned by the [block] and the [fallback].
     * @param scope The [CoroutineScope] in which to launch the asynchronous operation.
     * @param fallback The value to return if the [PlatformManager] is not alive or if the [block] throws an exception.
     * @param block A suspendable lambda function that will be executed if the [PlatformManager] is alive.
     *              It receives the [PlatformManager] instance as its receiver.
     * @return A [Deferred] representing the future result of the asynchronous operation.
     *         The deferred will complete with the result of the [block] or the [fallback] value.
     */
    inline fun <T> getAsyncDeferred(
        scope: CoroutineScope,
        fallback: T,
        crossinline block: @DisallowComposableCalls suspend PlatformManager.() -> T,
    ): Deferred<T> {
        return scope.async(Dispatchers.IO) {
            if (isAlive) {
                try {
                    block()
                } catch (e: Exception) {
                    Log.e(TAG, "Error in getAsyncDeferred block execution", e)
                    fallback
                }
            } else {
                fallback
            }
        }
    }

    /**
     * Sets Hidden API exemptions using [HiddenApiBypass.addHiddenApiExemptions].
     *
     * This function attempts to bypass Android's restrictions on accessing non-SDK interfaces (hidden APIs)
     * by adding the specified signature prefixes to an exemption list. This is primarily useful on
     * Android P (API level 28) and above, where these restrictions are enforced more strictly.
     *
     * On SDK versions below P, this function does nothing and returns `true` as exemptions are not needed.
     *
     * @param signaturePrefixes A vararg array of strings, where each string is a prefix of a hidden API
     *                          signature to be exempted. For example, "Landroid/app/ActivityThread;"
     *                          would exempt all members of the `ActivityThread` class. If no prefixes
     *                          are provided (i.e., an empty array or `arrayOf("")`), it might attempt
     *                          to exempt all hidden APIs, depending on the `HiddenApiBypass` library's
     *                          behavior.
     * @return `true` if the exemptions were successfully added (or not needed for the current SDK version),
     *         `false` otherwise (e.g., if `HiddenApiBypass.addHiddenApiExemptions` returns `false`).
     * @see HiddenApiBypass.addHiddenApiExemptions
     */
    fun setHiddenApiExemptions(vararg signaturePrefixes: String = arrayOf("")): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Log.d(
                TAG,
                "Setting Hidden API exemptions with prefixes: ${signaturePrefixes.joinToString()}"
            )
            HiddenApiBypass.addHiddenApiExemptions(*signaturePrefixes)
        } else {
            Log.d(TAG, "Hidden API exemptions not needed on SDK < P.")
            true
        }
    }

    fun <T : IBinder> T.proxyBy(service: IServiceManager): IBinder = object : IBinder {
        private val originalBinder: IBinder = this@proxyBy
        private val serviceBinder: IBinder = service.asBinder()

        override fun getInterfaceDescriptor(): String? = originalBinder.interfaceDescriptor
        override fun pingBinder(): Boolean = originalBinder.pingBinder()
        override fun isBinderAlive(): Boolean =
            originalBinder.isBinderAlive && serviceBinder.isBinderAlive

        override fun queryLocalInterface(descriptor: String): IInterface? = null
        override fun dump(fd: FileDescriptor, args: Array<out String>?) =
            originalBinder.dump(fd, args)

        override fun dumpAsync(fd: FileDescriptor, args: Array<out String>?) =
            originalBinder.dumpAsync(fd, args)

        override fun linkToDeath(recipient: IBinder.DeathRecipient, flags: Int) {
            originalBinder.linkToDeath(recipient, flags)
        }

        override fun unlinkToDeath(recipient: IBinder.DeathRecipient, flags: Int): Boolean {
            return originalBinder.unlinkToDeath(recipient, flags)
        }

        override fun transact(code: Int, data: Parcel, reply: Parcel?, flags: Int): Boolean {
            if (!serviceBinder.isBinderAlive) {
                Log.e(TAG, "Proxy transact: ServiceManager is dead.")
                return false
            }
            val newData = Parcel.obtain()
            var result = false
            try {
                newData.apply {
                    writeInterfaceToken(IServiceManager.DESCRIPTOR)
                    writeStrongBinder(originalBinder)
                    writeInt(code)
                    writeInt(flags)
                    if (data.dataSize() > 0) {
                        appendFrom(data, 0, data.dataSize())
                    }
                }

                result = serviceBinder.transact(BINDER_TRANSACTION, newData, reply, 0)
            } catch (e: Exception) {
                Log.e(TAG, "Exception during proxy transact", e)
                throw e
            } finally {
                newData.recycle()
            }
            return result
        }
    }

    /**
     * Adds a service defined by its class to the PlatformManager.
     *
     * This function attempts to add the specified service to the underlying `IServiceManager`.
     * If the `IServiceManager` is not initialized (i.e., `mServiceOrNull` is null),
     * this function will return `null` without attempting to add the service.
     *
     * @param T The type of the service, which must extend [IService].
     * @param clazz The [Class] object representing the service to be added.
     * @return An [IBinder] representing the added service if successful and the PlatformManager is initialized,
     *         otherwise `null`.
     * @see serviceOrNull
     * @see Service
     */
    fun <T : IService> addService(clazz: Class<T>): IBinder? = serviceOrNull {
        addService(Service<T>(clazz.name))
    }

    /**
     * Adds a service to the IServiceManager.
     *
     * This function attempts to add the provided service to the IServiceManager.
     * If the IServiceManager is not initialized (mServiceOrNull is null), this function
     * will return null. Otherwise, it delegates the call to the IServiceManager's
     * addService method.
     *
     * @param T The type of the service, which must extend IService.
     * @param service The Service object containing the service to be added.
     * @return An IBinder representing the added service if successful, or null otherwise
     *         (e.g., if the IServiceManager is not initialized or the addition fails).
     * @see IServiceManager.addService
     * @see serviceOrNull
     */
    fun <T : IService> addService(service: Service<T>): IBinder? = serviceOrNull {
        addService(service)
    }

    fun <T : IInterface> addService(name: String, service: T) {
        serviceOrNull {
            addServiceBinder(name, service.asBinder())
        }
    }

    /**
     * Extension function for `Class<T>` where `T` is an `IService`.
     * Adds an instance of this class as a service through the PlatformManager's IServiceManager.
     *
     * This is a convenience function that wraps the class in a `Service` object before adding it.
     *
     * @receiver The class of the service to be added.
     * @return An `IBinder` representing the added service if the `IServiceManager` is available and the operation succeeds, otherwise `null`.
     * @see PlatformManager.addService
     * @see Service
     */
    fun <T : IService> Class<T>.addAsService(): IBinder? = serviceOrNull {
        addService(Service<T>(this@addAsService.name))
    }

    /**
     * Retrieves a system service by its name.
     *
     * This function attempts to get the IBinder for a system service identified by the `name` parameter.
     * It relies on the underlying `IServiceManager` to perform the lookup.
     *
     * @param name The name of the system service to retrieve (e.g., "activity", "package").
     * @return The IBinder interface for the service if found and the `IServiceManager` is available,
     *         otherwise `null`.
     */
    fun getService(name: String): IBinder? = serviceOrNull {
        getService(name)
    }

    fun <T : IInterface> T.proxyBy(service: IServiceManager): IBinder =
        this.asBinder().proxyBy(service)

    /**
     * Retrieves a system service by its name and wraps it in a proxy.
     *
     * This function uses the `android.os.ServiceManager` to get the raw `IBinder` for the requested system service.
     * If the service is found (i.e., `systemServiceBinder` is not null), it then proxies this binder
     * through the `PlatformManager`'s `IServiceManager` instance (`this@PlatformManager.mService`).
     * This proxying mechanism is typically used to route binder calls through a central service manager,
     * potentially for security, logging, or other cross-cutting concerns.
     *
     * @param T The type of the `IInterface` that this function is an extension for.
     *          While the receiver `T` is not directly used to query the service, it provides the context
     *          for this extension function.
     * @param name The string name of the system service to retrieve (e.g., "activity", "package").
     * @return The proxied `IBinder` for the requested system service if found and the `PlatformManager`'s
     *         service is available, otherwise `null`.
     * @see android.os.ServiceManager.getService
     * @see proxyBy
     */
    fun <T : IInterface> T.getSystemService(name: String): IBinder? {
        val systemServiceBinder = android.os.ServiceManager.getService(name)
        return systemServiceBinder?.proxyBy(this@PlatformManager.mService)
    }

    fun <T> serviceOrNull(default: T, block: IServiceManager.() -> T): T =
        mServiceOrNull?.let { block(it) } ?: default

    fun <T> serviceOrNull(block: IServiceManager.() -> T): T? =
        mServiceOrNull?.let { block(it) }

    val context: Context
        @Throws(IllegalStateException::class)
        get() {
            val currentApp = ActivityThread.currentApplication()
                ?: throw IllegalStateException("Application is not initialized yet.")
            var ctx: Context = currentApp
            while (ctx is ContextWrapper) {
                ctx = ctx.baseContext ?: return ctx
            }
            return ctx
        }
}