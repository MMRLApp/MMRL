package com.dergoogler.mmrl.ui.component.dialog.dsl.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import com.dergoogler.mmrl.ui.component.dialog.dsl.DialogContainerScope
import com.dergoogler.mmrl.ui.component.dialog.dsl.DialogContainerSlot

@Composable
fun DialogContainerScope.Buttons(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.End,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) = Row(
    modifier = Modifier
        .layoutId(DialogContainerSlot.BUTTONS)
        .fillMaxWidth()
        .padding(contentPadding.buttons)
        .then(modifier),
    horizontalArrangement = horizontalArrangement,
    verticalAlignment = verticalAlignment,
    content = content
)


