package com.dergoogler.mmrl.ui.component.card.component

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.ui.component.card.CardAbsoluteScopeInstance
import com.dergoogler.mmrl.ui.component.card.CardScope

/**
 * A composable function that positions its content absolutely within the card.
 *
 * @param modifier The modifier to be applied to the layout.
 * @param alignment The alignment of the content within the card. Defaults to [Alignment.TopStart].
 * @param content The composable content to be placed inside the absolute layout.
 */
@Composable
fun CardScope.Absolute(
    modifier: Modifier = Modifier,
    alignment: Alignment = Alignment.TopStart,
    content: @Composable CardScope.() -> Unit,
) = Box(
    modifier =
        Modifier
            .absolute(alignment = alignment)
            .then(modifier),
) {
    val instance = remember { CardAbsoluteScopeInstance(this@Absolute, this) }
    instance.content()
}
