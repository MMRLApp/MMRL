package com.dergoogler.mmrl.ui.component

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.runtime.Composable
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Item
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Labels
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.webui.model.WebUIPermissions

@Composable
fun ListScope.PermissionItem(
    permissions: List<String>,
    contentPadding: PaddingValues,
) = permissions.forEach {
    val permission = getPermissionDetails(it)
    permission.nullable { p ->
        Item(
            contentPadding = contentPadding,
        ) {
            Title(p.title)
            Description(p.desc)
            p.rootSolutions.nullable {
                Labels {
                    it()
                }
            }
        }
    }
}

data class Permission(
    @StringRes val title: Int,
    @StringRes val desc: Int,
    val rootSolutions: (@Composable RowScope.() -> Unit)? = null,
)

@Composable
private fun getPermissionDetails(id: String): Permission? {
    val permissionMap =
        mapOf(
            "magisk.permission.SERVICE" to
                Permission(
                    R.string.view_module_features_service,
                    R.string.view_module_features_service_sub,
                ),
            "magisk.permission.POST_FS_DATA" to
                Permission(
                    R.string.view_module_features_post_fs_data,
                    R.string.view_module_features_post_fs_data_sub,
                ),
            "magisk.permission.RESETPROP" to
                Permission(
                    R.string.view_module_features_system_properties,
                    R.string.view_module_features_resetprop_sub,
                ),
            "magisk.permission.SEPOLICY" to
                Permission(
                    R.string.view_module_features_selinux_policy,
                    R.string.view_module_features_sepolicy_sub,
                ),
            "magisk.permission.ZYGISK" to
                Permission(
                    R.string.view_module_features_zygisk,
                    R.string.view_module_features_zygisk_sub,
                ),
            "magisk.permission.ACTION" to
                Permission(
                    R.string.view_module_features_action,
                    R.string.view_module_features_action_sub,
                ),
            "kernelsu.permission.WEBUI" to
                Permission(
                    R.string.view_module_features_webui,
                    R.string.view_module_features_webui_sub,
                    {
                        KernelSuLabel()
                        APatchLabel()
                        MMRLLabel()
                    },
                ),
            "kernelsu.permission.POST_MOUNT" to
                Permission(
                    R.string.view_module_features_post_mount,
                    R.string.view_module_features_postmount_sub,
                    {
                        KernelSuLabel()
                        APatchLabel()
                    },
                ),
            "kernelsu.permission.BOOT_COMPLETED" to
                Permission(
                    R.string.view_module_features_boot_completed,
                    R.string.view_module_features_bootcompleted_sub,
                    {
                        KernelSuLabel()
                        APatchLabel()
                    },
                ),
            "mmrl.permission.WEBUI" to
                Permission(
                    R.string.view_module_features_mmrl_webui,
                    R.string.view_module_features_mmrl_webui_sub,
                    { MMRLLabel() },
                ),
            "mmrl.permission.WEBUI_CONFIG" to
                Permission(
                    R.string.view_module_features_mmrl_webui_config,
                    R.string.view_module_features_mmrl_webui_config_sub,
                    { MMRLLabel() },
                ),
            "mmrl.permission.APKS" to
                Permission(
                    R.string.view_module_features_apks,
                    R.string.view_module_features_apks_sub,
                ),
            WebUIPermissions.PLUGIN_DEX_LOADER to
                Permission(
                    R.string.view_module_features_dex_loader,
                    R.string.view_module_features_dex_loader_sub,
                    { MMRLLabel() },
                ),
        )

    return permissionMap[id]
}
