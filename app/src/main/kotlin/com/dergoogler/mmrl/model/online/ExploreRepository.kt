package com.dergoogler.mmrl.model.online

import com.dergoogler.mmrl.app.moshi
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.dergoogler.mmrl.ext.isNotNullOrBlank
import com.dergoogler.mmrl.ext.toEncodedUrl
import kotlinx.serialization.Serializable

@Serializable
@JsonClass(generateAdapter = true)
data class ExploreRepositoryMember(
    val avatar: String,
    val name: String,
    val title: String? = null,
    val org: String? = null,
    val orgLink: String? = null,
    val desc: String? = null,
    val links: List<SocialLink>? = null,
    val sponsor: String? = null,
    val actionText: String? = null,
)

@Serializable
@JsonClass(generateAdapter = true)
data class SocialLink(
    val icon: String,
    val link: String,
    val ariaLabel: String? = null,
)

@Serializable
@JsonClass(generateAdapter = true)
data class ExploreRepository(
    val name: String,
    val url: String,
    val cover: String? = null,
    val timestamp: Float? = null,
    @Json(name = "modules_count") val modulesCount: Int? = null,
    val submission: String? = null,
    val description: String? = null,
    val donate: String? = null,
    val members: List<ExploreRepositoryMember>? = null,
) {
    val hasCover = cover.isNotNullOrBlank()

    fun toJSON(encode: Boolean = false): String {
        val jsonAdapter = moshi.adapter<ExploreRepository>(this::class.java)
        val json = jsonAdapter.toJson(this)

        if (encode) {
            return json.toEncodedUrl()
        }

        return json
    }
}