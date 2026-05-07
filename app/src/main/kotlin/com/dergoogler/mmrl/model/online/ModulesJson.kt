package com.dergoogler.mmrl.model.online

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ModulesJson(
    val name: String,
    val submission: String? = null,
    val website: String? = null,
    val donate: String? = null,
    val support: String? = null,
    val cover: String? = null,
    val description: String? = null,
    val metadata: ModulesJsonMetadata = ModulesJsonMetadata.default(),
    val modules: List<OnlineModule>,
) {
    companion object {
        const val CURRENT_VERSION = 1
    }
}

@JsonClass(generateAdapter = true)
data class ModulesJsonMetadata(
    val version: Int,
    val timestamp: Float,
) {
    companion object {
        fun default() =
            ModulesJsonMetadata(
                version = 0,
                timestamp = 0f,
            )
    }
}
