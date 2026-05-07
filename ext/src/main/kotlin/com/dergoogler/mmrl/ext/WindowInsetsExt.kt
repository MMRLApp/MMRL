package com.dergoogler.mmrl.ext

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.add
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable

val WindowInsets.Companion.none get() = WindowInsets(0, 0, 0, 0)
val WindowInsets.Companion.bars
    @Composable get() =
        WindowInsets.displayCutout
            .add(WindowInsets.statusBars)
            .add(WindowInsets.navigationBars)

val WindowInsets.Companion.barsWithSystem
    @Composable get() =
        WindowInsets.displayCutout
            .add(WindowInsets.statusBars)
            .add(WindowInsets.navigationBars)
            .add(WindowInsets.systemBars)
