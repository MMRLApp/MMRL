package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.ui.R
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.LocalListItemEnabled

/**
 * A composable function that displays a "Learn More" text, typically used as supporting content
 * within a [ListItemScope]. This text is clickable and will invoke the [onLearnMore] lambda
 * when pressed.
 *
 * The text style is derived from `MaterialTheme.typography.bodyMedium` with the color set to
 * `MaterialTheme.colorScheme.primary`.
 *
 * @param text The string resource ID for the text to be displayed. Defaults to `R.string.learn_more`.
 * @param enabled Controls the enabled state of the "Learn More" text. When `false`, it will not
 * react to clicks. Defaults to the value provided by `LocalListItemEnabled.current`.
 * @param onLearnMore A lambda function that will be invoked when the "Learn More" text is clicked.
 */
@Composable
fun ListItemScope.LearnMore(
    @StringRes text: Int = R.string.learn_more,
    enabled: Boolean = LocalListItemEnabled.current,
    onLearnMore: () -> Unit,
) {
    val style =
        MaterialTheme.typography.bodyMedium.copy(
            color = MaterialTheme.colorScheme.primary,
        )

    ProvideTextStyle(style) {
        Slot(ListItemSlot.Supporting) {
            Text(
                modifier =
                    Modifier.clickable(
                        enabled = enabled,
                        onClick = onLearnMore,
                    ),
                text = stringResource(text),
            )
        }
    }
}
