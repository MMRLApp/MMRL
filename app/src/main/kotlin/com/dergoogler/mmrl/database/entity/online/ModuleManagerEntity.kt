package com.dergoogler.mmrl.database.entity.online

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.dergoogler.mmrl.model.online.ModuleManager
import com.dergoogler.mmrl.model.online.ModuleManagerSolution

@Entity(tableName = "manager")
data class ModuleManagerEntity(
    @ColumnInfo(name = "magiskManager") val magisk: ModuleManagerSolution? = null,
    @ColumnInfo(name = "kernelsuManager") val kernelsu: ModuleManagerSolution? = null,
    @ColumnInfo(name = "apatchManager") val apatch: ModuleManagerSolution? = null,
) {
    constructor(original: ModuleManager?) : this(
        magisk = original?.magisk,
        kernelsu = original?.kernelsu,
        apatch = original?.apatch,
    )

    fun toManager() =
        ModuleManager(
            magisk = magisk,
            kernelsu = kernelsu,
            apatch = apatch,
        )
}
