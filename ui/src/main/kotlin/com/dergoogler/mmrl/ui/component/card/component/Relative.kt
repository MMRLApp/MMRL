package com.dergoogler.mmrl.ui.component.card.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.component.card.CardScope

/**
 * Composable function to define a relative layout within a Card.
 *
 * This composable allows you to position content relative to the Card's bounds.
 * It's essentially a [Box] that is marked with a special layout ID to be recognized
 * by the parent [Card] layout.
 *
 * @param modifier Optional [Modifier] to be applied to this relative layout.
 * @param content A composable lambda that receives a [BoxScope] to define the content
 *                that will be placed relatively within the Card.
 */
@Composable
fun CardScope.Relative(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) = Box(
    modifier = Modifier
        .relative()
        .then(modifier),
    content = content
)