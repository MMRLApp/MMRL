package com.dergoogler.mmrl.ui.screens.superuser

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
import com.dergoogler.mmrl.datastore.model.SuperUserMenu
import com.dergoogler.mmrl.ui.component.BottomSheet
import com.dergoogler.mmrl.ui.component.MenuChip
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

@Composable
fun SuperUserMenu(
    setMenu: (SuperUserMenu) -> Unit,
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
                menu = userPreferences.superUserMenu,
                setMenu = setMenu
            )
        }
    }
}

@Composable
private fun MenuBottomSheet(
    onClose: () -> Unit,
    menu: SuperUserMenu,
    setMenu: (SuperUserMenu) -> Unit,
) = BottomSheet(onDismissRequest = onClose) {
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

        FlowRow(
            modifier = Modifier
                .fillMaxWidth(1f)
                .wrapContentHeight(align = Alignment.Top),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            /*MenuChip(
                selected = menu.pinHasRoot,
                onClick = { setMenu(menu.copy(pinHasRoot = !menu.pinHasRoot)) },
                label = { Text(text = stringResource(id = R.string.menu_pin_installed)) }
            )*/

            MenuChip(
                selected = menu.showSystemApps,
                onClick = { setMenu(menu.copy(showSystemApps = !menu.showSystemApps)) },
                label = { Text(text = stringResource(id = R.string.menu_show_system_apps)) }
            )
        }
    }
}