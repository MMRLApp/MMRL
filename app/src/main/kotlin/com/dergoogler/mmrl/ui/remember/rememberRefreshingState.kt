package com.dergoogler.mmrl.ui.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch

@Composable
fun rememberRefreshingState(): Pair<Boolean, (suspend () -> Unit) -> Unit> {
    val scope = rememberCoroutineScope()
    var refreshing by remember { mutableStateOf(false) }

    val refresh: (suspend () -> Unit) -> Unit = { block ->
        scope.launch {
            refreshing = true
            try {
                block()
            } finally {
                refreshing = false
            }
        }
    }

    return refreshing to refresh
}