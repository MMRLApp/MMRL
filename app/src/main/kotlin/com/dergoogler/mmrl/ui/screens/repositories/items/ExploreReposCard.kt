package com.dergoogler.mmrl.ui.screens.repositories.items

import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.ramcosta.composedestinations.generated.destinations.ExploreRepositoriesScreenDestination
import dev.mmrlx.compose.layout.flashlightCard
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.icon.Icon
import dev.mmrlx.compose.ui.theme.MMRLXTheme

@Composable
fun ExploreReposCard() {
    val navigator = LocalDestinationsNavigator.current

    Column(
        modifier = Modifier
            .combinedClickable(
                onClick = {
                    navigator.navigate(ExploreRepositoriesScreenDestination)
                }
            )
            .fillMaxWidth()
            .flashlightCard()
            .padding(20.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(45.dp),
                painter =
                    painterResource(
                        id = R.drawable.cloud,
                    ),
                contentDescription = null,
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    text = stringResource(R.string.explore_more_repositories),
                    style = MMRLXTheme.typography.titleMedium,
                    color = MMRLXTheme.colors.secondaryForeground,
                )

                Text(
                    text = stringResource(R.string.get_the_most_out_of_mmrl),
                    style = MMRLXTheme.typography.bodyMedium,
                    color = MMRLXTheme.colors.secondaryForeground,
                )
            }
        }
    }
}
