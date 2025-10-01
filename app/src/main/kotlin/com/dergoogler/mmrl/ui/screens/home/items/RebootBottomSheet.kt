package com.dergoogler.mmrl.ui.screens.home.items

import android.content.Context
import android.os.Build
import android.os.PowerManager
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.component.BottomSheet
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.dialog.ConfirmDialog
import com.dergoogler.mmrl.ui.component.listItem.ListButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Labels
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Slot
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.dergoogler.mmrl.viewmodel.HomeViewModel

@Composable
fun RebootBottomSheet(
    onClose: () -> Unit,
) = BottomSheet(onDismissRequest = onClose) {
    List(
        modifier = Modifier.padding(bottom = 18.dp),
    ) {
        RebootItem(title = R.string.reboot)

        val pm = LocalContext.current.getSystemService(Context.POWER_SERVICE) as PowerManager?

        val hasSoftReboot =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && pm?.isRebootingUserspaceSupported == true

        RebootItem(
            enabled = hasSoftReboot,
            title = R.string.reboot_userspace,
            reason = "userspace"
        ) {
            if (!hasSoftReboot) {
                Labels {
                    LabelItem("Unsupported");
                }
            }
        }

        RebootItem(title = R.string.reboot_recovery, reason = "recovery")
        RebootItem(title = R.string.reboot_bootloader, reason = "bootloader")
        RebootItem(title = R.string.reboot_download, reason = "download")
        RebootItem(title = R.string.reboot_edl, reason = "edl")
    }
}

@Composable
private fun ListScope.RebootItem(
    enabled: Boolean = true,
    viewModel: HomeViewModel = hiltViewModel(),
    @StringRes title: Int, reason: String = "",
    content: @Composable ListItemScope.() -> Unit = {},
) {
    val userPreferences = LocalUserPreferences.current

    var confirmReboot by remember { mutableStateOf(false) }
    if (confirmReboot) ConfirmDialog(
        title = R.string.install_screen_reboot_title,
        description = R.string.install_screen_reboot_text,
        onClose = { confirmReboot = false },
        onConfirm = {
            confirmReboot = false
            viewModel.reboot()
        }
    )

    ButtonItem(
        enabled = enabled,
        onClick = {
            if (userPreferences.confirmReboot) {
                confirmReboot = true
            } else {
                viewModel.reboot(reason)
            }
        }
    ) {
        Title(title)
        content()
    }
}
