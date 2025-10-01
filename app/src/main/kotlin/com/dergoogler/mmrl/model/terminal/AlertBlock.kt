package com.dergoogler.mmrl.model.terminal

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

enum class AlertType {
    NOTICE,
    WARNING,
    ERROR,
}

data class AlertBlock(
    val lineNumber: Int,
    val type: AlertType,
    val title: String? = null,
    val text: String,
    override val lines: SnapshotStateList<Pair<Int, String>> = mutableStateListOf(),
) : Block