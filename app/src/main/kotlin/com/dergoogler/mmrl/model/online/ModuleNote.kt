package com.dergoogler.mmrl.model.online

import com.dergoogler.mmrl.ext.isNotNullOrBlank
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class ModuleNote(
    val title: String? = null,
    val message: String? = null,
) {
    val hasTitle = title.isNotNullOrBlank()
    val hasMessage = message.isNotNullOrBlank()
    val isDeprecated = title.orEmpty().lowercase() == "deprecated"

    fun isNotEmpty() =
        title.isNotNullOrBlank() || message.isNotNullOrBlank()
}


inline fun <R> ModuleNote?.hasValidMessage(block: (ModuleNote) -> R): R? {
    if (this != null && isNotEmpty()) {
        return block(this)
    }

    return null
}