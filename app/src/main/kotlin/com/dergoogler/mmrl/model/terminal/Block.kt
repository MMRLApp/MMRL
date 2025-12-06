package com.dergoogler.mmrl.model.terminal

import androidx.compose.runtime.snapshots.SnapshotStateList

sealed interface Block {
    val lines: SnapshotStateList<Pair<Int, String>>
}
