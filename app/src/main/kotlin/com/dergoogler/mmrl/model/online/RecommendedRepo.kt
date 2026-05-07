package com.dergoogler.mmrl.model.online

import com.squareup.moshi.JsonClass

@Deprecated("Use ExploreRepository instead")
@JsonClass(generateAdapter = true)
data class RecommendedRepo(
    val name: String,
    val url: String,
    val maintainers: List<String>? = null,
    val submissions: String? = null,
    val notes: String? = null,
)
