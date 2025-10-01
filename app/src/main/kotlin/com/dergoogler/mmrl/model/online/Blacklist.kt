package com.dergoogler.mmrl.model.online

import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.dergoogler.mmrl.ext.isNotNullOrBlank
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.squareup.moshi.JsonClass
import kotlinx.serialization.Serializable
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

@Serializable
@JsonClass(generateAdapter = true)
data class Blacklist(
    val id: String,
    val source: String,
    val notes: String? = null,
    val antifeatures: List<String>? = null,
) {
    val isValid = id.isNotNullOrBlank() && source.isNotNullOrBlank()

    companion object {
        val EMPTY = Blacklist(
            id = "",
            source = "",
            notes = null,
            antifeatures = null,
        )

        @Composable
        inline fun <R> hasBlacklist(blacklist: Blacklist?, block: (Blacklist) -> R): R? {
            val blacklisted by isBlacklisted(blacklist)

            return if (blacklisted) {
                block(blacklist!!)
            } else {
                null
            }
        }


        @Composable
        fun isBlacklisted(blacklist: Blacklist?): State<Boolean> {
            val alerts = LocalUserPreferences.current.blacklistAlerts
            return remember {
                derivedStateOf {
                    isBlacklisted(alerts, blacklist)
                }
            }
        }

        @OptIn(ExperimentalContracts::class)
        fun isBlacklisted(enabled: Boolean, blacklist: Blacklist?): Boolean {
            contract {
                returns(true) implies (blacklist != null)
            }

            return enabled &&
                    blacklist != null &&
                    !(blacklist.antifeatures != null && blacklist.antifeatures.size == 1 && blacklist.antifeatures.contains(
                        "NoSourceSince"
                    )) &&
                    blacklist.isValid
        }
    }
}
