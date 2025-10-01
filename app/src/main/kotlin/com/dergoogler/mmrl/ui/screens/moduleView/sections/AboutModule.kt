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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.online.hasCategories
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Item
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Icon
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalRepo
import com.dergoogler.mmrl.ui.screens.moduleView.listItemContentPaddingValues
import com.dergoogler.mmrl.ui.screens.repository.modules.ModulesFilter
import com.ramcosta.composedestinations.generated.destinations.TypedModulesScreenDestination
import com.ramcosta.composedestinations.generated.destinations.ViewDescriptionScreenDestination

@Composable
internal fun AboutModule() {
    val repo = LocalRepo.current
    val module = LocalOnlineModule.current
    val navigator = LocalDestinationsNavigator.current

    val categoriesLazyListState = rememberLazyListState()

    List(
        contentPadding = listItemContentPaddingValues
    ) {
        if (!module.readme.isNullOrBlank()) {
            ButtonItem(
                onClick = {
                    navigator.navigate(ViewDescriptionScreenDestination(module.readme))
                }
            ) {
                Icon(
                    slot = ListItemSlot.End,
                    painter = painterResource(id = R.drawable.arrow_right)
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
        text = module.description
            ?: stringResource(R.string.view_module_no_description),
        style = MaterialTheme.typography.bodyMedium.apply {
            if (module.description.isNullOrBlank()) {
                copy(
                    fontStyle = FontStyle.Italic
                )
            }
        },
        color = MaterialTheme.colorScheme.outline
    )

    module.hasCategories {
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(
            state = categoriesLazyListState,
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp)
        ) {
            items(it.size) { category ->
                AssistChip(
                    onClick = {
                        navigator.navigate(
                            TypedModulesScreenDestination(
                                type = ModulesFilter.CATEGORY,
                                title = it[category],
                                query = it[category],
                                repo = repo,
                            )
                        )
                    },
                    label = { Text(it[category]) }
                )
            }
        }
    }
}