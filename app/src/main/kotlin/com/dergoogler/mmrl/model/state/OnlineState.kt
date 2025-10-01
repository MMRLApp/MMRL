package com.dergoogler.mmrl.model.state

import com.dergoogler.mmrl.model.local.LocalModule
import com.dergoogler.mmrl.model.online.OnlineModule

data class OnlineState(
    val installed: Boolean,
    val updatable: Boolean,
    val hasLicense: Boolean,
    val lastUpdated: Float
) {
    @Suppress("FloatingPointLiteralPrecision")
    companion object {
        fun OnlineModule.createState(
            local: LocalModule?,
            hasUpdatableTag: Boolean,
        ): OnlineState {
            val installed = local != null && local.id.equals(id)
                    && local.author == author

            val updatable = if (installed && hasUpdatableTag) {
                local.versionCode < versionCode
            } else {
                false
            }

            return OnlineState(
                installed = installed,
                updatable = updatable,
                hasLicense = license.orEmpty().isNotBlank(),
                lastUpdated = versions.firstOrNull()?.timestamp ?: 0f
            )
        }

        fun example() = OnlineState(
            installed = true,
            updatable = false,
            hasLicense = true,
            lastUpdated = 1660640580.0f
        )
    }
}
