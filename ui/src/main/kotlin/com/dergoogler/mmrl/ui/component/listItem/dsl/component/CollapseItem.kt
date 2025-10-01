package com.dergoogler.mmrl.ui.component.listItem.dsl.component

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.semantics.Role
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.ui.R
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope

@Composable
fun ListScope.CollapseItem(
    modifier: Modifier = Modifier,
    isInitiallyExpanded: Boolean = false,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    meta: @Composable ListItemScope.(Int, Float) -> Unit,
    content: @Composable ListScope.() -> Unit,
) {
    var isExpanded by remember { mutableStateOf(isInitiallyExpanded) }

    val rotation by animateFloatAsState(
        targetValue = if (isExpanded) 360f else 0f,
        animationSpec = tween(durationMillis = 300), label = "coll"
    )

    val onClick: () -> Unit = {
        isExpanded = !isExpanded
    }

    val icon = if (isExpanded) R.drawable.chevron_up else R.drawable.chevron_down

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize()
            .alpha(alpha = if (enabled) 1f else 0.5f)
            .clickable(
                enabled = enabled,
                onClick = onClick,
                role = Role.Button,
                interactionSource = interactionSource,
                indication = ripple()
            )
            .then(modifier)
    ) {
        this@CollapseItem.Item {
            meta(icon, rotation)
        }

        isExpanded.takeTrue {
            List {
                this@CollapseItem.content()
            }
        }
    }
}