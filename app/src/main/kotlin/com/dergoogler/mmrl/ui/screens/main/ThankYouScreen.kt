package com.dergoogler.mmrl.ui.screens.main

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.isNotNullOrEmpty
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.ext.toDollars
import com.dergoogler.mmrl.ui.component.HorizontalDividerWithText
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.scaffold.Scaffold
import com.dergoogler.mmrl.ui.component.toolbar.BlurNavigateUpToolbar
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.screens.exploreRepositories.items.MemberCard
import com.dergoogler.mmrl.viewmodel.ThankYouViewModel
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph

@Destination<RootGraph>
@Composable
fun ThankYouScreen(
    vm: ThankYouViewModel = hiltViewModel(),
) = LocalScreenProvider {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val sponsors by vm.sponsors.collectAsStateWithLifecycle()
    val contributors by vm.contributors.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BlurNavigateUpToolbar(
                fade = true,
                fadeDistance = 50f,
                title = stringResource(id = R.string.thank_you),
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        this@Scaffold.ResponsiveContent {
            LazyVerticalGrid(
                modifier = Modifier
                    .padding(innerPadding)
                    .navigationBarsPadding(),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (sponsors.isNotNullOrEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalDividerWithText(
                                text = stringResource(
                                    R.string.sponsors
                                ),
                                thickness = 0.9.dp
                            )

                            Text(
                                text = stringResource(
                                    R.string.have_been_total_sponsored,
                                    vm.totalSponsorAmount.toDollars()
                                ),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    itemsIndexed(
                        items = sponsors,
                        key = { _, it -> it.name }
                    ) { index, it ->
                        MemberCard(
                            member = it,
                            index = index
                        )
                    }
                }

                if (contributors.isNotNullOrEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            HorizontalDividerWithText(
                                text = stringResource(
                                    R.string.contributors
                                ),
                                thickness = 0.9.dp
                            )

                            Text(
                                text = stringResource(
                                    R.string.total_community_contributions,
                                    vm.totalContributionsCount
                                ),
                                style = MaterialTheme.typography.titleSmall.copy(
                                    color = MaterialTheme.colorScheme.outline
                                )
                            )
                        }
                    }

                    itemsIndexed(
                        items = contributors,
                        key = { _, it -> it.name }
                    ) { index, it ->
                        MemberCard(
                            member = it,
                            index = index
                        )
                    }

                    item(span = { GridItemSpan(2) }) {
                        val paddingValues = LocalMainScreenInnerPaddings.current
                        Spacer(modifier = Modifier.height(paddingValues.calculateBottomPadding()))
                    }
                }
            }
        }
    }
}