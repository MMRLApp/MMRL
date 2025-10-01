package com.dergoogler.mmrl.ui.component.text

import androidx.annotation.DrawableRes
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ext.nullvoke

@Composable
fun IconText(
    text: String,
    @DrawableRes resId: Int?,
    alignment: Alignment.Horizontal = Alignment.Start,
    tint: Color = MaterialTheme.colorScheme.surfaceTint,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontStyle: FontStyle? = null,
    fontWeight: FontWeight? = null,
    fontFamily: FontFamily? = null,
    letterSpacing: TextUnit = TextUnit.Unspecified,
    textDecoration: TextDecoration? = null,
    textAlign: TextAlign? = null,
    lineHeight: TextUnit = TextUnit.Unspecified,
    overflow: TextOverflow = TextOverflow.Clip,
    softWrap: Boolean = true,
    maxLines: Int = Int.MAX_VALUE,
    minLines: Int = 1,
    onTextLayout: ((TextLayoutResult) -> Unit)? = null,
    onLinkClick: ((String) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
) {
    BBCodeText(
        modifier = modifier,
        text = text,
        prefix = (alignment == Alignment.Start) nullable "[icon=ugh] ",
        suffix = (alignment == Alignment.End) nullable " [icon=ugh]",
        bbEnabled = false,
        disabledTags = BBCodeTag.disableAllExcept(BBCodeTag.ICON, BBCodeTag.IMAGE),
        iconContent = resId nullvoke {
            {
                Icon(
                    painter = painterResource(id = this),
                    contentDescription = null,
                    tint = tint,
                )
            }
        },
        onLinkClick = onLinkClick,
        style = style,
        onTextLayout = onTextLayout,
        overflow = overflow,
        softWrap = softWrap,
        maxLines = maxLines,
        minLines = minLines,
        letterSpacing = letterSpacing,
        textDecoration = textDecoration,
        textAlign = textAlign,
        lineHeight = lineHeight,
        fontFamily = fontFamily,
        fontWeight = fontWeight,
        fontStyle = fontStyle,
        fontSize = fontSize,
        color = color
    )
}