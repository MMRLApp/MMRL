package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Slot

@Composable
fun ListScope.Section(
    title: String? = null,
    divider: Boolean = true,
    content: @Composable ListScope.() -> Unit,
) {
    val currentStyle = LocalTextStyle.current
    val style = currentStyle.copy(color = MaterialTheme.colorScheme.primary)

    title.nullable {
        Item(
            contentPadding = PaddingValues(vertical = 8.dp, horizontal = 25.dp),
        ) {
            Slot(ListItemSlot.Title) {
                ProvideTextStyle(style) {
                    Text(it)
                }
            }
        }
    }

    content()

    divider.takeTrue {
        HorizontalDivider(
            modifier = Modifier.padding(vertical = 8.dp),
            thickness = Dp.Hairline,
        )
    }
}
