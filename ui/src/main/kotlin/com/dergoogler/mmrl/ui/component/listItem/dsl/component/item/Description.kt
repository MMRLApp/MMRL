package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlotScope
import com.dergoogler.mmrl.ui.component.text.BBCodeText

/**
 * A description for the list item.
 *
 * This composable function is used to display a description within a list item.
 * It applies a specific text style (bodyMedium with outline color) to the content.
 *
 * @param content A composable lambda that defines the content of the description.
 *                This lambda is executed within a [BoxScope], allowing for flexible layout options.
 */
@Composable
fun ListItemScope.Description(
    styleTransform: (@Composable (TextStyle) -> TextStyle)? = null,
    content: @Composable ListItemSlotScope.() -> Unit,
) {
    val baseTextStyle = MaterialTheme.typography.bodyMedium.copy(
        color = MaterialTheme.colorScheme.outline
    )

    val finalTextStyle = if (styleTransform != null) {
        styleTransform(baseTextStyle)
    } else baseTextStyle

    Slot(
        slot = ListItemSlot.Description,
        content = {
            ProvideTextStyle(finalTextStyle) {
                content()
            }
        }
    )
}


/**
 * A description for the list item.
 *
 * @param text The description text. Supports style markup.
 */
@Composable
fun ListItemScope.Description(
    text: String,
    styleTransform: (@Composable (TextStyle) -> TextStyle)? = null,
) {
    this.Description(styleTransform) {
        BBCodeText(text)
    }
}


/**
 * A description for the list item.
 *
 * @param id The string resource ID for the description text.
 */
@Composable
fun ListItemScope.Description(
    @StringRes id: Int,
    styleTransform: (@Composable (TextStyle) -> TextStyle)? = null,
) = this.Description(stringResource(id), styleTransform)


/**
 * A description for the list item.
 *
 * @param id The string resource ID for the description text.
 * @param formatArgs The format arguments for the string resource.
 */
@Composable
fun ListItemScope.Description(
    @StringRes id: Int,
    vararg formatArgs: Any,
    styleTransform: (@Composable (TextStyle) -> TextStyle)? = null,
) = this.Description(stringResource(id, *formatArgs), styleTransform)

