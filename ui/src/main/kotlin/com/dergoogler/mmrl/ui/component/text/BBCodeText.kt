package com.dergoogler.mmrl.ui.component.text

import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.takeOrElse
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.core.graphics.toColorInt
import com.dergoogler.mmrl.ext.nullable
import java.util.Stack

/**
 * Enum class representing different types of BBCode tags that can be disabled.
 */
enum class BBCodeTag {
    BOLD,      // [b] and [bold]
    ITALIC,    // [i] and [italic]
    UNDERLINE, // [u] and [underline]
    COLOR,     // [color=...]
    BACKGROUND,// [bg=...]
    LINK,      // [link=...]
    ICON,      // [icon=...]
    IMAGE;     // [image=...]

    companion object {
        fun disableAllExcept(vararg tags: BBCodeTag): Set<BBCodeTag> {
            return entries.filterNot { it in tags }.toSet()
        }

        fun disableAll(): Set<BBCodeTag> {
            return entries.toSet()
        }

        fun enableAllExcept(vararg tags: BBCodeTag): Set<BBCodeTag> {
            return entries.filterNot { it in tags }.toSet()
        }
    }
}

/**
 * A Composable function that displays text with BBCode (Bulletin Board Code) formatting.
 *
 * This function allows for basic text styling such as bold, italic, underline, color,
 * background color, and links. It also supports embedding icons and images within the text.
 * Additionally, it supports prefix and suffix that are always applied regardless of bbEnabled setting.
 *
 * Example BBCode:
 * `[b]Bold text[/b]`
 * `[i]Italic text[/i]`
 * `[u]Underlined text[/u]`
 * `[color=red]Red text[/color]`
 * `[bg=yellow]Text with yellow background[/bg]`
 * `[link=https://example.com]Click here[/link]`
 * `[icon=icon_name]` (requires `iconContent` to be provided)
 * `[image=image_url]` (requires `imageContent` to be provided)
 *
 * Usage with prefix/suffix and selective tag disabling:
 * ```
 * BBCodeText(
 *     text = "name",
 *     disabledTags = BBCodeTag.disableAllExcept(BBCodeTag.ICON, BBCodeTag.IMAGE),
 * ) {
 *     prefix = "hello "
 *     suffix = " world"
 * }
 * ```
 *
 * If `bbEnabled` is set to `false`, the text will be displayed as plain text without any BBCode processing,
 * but prefix and suffix will still be applied.
 *
 * @param text The string containing the text to be displayed, potentially with BBCode tags.
 * @param prefix A string that will be displayed before the `text`.
 * @param suffix A string that will be displayed after the `text`.
 * @param bbEnabled A boolean indicating whether BBCode processing should be enabled. Defaults to `true`.
 * @param disabledTags A set of BBCodeTag values representing which tag types should be disabled.
 *                     When a tag type is disabled, it will be treated as plain text instead of formatting.
 * @param iconContent An optional Composable lambda that takes an icon name (String) and renders the corresponding icon.
 *                    This is used when `[icon=...]` tags are present in the `text`.
 * @param imageContent An optional Composable lambda that takes an image URL (String) and renders the corresponding image.
 *                     This is used when `[image=...]` tags are present in the `text`.
 * @param modifier The modifier to be applied to this layout node.
 * @param color The color of the text. `Color.Unspecified` uses the color from the `style` and then the
 *              `LocalContentColor`.
 * @param fontSize The size of glyphs to use when painting the text. See [TextStyle.fontSize].
 * @param fontStyle The typeface variant to use when drawing the letters (e.g., italic).
 *                  See [TextStyle.fontStyle].
 * @param fontWeight The typeface thickness to use when painting the text (e.g., [FontWeight.Bold]).
 * @param fontFamily The font family to be used when rendering the text. See [TextStyle.fontFamily].
 */
@Composable
fun BBCodeText(
    text: String,
    prefix: String? = null,
    suffix: String? = null,
    bbEnabled: Boolean = true,
    disabledTags: Set<BBCodeTag> = emptySet(),
    iconContent: (@Composable (String) -> Unit)? = null,
    imageContent: (@Composable (String) -> Unit)? = null,
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
    style: TextStyle = LocalTextStyle.current,
    onLinkClick: ((String) -> Unit)? = null,
) {
    val currentColor = LocalContentColor.current
    val textColor by remember(color) {
        derivedStateOf {
            color.takeOrElse { style.color.takeOrElse { currentColor } }
        }
    }

    val handler = LocalUriHandler.current

    var layoutResult: TextLayoutResult? by remember { mutableStateOf(null) }

    val (bbText, inline) = text.toStyleMarkup(disabledTags, iconContent, imageContent)

    val string = buildAnnotatedString {
        if (prefix != null) {
            val (prefix, prefixInlineContent) = prefix.toStyleMarkup(
                iconContent = iconContent,
                imageContent = imageContent
            )

            inline.putAll(prefixInlineContent)

            append(prefix)
        }

        if (bbEnabled) {
            append(bbText)
        } else {
            append(text)
        }

        if (suffix != null) {
            val (suffix, suffixInlineContent) = suffix.toStyleMarkup(
                iconContent = iconContent,
                imageContent = imageContent
            )

            inline.putAll(suffixInlineContent)

            append(suffix)
        }
    }

    Text(
        text = string,
        modifier = modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent(PointerEventPass.Initial)
                    if (event.changes.any { it.pressed }) {
                        val offset = event.changes.first().position
                        layoutResult?.let { layout ->
                            val position = layout.getOffsetForPosition(offset)

                            val ann = string
                                .getStringAnnotations(tag = "URL", start = position, end = position)
                                .firstOrNull()

                            ann?.let {
                                if (onLinkClick != null) {
                                    onLinkClick(it.item)
                                } else {
                                    handler.openUri(it.item)
                                }
                            }
                        }
                    }
                }
            }
        },
        style = style,
        inlineContent = inline,
        onTextLayout = {
            layoutResult = it
            onTextLayout.nullable { it2 ->
                it2(it)
            }
        },
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
        color = textColor
    )
}

private data class StyleState(
    val tag: BBCodeTag? = null,
    val color: Color = Color.Unspecified,
    val bg: Color = Color.Unspecified,
    val bold: Boolean = false,
    val italic: Boolean = false,
    val underline: Boolean = false,
    val link: String? = null,
)

@Composable
private fun String.toStyleMarkup(
    disabledTags: Set<BBCodeTag> = emptySet(),
    iconContent: (@Composable (String) -> Unit)? = null,
    imageContent: (@Composable (String) -> Unit)? = null,
): Pair<AnnotatedString, MutableMap<String, InlineTextContent>> {
    val tagRegex =
        Regex("""\[(/?)(color|link|bg|bold|b|italic|i|underline|u|icon|image)(?:=([^]]+))?/?]""")
    val matches = tagRegex.findAll(this)
    val styleStack = Stack<StyleState>().apply { push(StyleState()) }

    val inlineContent = remember { mutableMapOf<String, InlineTextContent>() }
    var iconCounter by remember { mutableIntStateOf(0) }
    var imageCounter by remember { mutableIntStateOf(0) }

    val annotated = buildAnnotatedString {
        var lastIndex = 0
        matches.forEach { match ->
            val text = this@toStyleMarkup.substring(lastIndex, match.range.first)
            if (text.isNotEmpty()) {
                applyStyle(this, text, styleStack.peek())
            }

            val isClosing = match.groupValues[1] == "/"
            val tag = match.groupValues[2]
            val value = match.groupValues[3]

            // Check if the tag type is disabled
            val tagType = when (tag) {
                "b", "bold" -> BBCodeTag.BOLD
                "i", "italic" -> BBCodeTag.ITALIC
                "u", "underline" -> BBCodeTag.UNDERLINE
                "color" -> BBCodeTag.COLOR
                "bg" -> BBCodeTag.BACKGROUND
                "link" -> BBCodeTag.LINK
                "icon" -> BBCodeTag.ICON
                "image" -> BBCodeTag.IMAGE
                else -> null
            }

            // If the tag type is disabled, treat it as plain text
            if (tagType != null && tagType in disabledTags) {
                val fullMatch = this@toStyleMarkup.substring(match.range)
                applyStyle(this, fullMatch, styleStack.peek())
                lastIndex = match.range.last + 1
                return@forEach
            }

            when {
                isClosing -> {
                    if (styleStack.size > 1 && styleStack.peek().tag == tagType) {
                        styleStack.pop()
                    }
                }

                tagType == BBCodeTag.ICON && iconContent != null && value.isNotBlank() -> {
                    val iconId = "inlineIcon_${iconCounter++}"
                    appendInlineContent(iconId, "[$value]")

                    inlineContent[iconId] = InlineTextContent(
                        Placeholder(
                            width = LocalTextStyle.current.fontSize,
                            height = LocalTextStyle.current.fontSize,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        runCatching {
                            iconContent(value)
                        }
                    }
                }

                tagType == BBCodeTag.IMAGE && imageContent != null && value.isNotBlank() -> {
                    val imageId = "inlineImage_${imageCounter++}"
                    appendInlineContent(imageId, "[$value]")

                    inlineContent[imageId] = InlineTextContent(
                        Placeholder(
                            width = LocalTextStyle.current.fontSize,
                            height = LocalTextStyle.current.fontSize,
                            placeholderVerticalAlign = PlaceholderVerticalAlign.Center
                        )
                    ) {
                        runCatching {
                            imageContent(value)
                        }
                    }
                }

                else -> {
                    val current = styleStack.peek()
                    val newStyle = when (tagType) {
                        BBCodeTag.COLOR -> current.copy(tag = tagType, color = colorFromName(value))
                        BBCodeTag.BACKGROUND -> current.copy(tag = tagType, bg = colorFromName(value))
                        BBCodeTag.BOLD -> current.copy(tag = tagType, bold = true)
                        BBCodeTag.ITALIC -> current.copy(tag = tagType, italic = true)
                        BBCodeTag.UNDERLINE -> current.copy(tag = tagType, underline = true)
                        BBCodeTag.LINK -> current.copy(
                            tag = tagType,
                            link = value,
                            color = MaterialTheme.colorScheme.primary,
                            underline = true
                        )

                        else -> current
                    }
                    styleStack.push(newStyle)
                }
            }

            lastIndex = match.range.last + 1
        }

        if (lastIndex < this@toStyleMarkup.length) {
            val remaining = this@toStyleMarkup.substring(lastIndex)
            applyStyle(this, remaining, styleStack.peek())
        }
    }

    return annotated to inlineContent
}

private fun applyStyle(builder: AnnotatedString.Builder, text: String, style: StyleState) {
    val spanStyle = SpanStyle(
        color = style.color,
        background = style.bg,
        fontWeight = if (style.bold) FontWeight.Bold else FontWeight.Normal,
        fontStyle = if (style.italic) FontStyle.Italic else FontStyle.Normal,
        textDecoration = if (style.underline) TextDecoration.Underline else null,
    )

    if (style.link != null) {
        builder.pushStringAnnotation(tag = "URL", annotation = style.link)
        builder.withStyle(spanStyle) { append(text) }
        builder.pop()
    } else {
        builder.withStyle(spanStyle) { append(text) }
    }
}

@Composable
private fun colorFromName(name: String?): Color {
    if (name == null) return Color.Unspecified
    if (name.startsWith("#")) {
        return runCatching { Color(name.toColorInt()) }.getOrElse { Color.Unspecified }
    }
    return when (name.lowercase()) {
        "black" -> Color.Black
        "red" -> Color.Red
        "green" -> Color.Green
        "yellow" -> Color.Yellow
        "blue" -> Color.Blue
        "magenta" -> Color.Magenta
        "cyan" -> Color.Cyan
        "white" -> Color.White
        "gray", "grey" -> Color.Gray
        "lightgray" -> Color.LightGray
        "primary" -> MaterialTheme.colorScheme.primary
        "secondary" -> MaterialTheme.colorScheme.secondary
        "tertiary" -> MaterialTheme.colorScheme.tertiary
        "background" -> MaterialTheme.colorScheme.background
        "surface" -> MaterialTheme.colorScheme.surface
        "error" -> MaterialTheme.colorScheme.error
        "outline" -> MaterialTheme.colorScheme.outline
        "inverse_surface" -> MaterialTheme.colorScheme.inverseSurface
        "inverse_on_surface" -> MaterialTheme.colorScheme.inverseOnSurface
        "inverse_primary" -> MaterialTheme.colorScheme.inversePrimary
        "surface_variant" -> MaterialTheme.colorScheme.surfaceVariant
        "on_surface_variant" -> MaterialTheme.colorScheme.onSurfaceVariant
        "surface_tint" -> MaterialTheme.colorScheme.surfaceTint
        "on_surface" -> MaterialTheme.colorScheme.onSurface
        "on_primary" -> MaterialTheme.colorScheme.onPrimary
        "on_secondary" -> MaterialTheme.colorScheme.onSecondary
        "on_tertiary" -> MaterialTheme.colorScheme.onTertiary
        "on_background" -> MaterialTheme.colorScheme.onBackground
        "on_error" -> MaterialTheme.colorScheme.onError
        else -> Color.Unspecified
    }
}

/**
 * Removes all BBCode markup from a string.
 *
 * This function uses a regular expression to find and remove all BBCode tags,
 * leaving only the plain text content.
 *
 * For example, `"[b]Hello [color=red]world[/color][/b]"` would become `"Hello world"`.
 *
 * @return The string with all BBCode markup removed.
 */
fun String.stripBBCodeMarkup(): String {
    val regex = Regex("""\[(/?)(\w+)(=[^]]+)?]""")
    return this.replace(regex, "")
}