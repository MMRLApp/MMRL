package com.dergoogler.mmrl.ui.component.terminal

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences

@Composable
fun Line(
    modifier: Modifier = Modifier,
    index: Int?,
    content: @Composable RowScope.() -> Unit,
) {
    val userPrefs = LocalUserPreferences.current
    val lineNumbersEnabled by rememberUpdatedState(userPrefs.showTerminalLineNumbers)

    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .then(modifier)
                .padding(
                    start = if (lineNumbersEnabled) 0.dp else 8.dp,
                    end = 8.dp,
                ),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        LineNumber(number = index, enabled = lineNumbersEnabled)
        content()
    }
}

@Composable
fun RowScope.LineNumber(
    number: Int?,
    enabled: Boolean,
) {
    if (!enabled) return

    val index =
        remember(number) {
            if (number == null || number == -1) "" else number.toString()
        }

    val style = LocalTextStyle.current
    val colorScheme = MaterialTheme.colorScheme

    val outlineColor =
        remember {
            colorScheme.outline.copy(alpha = 0.6f)
        }

    DisableSelection {
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.TopEnd,
        ) {
            Text(
                text = index,
                style = style.copy(color = outlineColor),
            )
        }
    }
}
