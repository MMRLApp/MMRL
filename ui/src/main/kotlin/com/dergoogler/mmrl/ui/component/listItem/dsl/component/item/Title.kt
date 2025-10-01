package com.dergoogler.mmrl.ui.component.listItem.dsl.component.item

import androidx.annotation.StringRes
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlotScope
import com.dergoogler.mmrl.ui.token.LocalTypography
import com.dergoogler.mmrl.ui.token.TypographyKeyTokens
import com.dergoogler.mmrl.ui.token.fromToken
import com.dergoogler.mmrl.ui.token.value

@Composable
fun ListItemScope.Title(
    content: @Composable ListItemSlotScope.() -> Unit,
) {
    ProvideTitleTypography(LocalTitleStyle.current) {
        Slot(
            slot = ListItemSlot.Title,
            content = content
        )
    }
}

@Composable
fun ListItemScope.Title(
    text: String,
    styleTransform: (@Composable (TextStyle) -> TextStyle)? = null,
) = Title {
    val titleStyle = LocalTitleStyle.current
    val textStyle = LocalTypography.current.fromToken(titleStyle)

    val finalTextStyle = if (styleTransform != null) {
        styleTransform(textStyle)
    } else textStyle

    ProvideTextStyle(finalTextStyle) {
        Text(text)
    }
}

@Composable
fun ListItemScope.Title(
    @StringRes id: Int,
    styleTransform: (@Composable (TextStyle) -> TextStyle)? = null,
) = Title(stringResource(id), styleTransform)

@Composable
fun ListItemScope.Title(
    @StringRes id: Int,
    styleTransform: (@Composable (TextStyle) -> TextStyle)? = null,
    vararg formatArgs: Any,
) = Title(stringResource(id, formatArgs), styleTransform)

val LocalTitleStyle = staticCompositionLocalOf {
    TypographyKeyTokens.BodyLarge
}

@Composable
fun ProvideTitleTypography(
    token: TypographyKeyTokens,
    content: @Composable () -> Unit,
) {
    val typography = token.value
    val mergedStyle = LocalTextStyle.current.merge(typography)
    CompositionLocalProvider(
        LocalTextStyle provides mergedStyle,
        LocalTitleStyle provides token,
        content = content
    )
}
