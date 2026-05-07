package com.dergoogler.mmrl.ui.component

import android.annotation.SuppressLint
import android.text.SpannableStringBuilder
import androidx.annotation.DrawableRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.text.HtmlCompat
import com.dergoogler.mmrl.ext.thenComposeInvoke
import com.dergoogler.mmrl.ext.toAnnotatedString

@Composable
fun HtmlText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    color: Color = LocalContentColor.current,
    onClick: (() -> Unit)? = null,
) {
    val spannableString = SpannableStringBuilder(text).toString()
    val spanned = HtmlCompat.fromHtml(spannableString, HtmlCompat.FROM_HTML_MODE_COMPACT)

    Text(
        modifier =
            if (onClick != null) {
                modifier.clickable(
                    onClick = onClick,
                )
            } else {
                modifier
            },
        style = style,
        color = color,
        text = spanned.toAnnotatedString(),
    )
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    clickTagColor: Color = MaterialTheme.colorScheme.surfaceTint,
    onTagClick: (String) -> Unit,
) {
    val boldPattern = """\*\*(.+?)\*\*""".toRegex() // Bold (double asterisks)
    val italicPattern = """(?<!\*)\*(?![*\s])(?:[^*]*[^*\s])?\*(?!\*)""".toRegex() // Italic (single asterisk with the new regex)
    val underlinePattern = """_(.+?)_""".toRegex() // Underline (underscores)
    val strikethroughPattern = """~~(.+?)~~""".toRegex() // Strikethrough (double tildes)
    val clickablePattern = """@(\w+)\((.*?)\)""".toRegex() // Clickable tag

    val annotatedString =
        buildAnnotatedString {
            var currentIndex = 0
            val processedRanges = mutableListOf<IntRange>()

            val matches = mutableListOf<Pair<MatchResult, SpanStyle?>>()

            listOf(
                boldPattern to SpanStyle(fontWeight = FontWeight.Bold),
                italicPattern to SpanStyle(fontStyle = FontStyle.Italic),
                underlinePattern to SpanStyle(textDecoration = TextDecoration.Underline),
                strikethroughPattern to SpanStyle(textDecoration = TextDecoration.LineThrough),
            ).forEach { (regex, style) ->
                matches.addAll(regex.findAll(text).map { it to style })
            }

            matches.addAll(clickablePattern.findAll(text).map { it to null })

            matches.sortBy { it.first.range.first }

            matches.forEach { (matchResult, style) ->
                val matchRange = matchResult.range
                if (processedRanges.any { it.first <= matchRange.last && it.last >= matchRange.first }) return@forEach

                if (currentIndex < matchRange.first) {
                    append(text.substring(currentIndex, matchRange.first))
                }

                if (style != null) {
                    if (style == SpanStyle(fontStyle = FontStyle.Italic)) {
                        val matchText = matchResult.value
                        val textForItalics = matchText.substring(1, matchText.length - 1)
                        withStyle(style) {
                            append(textForItalics)
                        }
                    } else {
                        withStyle(style) {
                            append(matchResult.groupValues[1])
                        }
                    }
                } else {
                    // Clickable tag
                    val id = matchResult.groupValues[1]
                    val displayText = matchResult.groupValues[2]
                    pushStringAnnotation(tag = "clickable", annotation = id)
                    withStyle(SpanStyle(color = clickTagColor)) {
                        append(displayText)
                    }
                    pop()
                }

                processedRanges.add(matchRange)
                currentIndex = matchRange.last + 1
            }

            if (currentIndex < text.length) {
                append(text.substring(currentIndex))
            }
        }

    var layoutResult: TextLayoutResult? by remember { mutableStateOf(null) }

    Text(
        text = annotatedString,
        style = style,
        onTextLayout = { layoutResult = it },
        modifier =
            Modifier
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        layoutResult?.let { layout ->
                            val position = layout.getOffsetForPosition(offset)
                            annotatedString
                                .getStringAnnotations(
                                    tag = "clickable",
                                    start = position,
                                    end = position,
                                ).firstOrNull()
                                ?.let { annotation ->
                                    onTagClick(annotation.item)
                                }
                        }
                    }
                }.then(modifier),
    )
}

@Composable
fun BulletList(
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    indent: Dp = 20.dp,
    lineSpacing: Dp = 0.dp,
    items: List<String>,
    onItemClick: () -> Unit = {},
) = Column(modifier = modifier) {
    items.forEach {
        Row {
            Text(
                text = "\u2022",
                style = style.copy(textAlign = TextAlign.Center),
                modifier = Modifier.width(indent),
            )
            HtmlText(
                text = it,
                style = style,
                modifier = Modifier.weight(1f, fill = true),
                onClick = onItemClick,
            )
        }
        if (lineSpacing > 0.dp && it != items.last()) {
            Spacer(modifier = Modifier.height(lineSpacing))
        }
    }
}

@Deprecated("Use TextRow instead", ReplaceWith("com.dergoogler.mmrl.ui.component.text.TextRow"))
@Composable
fun TextWithIcon(
    text: (@Composable RowScope.(TextStyle) -> Unit)? = null,
    icon: (@Composable RowScope.(Dp) -> Unit)? = null,
    style: TextStyle = LocalTextStyle.current,
    @SuppressLint("ModifierParameter") rowModifier: Modifier = Modifier,
    iconScalingFactor: Float = 1.4285715f,
    spacing: Float = 11.428572f,
    /**
     * Moved the icon to the right of the text.
     */
    rightIcon: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Center,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) {
    if (text == null && icon == null) return

    val iconSize = with(LocalDensity.current) { style.fontSize.toDp() * iconScalingFactor }
    val spacer = with(LocalDensity.current) { spacing.toDp() }

    Row(
        modifier = rowModifier,
        verticalAlignment = verticalAlignment,
        horizontalArrangement = horizontalArrangement,
    ) {
        if (icon != null && !rightIcon) {
            icon(iconSize)
            Spacer(modifier = Modifier.width(spacer))
        }
        if (text != null) {
            text(style)
        }
        if (icon != null && rightIcon) {
            Spacer(modifier = Modifier.width(spacer))
            icon(iconSize)
        }
    }
}

@Deprecated("Use TextWithIcon instead", ReplaceWith("com.dergoogler.mmrl.ui.component.text.TextWithIcon"))
@Composable
fun TextWithIcon(
    text: String,
    @SuppressLint("ModifierParameter") iconModifier: Modifier = Modifier,
    @SuppressLint("ModifierParameter") rowModifier: Modifier = Modifier,
    textModifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    style: TextStyle = LocalTextStyle.current,
    contentDescription: String? = null,
    tint: Color = LocalContentColor.current,
    iconScalingFactor: Float = 1.4285715f,
    spacing: Float = 11.428572f,
    maxLines: Int = Int.MAX_VALUE,
    overflow: TextOverflow = TextOverflow.Clip,
    showText: Boolean = true,
    /**
     * Moved the icon to the right of the text.
     */
    rightIcon: Boolean = false,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
) = TextWithIcon(
    rowModifier = rowModifier,
    style = style,
    iconScalingFactor = iconScalingFactor,
    spacing = spacing,
    rightIcon = rightIcon,
    text =
        text.thenComposeInvoke<String, RowScope, TextStyle>(showText) { it, stl ->
            Text(
                text = it,
                style = stl,
                modifier = textModifier,
                maxLines = maxLines,
                overflow = overflow,
            )
        },
    icon =
        icon.thenComposeInvoke<Int, RowScope, Dp> { it, size ->
            Icon(
                painter = painterResource(id = it),
                contentDescription = contentDescription,
                tint = tint,
                modifier =
                    Modifier
                        .size(size)
                        .then(iconModifier),
            )
        },
    verticalAlignment = verticalAlignment,
    horizontalArrangement = horizontalArrangement,
)
