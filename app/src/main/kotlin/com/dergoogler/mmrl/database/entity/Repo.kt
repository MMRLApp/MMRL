package com.dergoogler.mmrl.database.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dergoogler.mmrl.model.online.ModulesJson
import com.dergoogler.mmrl.model.state.RepoState
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "repos")
data class Repo(
    @PrimaryKey val url: String,
    val name: String = url,
    val enable: Boolean = true,
    val submission: String? = null,
    val website: String? = null,
    val cover: String? = null,
    val description: String? = null,
    val donate: String? = null,
    val support: String? = null,
    @Embedded val metadata: RepoMetadata = RepoMetadata.default(),
) {
    val isCompatible get() = metadata.version == ModulesJson.CURRENT_VERSION

    val isUpdateJson get() = name == UPDATE_JSON

    override fun equals(other: Any?): Boolean {
        return when (other) {
            is Repo -> url == other.url
            else -> false
        }
    }

    override fun hashCode(): Int {
        return url.hashCode()
    }

    fun copy(modulesJson: ModulesJson) = copy(
        name = modulesJson.name,
        website = modulesJson.website,
        support = modulesJson.support,
        donate = modulesJson.donate,
        submission = modulesJson.submission,
        cover = modulesJson.cover,
        description = modulesJson.description,
        metadata = RepoMetadata(
            version = modulesJson.metadata.version,
            timestamp = modulesJson.metadata.timestamp,
            size = modulesJson.modules.size
        )
    )

    companion object {
        fun String.toRepo() = Repo(url = this)
        fun example() = RepoState.example().toRepo()

        const val UPDATE_JSON = "Update Json"
    }
}

@Serializable
@Entity(tableName = "metadata")
data class RepoMetadata(
    val version: Int,
    val timestamp: Float,
    val size: Int,
) {
    companion object {
        fun default() = RepoMetadata(
            version = ModulesJson.CURRENT_VERSION,
            timestamp = 0f,
            size = 0
        )
    }
}
