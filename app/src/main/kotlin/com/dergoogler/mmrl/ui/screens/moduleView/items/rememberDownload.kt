package com.dergoogler.mmrl.ui.screens.moduleView.items

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import com.dergoogler.mmrl.model.online.VersionItem
import com.dergoogler.mmrl.ui.activity.terminal.install.InstallActivity
import com.dergoogler.mmrl.viewmodel.ModuleViewModel

@Composable
fun rememberDownload(): (VersionItem, Boolean) -> Unit {
    val context = LocalContext.current
    val vm = ModuleViewModel.current

    return remember {
        { item, install ->
            vm.downloader(context, item) {
                if (install) {
                    vm.installConfirm = false
                    InstallActivity.start(
                        context = context,
                        uri = it.toUri(),
                        confirm = false,
                    )
                }
            }
        }
    }
}