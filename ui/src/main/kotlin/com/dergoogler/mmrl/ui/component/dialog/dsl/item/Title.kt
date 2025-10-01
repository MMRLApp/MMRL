package com.dergoogler.mmrl.ui.component.dialog.dsl.item

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.layoutId
import com.dergoogler.mmrl.ui.component.ProvideContentColorTextStyle
import com.dergoogler.mmrl.ui.component.dialog.dsl.DialogContainerScope
import com.dergoogler.mmrl.ui.component.dialog.dsl.DialogContainerSlot

@Composable
fun DialogContainerScope.Title(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = this.contentPadding.title,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.SpaceBetween,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    content: @Composable RowScope.() -> Unit
) = ProvideContentColorTextStyle(
    contentColor = style.titleContentColor,
    textStyle = MaterialTheme.typography.headlineSmall,
) {
    Row(
        modifier = Modifier
            .layoutId(DialogContainerSlot.TITLE)
            .fillMaxWidth()
            .padding(contentPadding)
            .then(modifier),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = verticalAlignment,
        content = content
    )
}