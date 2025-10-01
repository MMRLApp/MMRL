@file:Suppress("unused")

package com.dergoogler.mmrl.platform.ksu

import android.os.Parcelable
import androidx.annotation.Keep
import androidx.compose.runtime.Immutable
import com.dergoogler.mmrl.platform.ksu.KsuNative.KERNEL_SU_DOMAIN
import com.dergoogler.mmrl.platform.ksu.KsuNative.ROOT_GID
import com.dergoogler.mmrl.platform.ksu.KsuNative.ROOT_UID
import kotlinx.parcelize.Parcelize


@Immutable
@Parcelize
@Keep
data class Profile(
    // and there is a default profile for root and non-root
    val name: String,
    // current uid for the package, this is convivent for kernel to check
    // if the package name doesn't match uid, then it should be invalidated.
    val currentUid: Int = 0,

    // if this is true, kernel will grant root permission to this package
    val allowSu: Boolean = false,

    // these are used for root profile
    val rootUseDefault: Boolean = true,
    val rootTemplate: String? = null,
    val uid: Int = ROOT_UID,
    val gid: Int = ROOT_GID,
    val groups: List<Int> = mutableListOf(),
    val capabilities: List<Int> = mutableListOf(),
    val context: String = KERNEL_SU_DOMAIN,
    val namespace: Int = Namespace.INHERITED.ordinal,

    val nonRootUseDefault: Boolean = true,
    val umountModules: Boolean = true,
    var rules: String = "", // this field is save in ksud!!
) : Parcelable {
    enum class Namespace {
        INHERITED,
        GLOBAL,
        INDIVIDUAL,
    }

    constructor() : this("")

    companion object {
        fun onChange(profile: Profile) {

        }
    }
}