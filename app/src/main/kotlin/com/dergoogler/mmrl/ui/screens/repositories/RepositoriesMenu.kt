package com.dergoogler.mmrl.ui.screens.repositories

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.Option
import com.dergoogler.mmrl.datastore.model.RepositoriesMenu
import com.dergoogler.mmrl.ui.component.BottomSheet
import com.dergoogler.mmrl.ui.component.MenuChip
import com.dergoogler.mmrl.ui.component.Segment
import com.dergoogler.mmrl.ui.component.SegmentedButtons
import com.dergoogler.mmrl.ui.component.SegmentedButtonsDefaults
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

@Composable
fun RepositoriesMenu(
    setMenu: (RepositoriesMenu) -> Unit,
) {
    val userPreferences = LocalUserPreferences.current
    var open by rememberSaveable { mutableStateOf(false) }

    IconButton(
        onClick = { open = true }
    ) {
        Icon(
            painter = painterResource(id = R.drawable.filter_outlined),
            contentDescription = null
        )

        if (open) {
            MenuBottomSheet(
                onClose = { open = false },
                menu = userPreferences.repositoriesMenu,
                setMenu = setMenu
            )
        }
    }
}

@Composable
private fun MenuBottomSheet(
    onClose: () -> Unit,
    menu: RepositoriesMenu,
    setMenu: (RepositoriesMenu) -> Unit,
) = BottomSheet(onDismissRequest = onClose) {
    val options = listOf(
        Option.Name to R.string.menu_sort_option_name,
        Option.UpdatedTime to R.string.menu_sort_option_updated
    )

    Text(
        text = stringResource(id = R.string.menu_advanced_menu),
        style = MaterialTheme.typography.headlineSmall,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )

    Column(
        modifier = Modifier.padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = stringResource(id = R.string.menu_sort_mode),
            style = MaterialTheme.typography.titleSmall
        )

        SegmentedButtons(
            border = SegmentedButtonsDefaults.border(
                color = MaterialTheme.colorScheme.secondary
            )
        ) {
            options.forEach { (option, label) ->
                Segment(
                    selected = option == menu.option,
                    onClick = { setMenu(menu.copy(option = option)) },
                    colors = SegmentedButtonsDefaults.buttonColor(
                        selectedContainerColor = MaterialTheme.colorScheme.secondary,
                        selectedContentColor = MaterialTheme.colorScheme.onSecondary
                    ),
                    icon = null
                ) {
                    Text(text = stringResource(id = label))
                }
            }
        }

        FlowRow(
            modifier = Modifier
                .fillMaxWidth(1f)
                .wrapContentHeight(align = Alignment.Top),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MenuChip(
                selected = menu.descending,
                onClick = { setMenu(menu.copy(descending = !menu.descending)) },
                label = { Text(text = stringResource(id = R.string.menu_descending)) }
            )


            MenuChip(
                selected = menu.showModulesCount,
                onClick = { setMenu(menu.copy(showModulesCount = !menu.showModulesCount)) },
                label = { Text(text = stringResource(id = R.string.menu_show_modules_count)) }
            )

            MenuChip(
                selected = menu.showCover,
                onClick = { setMenu(menu.copy(showCover = !menu.showCover)) },
                label = { Text(text = stringResource(id = R.string.menu_show_cover)) }
            )

            MenuChip(
                selected = menu.showUpdatedTime,
                onClick = { setMenu(menu.copy(showUpdatedTime = !menu.showUpdatedTime)) },
                label = { Text(text = stringResource(id = R.string.menu_show_updated)) }
            )
        }
    }
}