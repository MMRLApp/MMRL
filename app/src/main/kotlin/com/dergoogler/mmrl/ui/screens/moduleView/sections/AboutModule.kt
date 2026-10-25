package com.dergoogler.mmrl.ui.screens.moduleView.sections

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AssistChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.online.hasCategories
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalRepo
import com.dergoogler.mmrl.ui.screens.moduleView.listItemContentPaddingValues
import com.ramcosta.composedestinations.generated.destinations.DescriptionScreenDestination
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.list.List
import dev.mmrlx.compose.ui.list.ListItemSlot
import dev.mmrlx.compose.ui.list.component.ButtonItem
import dev.mmrlx.compose.ui.list.component.Item
import dev.mmrlx.compose.ui.list.component.item.Icon
import dev.mmrlx.compose.ui.list.component.item.Title

@Composable
internal fun AboutModule() {
    val repo = LocalRepo.current
    val module = LocalOnlineModule.current
    val navigator = LocalDestinationsNavigator.current

    val categoriesLazyListState = rememberLazyListState()

    List(
        contentPadding = listItemContentPaddingValues,
    ) {
        if (!module.readme.isNullOrBlank()) {
            ButtonItem(
                onClick = {
                    navigator.navigate(DescriptionScreenDestination(module.readme))
                },
            ) {
                Icon(
                    slot = ListItemSlot.End,
                    painter = painterResource(id = R.drawable.arrow_right),
                )
                Title(R.string.view_module_about_this_module)
            }
        } else {
            Item {
                Title(R.string.view_module_about_this_module)
            }
        }
    }

    Text(
        modifier = Modifier.padding(horizontal = 16.dp),
        text =
            module.description
                ?: stringResource(R.string.view_module_no_description),
        style =
            MaterialTheme.typography.bodyMedium.apply {
                if (module.description.isNullOrBlank()) {
                    copy(
                        fontStyle = FontStyle.Italic,
                    )
                }
            },
        color = MaterialTheme.colorScheme.outline,
    )

    module.hasCategories {
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            state = categoriesLazyListState,
            modifier =
                Modifier
                    .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
        ) {
            items(it.size) { category ->
                AssistChip(
                    onClick = {
//                        navigator.navigate(
//                            TypedModulesScreenDestination(
//                                type = ModulesFilter.CATEGORY,
//                                title = it[category],
//                                query = it[category],
//                                repo = repo,
//                            ),
//                        )
                    },
                    label = { Text(it[category]) },
                )
            }
        }
    }
}
