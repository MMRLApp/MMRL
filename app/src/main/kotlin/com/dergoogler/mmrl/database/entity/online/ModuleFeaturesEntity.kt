package com.dergoogler.mmrl.database.entity.online

import androidx.room.Entity
import androidx.room.TypeConverters
import com.dergoogler.mmrl.model.online.ModuleFeatures
import com.squareup.moshi.Json

@Entity(tableName = "root")
@TypeConverters
data class ModuleFeaturesEntity(
    val service: Boolean? = false,
    @Json(name = "post_fs_data") val postFsData: Boolean? = false,
    val resetprop: Boolean? = false,
    val sepolicy: Boolean? = false,
    val zygisk: Boolean? = false,
    val apks: Boolean? = false,
    val webroot: Boolean? = false,
    @Json(name = "post_mount") val postMount: Boolean? = false,
    @Json(name = "boot_completed") val bootCompleted: Boolean? = false,
    val action: Boolean? = false,
) {
    constructor(original: ModuleFeatures?) : this(
        service = original?.service,
        postFsData = original?.postFsData,
        resetprop = original?.resetprop,
        sepolicy = original?.sepolicy,
        zygisk = original?.zygisk,
        apks = original?.apks,
        webroot = original?.webroot,
        postMount = original?.postMount,
        bootCompleted = original?.bootCompleted,
        action = original?.action,
    )

    fun toFeatures() =
        ModuleFeatures(
            service = service,
            postFsData = postFsData,
            resetprop = resetprop,
            sepolicy = sepolicy,
            zygisk = zygisk,
            apks = apks,
            webroot = webroot,
            postMount = postMount,
            bootCompleted = bootCompleted,
            action = action,
        )
}
