package com.dergoogler.mmrl.model.terminal

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class GroupBlock(
    val title: String? = null,
    val startLine: Int? = null,
    override val lines: SnapshotStateList<Pair<Int, String>> = mutableStateListOf(),
    val initiallyExpanded: Boolean = true,
) : Block
