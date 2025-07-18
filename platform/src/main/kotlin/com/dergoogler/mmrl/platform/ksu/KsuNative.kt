package com.dergoogler.mmrl.platform.ksu

object KsuNative {
    // minimal supported kernel version
    // 10915: allowlist breaking change, add app profile
    // 10931: app profile struct add 'version' field
    // 10946: add capabilities
    // 10977: change groups_count and groups to avoid overflow write
    // 11071: Fix the issue of failing to set a custom SELinux type.
    const val MINIMAL_SUPPORTED_KERNEL = 11071

    // 11640: Support query working mode, LKM or GKI
    // when MINIMAL_SUPPORTED_KERNEL > 11640, we can remove this constant.
    const val MINIMAL_SUPPORTED_KERNEL_LKM = 11648

    // 12404: Support disable sucompat mode
    const val MINIMAL_SUPPORTED_SU_COMPAT_NEXT = 12404
    const val MINIMAL_SUPPORTED_SU_COMPAT = 12040

    // 12569: support get hook mode
    const val MINIMAL_SUPPORTED_HOOK_MODE = 12569

    const val KERNEL_SU_DOMAIN = "u:r:su:s0"

    const val ROOT_UID = 0
    const val ROOT_GID = 0

    init {
        System.loadLibrary("mmrl-kernelsu")
    }

    external fun grantRoot(): Boolean

    external fun becomeManager(pkg: String?): Boolean

    external fun getAllowList(): IntArray
    external fun isSafeMode(): Boolean
    external fun getVersion(): Int
    external fun isLkmMode(): Boolean?
    external fun uidShouldUmount(uid: Int): Boolean

    /**
     * `su` compat mode can be disabled temporarily.
     *  0: disabled
     *  1: enabled
     *  negative : error
     */
    external fun isSuEnabled(): Boolean
    external fun setSuEnabled(enabled: Boolean): Boolean

    private const val NON_ROOT_DEFAULT_PROFILE_KEY = "$"
    private const val NOBODY_UID = 9999


    fun requireNewKernel(): Boolean {
        return getVersion() < MINIMAL_SUPPORTED_KERNEL
    }

    /**
     * # KsuNext
     */
    external fun getHookMode(): String?

    /**
     * # SukiSU
     */
    external fun isKPMEnabled(): Boolean

    /**
     * # SukiSU
     */
    external fun getHookType(): String
}