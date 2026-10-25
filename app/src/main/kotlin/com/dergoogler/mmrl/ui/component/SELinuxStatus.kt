package com.dergoogler.mmrl.ui.component

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.painterResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.platform.PlatformManager
import dev.mmrlx.compose.ui.list.ListScope
import dev.mmrlx.compose.ui.list.component.Item
import dev.mmrlx.compose.ui.list.component.item.Description
import dev.mmrlx.compose.ui.list.component.item.Icon
import dev.mmrlx.compose.ui.list.component.item.Title
import com.dergoogler.mmrl.ui.remember.remember

@Composable
fun ListScope.SELinuxStatus() {
    val seLinuxStatus by PlatformManager.remember(R.string.selinux_status_unknown) {
        try {
            if (!isSELinuxEnabled) {
                return@remember R.string.selinux_status_disabled
            }

            if (isSELinuxEnforced) {
                return@remember R.string.selinux_status_enforcing
            }

            return@remember R.string.selinux_status_permissive
        } catch (e: Exception) {
            Log.e("SELinuxStatus", "Failed to check SELinux status", e)
            return@remember R.string.selinux_status_unknown
        }
    }

    Item {
        Icon(painter = painterResource(R.drawable.shield_bolt))
        Title(R.string.selinux_status)
        Description(seLinuxStatus)
    }
}
