package com.dergoogler.mmrl.ui.screens.exploreRepositories.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.isNotNullOrEmpty
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.toDecodedUrl
import com.dergoogler.mmrl.model.online.ExploreRepositoryMember
import dev.mmrlx.compose.layout.card
import dev.mmrlx.compose.ui.scaffold.PositionLayout
import dev.mmrlx.compose.ui.theme.MMRLXTheme

@Composable
fun MemberCard(
    member: ExploreRepositoryMember,
    index: Int? = null,
) {
    val browser = LocalUriHandler.current
    val colors = MMRLXTheme.colors
    val interactionSource = remember { MutableInteractionSource() }

    PositionLayout(modifier = Modifier.card()) {
        index.nullable {
            if (it < 3) {
                Surface(
                    shape = RoundedCornerShape(
                        topEnd = 20.dp,
                        bottomStart = 0.dp,
                        bottomEnd = 0.dp,
                    ),
                    color = colors.primary,
                    modifier = Modifier.absolute(Alignment.TopEnd),
                ) {
                    Text(
                        text = "#${it + 1}",
                        style = MMRLXTheme.typography.bodySmall,
                        color = colors.primaryForeground,
                        modifier = Modifier.padding(
                            horizontal = 16.dp,
                            vertical = 8.dp,
                        ),
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .relative()
                .fillMaxWidth()
                .padding(horizontal = 32.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            AsyncImage(
                model = member.avatar,
                modifier = Modifier
                    .size(60.dp)
                    .clip(RoundedCornerShape(100)),
                contentDescription = null,
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(top = 24.dp),
            ) {
                Text(
                    text = member.name.toDecodedUrl(),
                    maxLines = 1,
                    style = MMRLXTheme.typography.titleMedium,
                    color = colors.foreground,
                )

                member.title.nullable {
                    Text(
                        text = it.toDecodedUrl(),
                        maxLines = 1,
                        style = MMRLXTheme.typography.titleSmall,
                        color = colors.mutedForeground,
                    )
                }

                if (member.links.isNotNullOrEmpty()) {
                    LazyRow(
                        modifier = Modifier.padding(
                            top = 24.dp,
                            start = 16.dp,
                            end = 16.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(
                            items = member.links,
                            key = { it.icon },
                        ) {
                            Icon(
                                painter = painterResource(
                                    id = when (it.icon.lowercase()) {
                                        "github" -> R.drawable.brand_github
                                        "gitlab" -> R.drawable.brand_gitlab
                                        "commit" -> R.drawable.git_commit
                                        else -> R.drawable.transparent
                                    },
                                ),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(100))
                                    .clickable(
                                        enabled = true,
                                        interactionSource = interactionSource,
                                        onClick = {
                                            browser.openUri(it.link)
                                        },
                                        indication = ripple(),
                                    ),
                                contentDescription = it.icon.lowercase(),
                                tint = colors.foreground,
                            )
                        }
                    }
                }
            }
        }
    }
}