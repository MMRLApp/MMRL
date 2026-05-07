package com.dergoogler.mmrl.ui.component

import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.dergoogler.mmrl.ui.token.TypographyKeyTokens
import com.dergoogler.mmrl.ui.token.value

@Composable
fun ProvideContentColorTextStyle(
    contentColor: Color,
    textStyle: TextStyle,
    content: @Composable () -> Unit,
) {
    val mergedStyle = LocalTextStyle.current.merge(textStyle)
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalTextStyle provides mergedStyle,
        content = content,
    )
}

@Composable
fun ProvideTextStyleFromToken(
    color: Color,
    textToken: TypographyKeyTokens,
    content: @Composable () -> Unit,
) = ProvideContentColorTextStyle(
    contentColor = color,
    textStyle = textToken.value,
    content = content,
)
