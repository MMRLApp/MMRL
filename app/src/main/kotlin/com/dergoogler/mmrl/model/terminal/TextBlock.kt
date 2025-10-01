package com.dergoogler.mmrl.model.terminal

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class TextBlock(
    val lineNumber: Int,
    val text: String,
    override val lines: SnapshotStateList<Pair<Int, String>> = mutableStateListOf(),
    val key: String? = null
) : Block