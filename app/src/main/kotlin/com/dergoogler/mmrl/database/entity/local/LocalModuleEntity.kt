package com.dergoogler.mmrl.database.entity.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.local.propertiesStream

@Entity(tableName = "localModules")
@TypeConverters
data class LocalModuleEntity(
    @PrimaryKey val id: String,
    val name: String,
    val version: String,
    val versionCode: Int,
    val author: String,
    val description: String,
    val state: String,
    val size: Long,
    val updateJson: String,
    val lastUpdated: Long,
) {
    constructor(original: LocalModule) : this(
        id = original.id,
        name = original.name,
        version = original.version,
        versionCode = original.versionCode,
        author = original.author,
        description = original.description,
        state = original.state.name,
        size = original.size,
        updateJson = original.updateJson,
        lastUpdated = original.lastUpdated,
    )

    fun toModule(): LocalModule {
        val props = mapOf(
            "name" to name,
            "version" to version,
            "versionCode" to versionCode,
            "author" to author,
            "description" to description,
            "updateJson" to updateJson,
            "state" to state,
            "size" to size,
            "lastUpdated" to lastUpdated,
        ).propertiesStream

        return LocalModule(
            id = id,
            propsSource = props
        )
    }
}

@Entity(tableName = "localModules_updatable")
data class LocalModuleUpdatable(
    @PrimaryKey val id: String,
    val updatable: Boolean,
)
