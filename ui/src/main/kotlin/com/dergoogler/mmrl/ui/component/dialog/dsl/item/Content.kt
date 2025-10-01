package com.dergoogler.mmrl.ui.component.dialog.dsl.item

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
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
fun DialogContainerScope.Content(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = this.contentPadding.content,
    contentAlignment: Alignment = Alignment.CenterStart,
    propagateMinConstraints: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) = ProvideContentColorTextStyle(
    contentColor = style.textContentColor,
    textStyle = MaterialTheme.typography.bodyMedium,
) {
    Box(
        modifier = Modifier
            .layoutId(DialogContainerSlot.CONTENT)
            .padding(contentPadding)
            .then(modifier),
        contentAlignment = contentAlignment,
        propagateMinConstraints = propagateMinConstraints,
        content = content
    )
}
