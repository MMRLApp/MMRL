package com.dergoogler.mmrl.ui.screens.exploreRepositories.items

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.fadingEdge
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.model.online.ExploreRepository
import com.dergoogler.mmrl.ui.component.Cover
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.utils.toFormattedDateSafely
import com.ramcosta.composedestinations.generated.destinations.ExploreRepositoryScreenDestination
import dev.mmrlx.compose.layout.flashlightCard
import dev.mmrlx.compose.ui.Badge
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.theme.MMRLXTheme

@Composable
fun RepoCard(repo: ExploreRepository) {
    val navigator = LocalDestinationsNavigator.current

    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.repositoriesMenu
    val colors = MMRLXTheme.colors

    val repoCover = repo.cover.nullable(menu.showCover) { it }

    Column(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    navigator.navigate(ExploreRepositoryScreenDestination(repo))
                },
            )
            .fillMaxWidth()
            .flashlightCard(),
    ) {
        repoCover.nullable(menu.showCover) {
            if (it.isNotEmpty()) {
                Box(
                    contentAlignment = Alignment.Center,
                ) {
                    Cover(
                        modifier = Modifier.fadingEdge(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black,
                                ),
                                startY = Float.POSITIVE_INFINITY,
                                endY = 0f,
                            ),
                        ),
                        url = it,
                    )

                    repo.modulesCount.nullable(menu.showModulesCount) {
                        Box(
                            modifier = Modifier
                                .absolutePadding(
                                    top = 16.dp,
                                    right = 16.dp,
                                )
                                .align(Alignment.TopEnd),
                        ) {
                            ModuleCountLabelItem(it)
                        }
                    }
                }
            }
        }

        Row(
            modifier = Modifier.padding(
                top = 16.dp,
                start = 16.dp,
                end = 16.dp,
            ),
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = repo.name,
                    style = MMRLXTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                    ),
                    color = colors.foreground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                repo.timestamp.nullable(menu.showUpdatedTime) {
                    Text(
                        text = stringResource(
                            id = R.string.module_update_at,
                            it.toFormattedDateSafely,
                        ),
                        style = MMRLXTheme.typography.bodySmall,
                        color = colors.mutedForeground,
                    )
                }
            }

            if (
                repoCover == null &&
                repo.modulesCount != null &&
                menu.showModulesCount
            ) {
                ModuleCountLabelItem(repo.modulesCount)
            }
        }

        repo.description.nullable {
            Text(
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp),
                text = it,
                style = MMRLXTheme.typography.bodySmall,
                color = colors.mutedForeground,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun ModuleCountLabelItem(count: Int) {
    Badge(
        text = stringResource(
            id = R.string.repo_modules,
            count,
        ),
    )
}
