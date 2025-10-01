package com.dergoogler.mmrl.ui.remember

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

@Composable
fun rememberVisibility(
    initial: Boolean = false,
    content: @Composable VisibilityScope.() -> Unit
): VisibilityActions {
    var visible by remember { mutableStateOf(initial) }

    val actions = remember {
        object : VisibilityActions {
            override fun show() { visible = true }
            override fun hide() { visible = false }
            override fun toggle() { visible = !visible }
        }
    }

    val scope = remember {
        object : VisibilityScope {
            override val isVisible get() = visible
            override fun dismiss() = actions.hide()
        }
    }

    if (visible) {
        scope.content()
    }

    return actions
}

interface VisibilityActions {
    fun show()
    fun hide()
    fun toggle()
}

interface VisibilityScope {
    val isVisible: Boolean
    fun dismiss()
}