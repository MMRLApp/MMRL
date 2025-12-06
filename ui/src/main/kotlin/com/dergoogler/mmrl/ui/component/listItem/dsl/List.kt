package com.dergoogler.mmrl.ui.component.listItem.dsl

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

val DefaultContentPaddingValues = PaddingValues(vertical = 16.dp, horizontal = 25.dp)
val DefaultIconSize = 24.dp

/**
 * A composable function that displays a list of items using a DSL.
 *
 * @param modifier The modifier to be applied to the list.
 * @param contentPadding The padding to be applied to the content of the list.
 * @param content The content of the list, defined using the [ListScope] DSL.
 */
@Composable
fun List(
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    horizontalAlignment: Alignment.Horizontal = Alignment.Start,
    contentPadding: PaddingValues = DefaultContentPaddingValues,
    content: @Composable ListScope.() -> Unit,
) {
    Column(
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalAlignment = horizontalAlignment,
    ) {
        val instance =
            remember {
                ListScopeInstance(
                    columnScope = this,
                    contentPaddingValues = contentPadding,
                    iconSize = DefaultIconSize,
                )
            }

        instance.content()
    }
}
