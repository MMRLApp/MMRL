package com.dergoogler.mmrl.model.local

import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.datastore.model.WorkingMode
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.RadioDialogItem

data class FeaturedManager(
    private val nameAs: Any,
    @DrawableRes val icon: Int,
    private val desc: Any? = null,
    val workingMode: WorkingMode,
) {
    @Composable
    fun toRadioDialogItem() = RadioDialogItem(
        title = name,
        value = workingMode
    )

    companion object {
        @get:Composable
        val FeaturedManager?.name: String
            get() {
                if (this == null) return stringResource(R.string.settings_root_none)

                return when (nameAs) {
                    is String -> nameAs
                    is Int -> stringResource(nameAs)
                    else -> stringResource(R.string.settings_root_none)
                }
            }

        @get:Composable
        val FeaturedManager?.description: String?
            get() {
                if (this == null) return null

                return when (desc) {
                    is String -> desc
                    is Int -> stringResource(desc)
                    else -> null
                }
            }

        val managers
            get() = listOf(
                FeaturedManager(
                    nameAs = R.string.working_mode_magisk_title,
                    icon = com.dergoogler.mmrl.ui.R.drawable.magisk_logo,
                    workingMode = WorkingMode.MODE_MAGISK,
                ),

                FeaturedManager(
                    nameAs = R.string.working_mode_kernelsu_title,
                    icon = com.dergoogler.mmrl.ui.R.drawable.kernelsu_logo,
                    workingMode = WorkingMode.MODE_KERNEL_SU,
                ),

                FeaturedManager(
                    nameAs = "rsuntk/KernelSU",
                    icon = com.dergoogler.mmrl.ui.R.drawable.kernelsu_logo,
                    workingMode = WorkingMode.MODE_RKSU,
                ),

                FeaturedManager(
                    nameAs = "5ec1cff/KernelSU",
                    icon = com.dergoogler.mmrl.ui.R.drawable.kernelsu_logo,
                    workingMode = WorkingMode.MODE_MKSU,
                ),

                FeaturedManager(
                    nameAs = R.string.working_mode_kernelsu_next_title,
                    icon = com.dergoogler.mmrl.ui.R.drawable.kernelsu_next_logo,
                    workingMode = WorkingMode.MODE_KERNEL_SU_NEXT,
                ),

                FeaturedManager(
                    nameAs = R.string.working_mode_apatch_title,
                    icon = com.dergoogler.mmrl.ui.R.drawable.brand_android,
                    workingMode = WorkingMode.MODE_APATCH
                ),

                FeaturedManager(
                    nameAs = R.string.working_mode_sukisu_title,
                    icon = com.dergoogler.mmrl.ui.R.drawable.sukisu_logo,
                    workingMode = WorkingMode.MODE_SUKISU
                ),

                FeaturedManager(
                    nameAs = R.string.setup_non_root_title,
                    icon = R.drawable.shield_lock,
                    workingMode = WorkingMode.MODE_NON_ROOT
                ),
            )
    }
}


