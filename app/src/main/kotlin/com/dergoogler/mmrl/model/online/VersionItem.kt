package com.dergoogler.mmrl.model.online

import com.dergoogler.mmrl.ext.isNotNullOrBlank
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.utils.Utils
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@Serializable
@JsonClass(generateAdapter = true)
data class VersionItem(
    @Json(ignore = true) val repoUrl: String = "",
    val timestamp: Float,
    val version: String,
    val versionCode: Int,
    val zipUrl: String,
    val size: Int? = null,
    val changelog: String = "",
) {
    val versionDisplay get() = Utils.getVersionDisplay(version, versionCode)
    val hasSize = size != null

    val isEmpty get() = this == EMPTY


    companion object {
        val EMPTY = VersionItem(timestamp = 0f, version = "", versionCode = 0, zipUrl = "")
    }
}

@OptIn(ExperimentalContracts::class)
inline fun <R> VersionItem?.isValid(block: (VersionItem) -> R): R? {
    contract {
        callsInPlace(block, InvocationKind.AT_MOST_ONCE)
    }

    if (this == null) return null

    if (isEmpty) {
        return null
    }

    return block(this)
}
