package com.dergoogler.mmrl.database.entity

import androidx.room.Entity
import com.dergoogler.mmrl.model.online.VersionItem

@Entity(tableName = "versions", primaryKeys = ["id", "repoUrl", "versionCode"])
data class VersionItemEntity(
    val id: String,
    val repoUrl: String,
    val timestamp: Float,
    val version: String,
    val versionCode: Int,
    val zipUrl: String,
    val changelog: String,
) {
    constructor(
        original: VersionItem,
        id: String,
        repoUrl: String,
    ) : this(
        id = id,
        repoUrl = repoUrl,
        timestamp = original.timestamp,
        version = original.version,
        versionCode = original.versionCode,
        zipUrl = original.zipUrl,
        changelog = original.changelog,
    )

    fun toItem() =
        VersionItem(
            repoUrl = repoUrl,
            timestamp = timestamp,
            version = version,
            versionCode = versionCode,
            zipUrl = zipUrl,
            changelog = changelog,
        )
}
