package com.dergoogler.mmrl.ui.screens.superuser

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.dergoogler.mmrl.platform.ksu.KsuNative
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.LabelItemDefaults
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Labels
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Start
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.viewmodel.SuperUserViewModel
import com.dergoogler.mmrl.viewmodel.SuperUserViewModel.AppInfo.Companion.loadIcon


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListScope.SuperUserItem(
    app: SuperUserViewModel.AppInfo,
    onClick: () -> Unit,
) {
    val context = LocalContext.current

    ButtonItem(
        onClick = onClick
    ) {
        Title(app.label)
        Description(app.packageName)

        Start {
            AsyncImage(
                model = app.loadIcon(context),
                contentDescription = app.label,
                modifier = Modifier.size(48.dp)
            )
        }

        Labels {
            if (app.allowSu) {
                LabelItem(
                    text = "ROOT",
                )
            } else {
                if (KsuNative.uidShouldUmount(app.uid)) {
                    LabelItem(
                        text = "UMOUNT",
                        style = LabelItemDefaults.style.copy(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer,
                            contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    )
                }
            }
            if (app.hasCustomProfile) {
                LabelItem(
                    text = "CUSTOM",
                    style = LabelItemDefaults.style.copy(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                    )
                )
            } else if (!app.allowSu && !KsuNative.uidShouldUmount(app.uid)) {
                LabelItem(
                    text = "DEFAULT",
                    style = LabelItemDefaults.style.copy(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    )
                )
            }
        }
    }
}