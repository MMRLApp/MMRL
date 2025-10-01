package com.dergoogler.mmrl.model.terminal

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.snapshots.SnapshotStateList

data class CardBlock(
    override val lines: SnapshotStateList<Pair<Int, String>> = mutableStateListOf(),
) : Block
