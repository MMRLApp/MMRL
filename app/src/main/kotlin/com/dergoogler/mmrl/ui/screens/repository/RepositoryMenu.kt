package com.dergoogler.mmrl.ui.screens.repository

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.Option
import com.dergoogler.mmrl.datastore.model.RepositoryMenu
import com.dergoogler.mmrl.ui.component.MenuChip1
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import dev.mmrlx.compose.ui.Text
import dev.mmrlx.compose.ui.button.Segment
import dev.mmrlx.compose.ui.button.SegmentedButtons
import dev.mmrlx.compose.ui.dialog.rememberModalBottomSheet
import dev.mmrlx.compose.ui.icon.Icon
import dev.mmrlx.compose.ui.icon.IconButton
import dev.mmrlx.compose.ui.theme.MMRLXTheme

@Composable
fun RepositoryMenu(setMenu: (RepositoryMenu) -> Unit) {
    val sheet = rememberModalBottomSheet()

    IconButton(
        onClick = { sheet.open() },
    ) {
        Icon(
            painter = painterResource(id = R.drawable.filter_outlined),
            contentDescription = null,
        )

        sheet {
            MenuBottomSheet(
                setMenu = setMenu,
            )
        }
    }
}

@Composable
private fun ColumnScope.MenuBottomSheet(
    setMenu: (RepositoryMenu) -> Unit,
) {
    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.repositoryMenu

    val options =
        listOf(
            Option.Name to R.string.menu_sort_option_name,
            Option.UpdatedTime to R.string.menu_sort_option_updated,
            Option.Size to R.string.menu_sort_option_size,
        )

    Text(
        text = stringResource(id = R.string.menu_advanced_menu),
        style = MMRLXTheme.typography.headlineSmall,
        modifier = Modifier.align(Alignment.CenterHorizontally),
    )

    Column(
        modifier = Modifier.padding(all = 18.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = stringResource(id = R.string.menu_sort_mode),
            style = MMRLXTheme.typography.titleSmall,
        )

        SegmentedButtons {
            options.forEach { (option, label) ->
                Segment(
                    selected = option == menu.option,
                    onClick = { setMenu(menu.copy(option = option)) },
                    icon = null,
                ) {
                    Text(text = stringResource(id = label))
                }
            }
        }

        FlowRow(
            modifier =
                Modifier
                    .fillMaxWidth(1f)
                    .wrapContentHeight(align = Alignment.Top),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            MenuChip1(
                selected = menu.descending,
                onClick = { setMenu(menu.copy(descending = !menu.descending)) },
                label = { Text(text = stringResource(id = R.string.menu_descending)) },
            )

            MenuChip1(
                selected = menu.pinInstalled,
                onClick = { setMenu(menu.copy(pinInstalled = !menu.pinInstalled)) },
                label = { Text(text = stringResource(id = R.string.menu_pin_installed)) },
            )

            MenuChip1(
                selected = menu.pinUpdatable,
                onClick = { setMenu(menu.copy(pinUpdatable = !menu.pinUpdatable)) },
                label = { Text(text = stringResource(id = R.string.menu_pin_updatable)) },
            )

            MenuChip1(
                selected = menu.showIcon,
                onClick = { setMenu(menu.copy(showIcon = !menu.showIcon)) },
                label = { Text(text = stringResource(id = R.string.menu_show_icon)) },
            )

            MenuChip1(
                selected = menu.showCover,
                onClick = { setMenu(menu.copy(showCover = !menu.showCover)) },
                label = { Text(text = stringResource(id = R.string.menu_show_cover)) },
            )

            MenuChip1(
                selected = menu.showVerified,
                onClick = { setMenu(menu.copy(showVerified = !menu.showVerified)) },
                label = { Text(text = stringResource(id = R.string.menu_show_verified)) },
            )

            MenuChip1(
                selected = menu.showLicense,
                onClick = { setMenu(menu.copy(showLicense = !menu.showLicense)) },
                label = { Text(text = stringResource(id = R.string.menu_show_license)) },
            )

            MenuChip1(
                selected = menu.showAntiFeatures,
                onClick = { setMenu(menu.copy(showAntiFeatures = !menu.showAntiFeatures)) },
                label = { Text(text = stringResource(id = R.string.menu_show_antifeatures)) },
            )

            MenuChip1(
                selected = menu.showCategory,
                onClick = { setMenu(menu.copy(showCategory = !menu.showCategory)) },
                label = { Text(text = stringResource(id = R.string.menu_show_category)) },
            )

            MenuChip1(
                selected = menu.showUpdatedTime,
                onClick = { setMenu(menu.copy(showUpdatedTime = !menu.showUpdatedTime)) },
                label = { Text(text = stringResource(id = R.string.menu_show_updated)) },
            )

            MenuChip1(
                enabled = menu.showUpdatedTime,
                selected = menu.showStars,
                onClick = { setMenu(menu.copy(showStars = !menu.showStars)) },
                label = { Text(text = stringResource(id = R.string.menu_show_stars)) },
            )
        }
    }
}
