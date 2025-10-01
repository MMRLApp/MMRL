package com.dergoogler.mmrl.model.online


import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@Deprecated("Use permissions instead")
@JsonClass(generateAdapter = true)
data class ModuleFeatures(
    val service: Boolean? = null,
    @Json(name = "post_fs_data") val postFsData: Boolean? = null,
    val resetprop: Boolean? = null,
    val sepolicy: Boolean? = null,
    val zygisk: Boolean? = null,
    val apks: Boolean? = null,
    val webroot: Boolean? = null,
    @Json(name = "post_mount") val postMount: Boolean? = null,
    @Json(name = "boot_completed") val bootCompleted: Boolean? = null,
    val action: Boolean? = null,
) {
    fun isNotEmpty() =
        service != null || postFsData != null || resetprop != null || sepolicy != null || zygisk != null || apks != null || webroot != null || postMount != null || bootCompleted != null || action != null

    val size = listOf(
        service,
        postFsData,
        resetprop,
        sepolicy,
        zygisk,
        apks,
        webroot,
        postMount,
        bootCompleted,
        action
    ).count { it != null }
}

