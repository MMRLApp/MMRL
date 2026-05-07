package com.dergoogler.mmrl.model.online

import android.content.Context
import com.dergoogler.mmrl.R
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Contributor(
    val login: String,
    val avatarUrl: String,
    val url: String,
    val contributions: Int,
) {
    fun toMember(context: Context) =
        ExploreRepositoryMember(
            avatar = avatarUrl,
            name = login,
            title = context.getString(R.string.contributions, contributions),
            links =
                listOf(
                    SocialLink(
                        icon = "github",
                        link = url,
                    ),
                    SocialLink(
                        icon = "commit",
                        link = "https://github.com/MMRLApp/MMRL/commits?author=$login",
                    ),
                ),
        )
}
