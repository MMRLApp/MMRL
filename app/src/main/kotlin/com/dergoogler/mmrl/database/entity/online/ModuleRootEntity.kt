package com.dergoogler.mmrl.database.entity.online

import androidx.room.Entity
import com.dergoogler.mmrl.model.online.ModuleRoot

@Deprecated("ModuleRootEntity is replaced by ModuleManagerEntity")
@Entity(tableName = "root")
data class ModuleRootEntity(
    val magisk: String? = null,
    val kernelsu: String? = null,
    val apatch: String? = null,
) {
    constructor(original: ModuleRoot?) : this(
        magisk = original?.magisk,
        kernelsu = original?.kernelsu,
        apatch = original?.apatch,
    )

    fun toRoot() =
        ModuleRoot(
            magisk = magisk,
            kernelsu = kernelsu,
            apatch = apatch,
        )
}
