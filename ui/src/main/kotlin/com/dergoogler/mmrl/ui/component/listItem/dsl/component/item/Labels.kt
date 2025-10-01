package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.FlowRowScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ListItemScope.Labels(
    content: @Composable FlowRowScope.() -> Unit,
) {
    Slot(ListItemSlot.Supporting) {
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            content()
        }
    }
}