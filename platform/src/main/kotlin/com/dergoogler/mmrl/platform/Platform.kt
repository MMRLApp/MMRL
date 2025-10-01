@file:Suppress("unused", "PropertyName")

package com.dergoogler.mmrl.platform

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log

/**
 * A sealed interface representing the hierarchy of supported platforms.
 * This is used for type-safe checking and categorization of different platform implementations,
 * particularly distinguishing between various root solutions and non-root environments.
 */
sealed class PlatformType(val id: String) {
    open val MINIMAL_SUPPORTED_SU_COMPAT: Int = -1
    open val MINIMAL_SUPPORTED_KERNEL_LKM: Int = -1
    open val MINIMAL_SUPPORTED_KERNEL: Int = -1

    // KernelSU Next
    open val MINIMAL_SUPPORTED_HOOK_MODE = -1
    open val MINIMAL_SUPPORTED_MANAGER_UID = -1

    // SukiSU Ultra
    open val MINIMAL_SUPPORTED_KERNEL_FULL = ""
    open val MINIMAL_SUPPORTED_KPM = -1
    open val MINIMAL_SUPPORTED_DYNAMIC_MANAGER = -1

    /** Represents the Magisk platform. */
    data object Magisk : PlatformType("magisk")

    /** Represents the base class for KernelSU and its variants. */
    open class KernelSU(id: String = "kernelsu") : PlatformType(id) {
        override val MINIMAL_SUPPORTED_KERNEL = 11071
        override val MINIMAL_SUPPORTED_KERNEL_LKM = 11648
        override val MINIMAL_SUPPORTED_SU_COMPAT = 12040
    }

    /** Represents the KernelSU Next Gen variant, inheriting from [KernelSU]. */
    data object KernelSuNext : KernelSU("ksunext") {
        override val MINIMAL_SUPPORTED_KERNEL = 12797
        override val MINIMAL_SUPPORTED_KERNEL_LKM = 12797
        override val MINIMAL_SUPPORTED_SU_COMPAT = 12404
        override val MINIMAL_SUPPORTED_HOOK_MODE = 12569
        override val MINIMAL_SUPPORTED_MANAGER_UID = 12751
    }

    /** Represents the APatch platform. */
    data object APatch : PlatformType("apatch")

    /** Represents the MKSU variant, inheriting from [KernelSU]. */
    data object MKSU : KernelSU("mksu")

    /** Represents the SukiSU variant, inheriting from [KernelSU]. */
    data object SukiSU : KernelSU("sukisu") {
        override val MINIMAL_SUPPORTED_KERNEL = 11071
        override val MINIMAL_SUPPORTED_KERNEL_LKM = 11648
        override val MINIMAL_SUPPORTED_SU_COMPAT = 12040
        override val MINIMAL_SUPPORTED_KERNEL_FULL = "v3.1.5"
        override val MINIMAL_SUPPORTED_KPM = 12800
        override val MINIMAL_SUPPORTED_DYNAMIC_MANAGER = 13215
    }

    /** Represents the RKSU variant, inheriting from [KernelSU]. */
    data object RKSU : KernelSU("rksu") {
        override val MINIMAL_SUPPORTED_SU_COMPAT: Int = 12071
        override val MINIMAL_SUPPORTED_KERNEL_LKM: Int = 11648
        override val MINIMAL_SUPPORTED_KERNEL: Int = 11071
    }

    /**
     * Represents the Shizuku platform, which provides a way to use system APIs
     * without root, via a user-granted ADB or root service.
     */
    data object Shizuku : PlatformType("shizuku")

    /**
     * Represents a non-root environment where no elevated privileges are available.
     */
    data object NonRoot : PlatformType("nonroot")

    data object Unknown : PlatformType("unknown")
}

const val TIMEOUT_MILLIS = 15_000L
const val PLATFORM_KEY = "PLATFORM"
internal const val BINDER_TRANSACTION = 84398154

/**
 * Represents the various platforms supported by the application.
 *
 * @property type The platform type instance containing the unique identifier.
 */
enum class Platform(val type: PlatformType) {
    Magisk(PlatformType.Magisk),
    KernelSU(PlatformType.KernelSU()),
    KsuNext(PlatformType.KernelSuNext),
    APatch(PlatformType.APatch),
    MKSU(PlatformType.MKSU),
    SukiSU(PlatformType.SukiSU),
    RKSU(PlatformType.RKSU),
    Shizuku(PlatformType.Shizuku),
    NonRoot(PlatformType.NonRoot),
    Unknown(PlatformType.Unknown);

    val id: String get() = type.id

    companion object {
        private val platformMap = entries.associateBy { it.id }

        fun from(value: String): Platform = platformMap[value] ?: NonRoot

        /**
         * Creates an [Intent] for a specific platform.
         *
         * This function is an inline extension function on the [Context] class.
         * It takes a reified type parameter `T` which represents the target component (e.g., Activity or Service)
         * and a [Platform] enum value indicating the platform for which the intent is being created.
         *
         * The created [Intent] will have its component set to the fully qualified name of the class `T`
         * within the current application's package.
         * It will also include the specified [platform] as an extra, using [PLATFORM_KEY] as the key.
         *
         * @param T The reified type of the target component (e.g., an Activity or Service class).
         * @param platform The [Platform] for which this intent is being created.
         * @return An [Intent] configured to launch the specified component for the given platform.
         */
        inline fun <reified T> Context.createPlatformIntent(platform: Platform): Intent =
            Intent().apply {
                component = ComponentName(packageName, T::class.java.name)
                putExtra(PLATFORM_KEY, platform)
            }

        fun Intent.getPlatform(): Platform? = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getSerializableExtra(PLATFORM_KEY, Platform::class.java)
            } else {
                @Suppress("DEPRECATION")
                getSerializableExtra(PLATFORM_KEY) as? Platform
            }
        } catch (e: Exception) {
            Log.e("Platform", "Error getting platform", e)
            null
        }

        fun Intent.putPlatform(platform: Platform) {
            putExtra(PLATFORM_KEY, platform)
        }
    }

    // Primary platform checks
    val isMagisk: Boolean get() = this == Magisk
    val isKernelSU: Boolean get() = this == KernelSU
    val isKernelSuNext: Boolean get() = this == KsuNext
    val isAPatch: Boolean get() = this == APatch
    val isMKSU: Boolean get() = this == MKSU
    val isSukiSU: Boolean get() = this == SukiSU
    val isRKSU: Boolean get() = this == RKSU
    val isShizuku: Boolean get() = this == Shizuku
    val isNonRoot: Boolean get() = this == NonRoot

    // Category checks
    val isKernelSuVariant: Boolean get() = type is PlatformType.KernelSU
    val isKernelSuOrNext: Boolean get() = this == KernelSU || this == KsuNext
    val isValid: Boolean get() = this != NonRoot

    // Negation checks (computed properties for consistency)
    val isNotMagisk: Boolean get() = !isMagisk
    val isNotKernelSU: Boolean get() = this != KernelSU && this != KsuNext
    val isNotKernelSuNext: Boolean get() = !isKernelSuNext
    val isNotAPatch: Boolean get() = !isAPatch
    val isNotMKSU: Boolean get() = !isMKSU
    val isNotSukiSU: Boolean get() = !isSukiSU
    val isNotRKSU: Boolean get() = !isRKSU
    val isNotShizuku: Boolean get() = !isShizuku
    val isNotNonRoot: Boolean get() = !isNonRoot
    val isNotValid: Boolean get() = !isValid

    @Deprecated("Use 'id' property instead", ReplaceWith("id"))
    val current: String get() = id
}