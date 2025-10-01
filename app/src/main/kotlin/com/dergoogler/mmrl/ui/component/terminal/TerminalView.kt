package com.dergoogler.mmrl.ui.component.terminal

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.model.terminal.AlertBlock
import com.dergoogler.mmrl.model.terminal.Block
import com.dergoogler.mmrl.model.terminal.CardBlock
import com.dergoogler.mmrl.model.terminal.GroupBlock
import com.dergoogler.mmrl.model.terminal.TextBlock
import com.dergoogler.mmrl.ui.activity.terminal.Terminal
import com.dergoogler.mmrl.ui.component.NavigationBarsSpacer
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

val LocalTerminalWidth = staticCompositionLocalOf<Dp> {
    error("CompositionLocal LocalTerminalWidth not present")
}

val LocalTerminal = staticCompositionLocalOf<Terminal> {
    error("CompositionLocal LocalTerminal not present")
}
@Composable
fun TerminalView(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    terminal: Terminal,
    state: LazyListState,
    style: TextStyle = MaterialTheme.typography.bodySmall.copy(
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontFamily = FontFamily.Monospace
    ),
) {
    val userPrefs = LocalUserPreferences.current
    val density = LocalDensity.current
    val colorScheme = MaterialTheme.colorScheme

    val list by remember(terminal) {
        derivedStateOf { terminal.console }
    }

    val textStyle = remember(style, colorScheme) {
        style.copy(color = colorScheme.onSurfaceVariant)
    }

    var lazyColumnWidth by remember { mutableIntStateOf(0) }
    val widthDp = remember(lazyColumnWidth, density) {
        with(density) { lazyColumnWidth.toDp() }
    }

    LaunchedEffect(list.size) {
        if (list.isNotEmpty()) {
            state.animateScrollToItem(list.size - 1)
        }
    }

    ProvideTextStyle(value = textStyle) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .let {
                    if (userPrefs.terminalTextWrap) it
                    else it.horizontalScroll(rememberScrollState())
                }
        ) {
            SelectionContainer {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .onGloballyPositioned { coords ->
                            lazyColumnWidth = coords.size.width
                        }
                        .then(modifier),
                    state = state,
                    contentPadding = contentPadding
                ) {
                    itemsIndexed(
                        items = list,
                        key = { index, block ->
                            "${index}_${block.hashCode()}"
                        }
                    ) { _, block ->
                        CompositionLocalProvider(
                            LocalTerminal provides terminal,
                            LocalTerminalWidth provides widthDp
                        ) {
                            TerminalBlockContent(block)
                        }
                    }

                    item {
                        NavigationBarsSpacer()
                    }
                }
            }
        }
    }
}

@Composable
private fun TerminalBlockContent(block: Block) {
    when (block) {
        is GroupBlock -> GroupBlockView(block)
        is CardBlock -> CardView(block)
        is TextBlock -> TextView(block)
        is AlertBlock -> AlertView(block)
    }
}