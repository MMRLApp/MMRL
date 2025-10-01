package com.dergoogler.mmrl.ui.screens.moduleView.items

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.CompositionLocalProvider
import com.dergoogler.mmrl.model.online.OtherSources
import com.dergoogler.mmrl.ui.providable.LocalOnlineModuleState
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.ui.screens.repository.ModuleItemCompact

@Composable
fun OtherSourcesItem(items: List<OtherSources>) {
    val navController = LocalNavController.current
    val pagerState = rememberPagerState(pageCount = { (items.size + 2) / 3 })

    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
    ) { page ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (i in 0 until 3) {
                val itemIndex = page * 3 + i
                if (itemIndex < items.size) {
                    val item = items[itemIndex]

                    CompositionLocalProvider(
                        LocalOnlineModule provides item.online,
                        LocalOnlineModuleState provides item.state
                    ) {
                        ModuleItemCompact(
                            sourceProvider = item.repo.name,
                            showLabels = false,
                            showLastUpdated = false,
                            onClick = {
//                                navController.navigateSingleTopTo(
//                                    route = RepositoriesScreen.View.route,
//                                    args = mapOf(
//                                        "moduleId" to item.online.id,
//                                        "repoUrl" to item.repo.url
//                                    ),
//                                    launchSingleTop = false
//                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
