package com.dergoogler.mmrl.ui.component

import android.content.Context
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.mikepenz.markdown.compose.Markdown
import com.mikepenz.markdown.model.ImageData
import com.mikepenz.markdown.model.ImageTransformer
import com.mikepenz.markdown.model.MarkdownColors
import com.mikepenz.markdown.model.MarkdownTypography
import com.mikepenz.markdown.model.markdownAnnotator
import com.mikepenz.markdown.utils.EntityConverter
import org.intellij.markdown.MarkdownElementTypes
import org.intellij.markdown.ast.ASTNode
import org.intellij.markdown.ast.getTextInNode
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.flavours.gfm.GFMTokenTypes

class MarkdownImageTransformerImpl(
    private val context: Context,
) : ImageTransformer {
    @Composable
    override fun transform(link: String): ImageData =
        rememberAsyncImagePainter(
            model =
                ImageRequest
                    .Builder(context)
                    .data(link)
                    .memoryCacheKey(link)
                    .diskCacheKey(link)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .build(),
        ).let { ImageData(it) }

    @Composable
    override fun intrinsicSize(painter: Painter): Size {
        var size by remember(painter) { mutableStateOf(painter.intrinsicSize) }
        if (painter is AsyncImagePainter) {
            val painterState = painter.state
            val intrinsicSize = painterState.painter?.intrinsicSize
            intrinsicSize?.also { size = it }
        }
        return size
    }
}

@Composable
fun MarkdownText(
    text: String,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    colors: MarkdownColors = MarkdownDefaults.colors(),
    typography: MarkdownTypography = MarkdownDefaults.typography(style = style),
) = Markdown(
    modifier = modifier,
    content = text,
    imageTransformer = MarkdownImageTransformerImpl(LocalContext.current),
    colors = colors,
    flavour =
        GFMFlavourDescriptor(
            useSafeLinks = true,
            absolutizeAnchorLinks = true,
        ),
    annotator =
        markdownAnnotator { content, child ->
            fun ASTNode.getUnescapedTextInNode(allFileText: CharSequence): String {
                val escapedText = getTextInNode(allFileText).toString()
                return EntityConverter.replaceEntities(
                    escapedText,
                    processEntities = false,
                    processEscapes = true,
                )
            }

            when (child.type) {
                MarkdownElementTypes.AUTOLINK -> {
                    append(child.getUnescapedTextInNode(content))
                    true
                }

                GFMTokenTypes.GFM_AUTOLINK -> {
                    append(child.getUnescapedTextInNode(content))
                    true
                }

                else -> false
            }
        },
    typography = typography,
)

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

@Immutable
class MarkdownColorsStyle internal constructor(
    override val text: Color,
    override val codeText: Color,
    override val inlineCodeText: Color,
    override val linkText: Color,
    override val codeBackground: Color,
    override val inlineCodeBackground: Color,
    override val dividerColor: Color,
) : MarkdownColors {
    @Suppress("RedundantIf")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is MarkdownColorsStyle) return false

        if (text != other.text) return false
        if (codeText != other.codeText) return false
        if (inlineCodeText != other.inlineCodeText) return false
        if (linkText != other.linkText) return false
        if (codeBackground != other.codeBackground) return false
        if (inlineCodeBackground != other.inlineCodeBackground) return false
        if (dividerColor != other.dividerColor) return false

        return true
    }

    fun copy(
        text: Color = this.text,
        codeText: Color = this.codeText,
        inlineCodeText: Color = this.inlineCodeText,
        linkText: Color = this.linkText,
        codeBackground: Color = this.codeBackground,
        inlineCodeBackground: Color = this.inlineCodeBackground,
        dividerColor: Color = this.dividerColor,
    ): MarkdownColorsStyle =
        MarkdownColorsStyle(
            text = text,
            codeText = codeText,
            inlineCodeText = inlineCodeText,
            linkText = linkText,
            codeBackground = codeBackground,
            inlineCodeBackground = inlineCodeBackground,
            dividerColor = dividerColor,
        )

    override fun hashCode(): Int {
        var result = text.hashCode()
        result = 31 * result + codeText.hashCode()
        result = 31 * result + inlineCodeText.hashCode()
        result = 31 * result + linkText.hashCode()
        result = 31 * result + codeBackground.hashCode()
        result = 31 * result + inlineCodeBackground.hashCode()
        result = 31 * result + dividerColor.hashCode()

        return result
    }
}

@Immutable
class MarkdownTextStyle internal constructor(
    override val h1: TextStyle,
    override val h2: TextStyle,
    override val h3: TextStyle,
    override val h4: TextStyle,
    override val h5: TextStyle,
    override val h6: TextStyle,
    override val text: TextStyle,
    override val code: TextStyle,
    override val inlineCode: TextStyle,
    override val quote: TextStyle,
    override val paragraph: TextStyle,
    override val ordered: TextStyle,
    override val bullet: TextStyle,
    override val list: TextStyle,
    override val link: TextStyle,
) : MarkdownTypography {
    @Suppress("RedundantIf")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is MarkdownTextStyle) return false

        if (h1 != other.h1) return false
        if (h2 != other.h2) return false
        if (h3 != other.h3) return false
        if (h4 != other.h4) return false
        if (h5 != other.h5) return false
        if (h6 != other.h6) return false
        if (text != other.text) return false
        if (code != other.code) return false
        if (inlineCode != other.inlineCode) return false
        if (quote != other.quote) return false
        if (paragraph != other.paragraph) return false
        if (ordered != other.ordered) return false
        if (bullet != other.bullet) return false
        if (list != other.list) return false
        if (link != other.link) return false

        return true
    }

    fun copy(
        h1: TextStyle = this.h1,
        h2: TextStyle = this.h2,
        h3: TextStyle = this.h3,
        h4: TextStyle = this.h4,
        h5: TextStyle = this.h5,
        h6: TextStyle = this.h6,
        text: TextStyle = this.text,
        code: TextStyle = this.code,
        inlineCode: TextStyle = this.inlineCode,
        quote: TextStyle = this.quote,
        paragraph: TextStyle = this.paragraph,
        ordered: TextStyle = this.ordered,
        bullet: TextStyle = this.bullet,
        list: TextStyle = this.list,
        link: TextStyle = this.link,
    ): MarkdownTextStyle =
        MarkdownTextStyle(
            h1 = h1,
            h2 = h2,
            h3 = h3,
            h4 = h4,
            h5 = h5,
            h6 = h6,
            text = text,
            code = code,
            inlineCode = inlineCode,
            quote = quote,
            paragraph = paragraph,
            ordered = ordered,
            bullet = bullet,
            list = list,
            link = link,
        )

    override fun hashCode(): Int {
        var result = h1.hashCode()
        result = 31 * result + h2.hashCode()
        result = 31 * result + h3.hashCode()
        result = 31 * result + h4.hashCode()
        result = 31 * result + h5.hashCode()
        result = 31 * result + h6.hashCode()

        result = 31 * result + text.hashCode()
        result = 31 * result + code.hashCode()
        result = 31 * result + inlineCode.hashCode()
        result = 31 * result + quote.hashCode()
        result = 31 * result + paragraph.hashCode()
        result = 31 * result + ordered.hashCode()
        result = 31 * result + bullet.hashCode()
        result = 31 * result + list.hashCode()
        result = 31 * result + link.hashCode()
        return result
    }
}

object MarkdownDefaults {
    @Composable
    fun typography(
        style: TextStyle = LocalTextStyle.current,
        h1: TextStyle = MaterialTheme.typography.headlineLarge,
        h2: TextStyle = MaterialTheme.typography.headlineMedium,
        h3: TextStyle = MaterialTheme.typography.headlineSmall,
        h4: TextStyle = MaterialTheme.typography.titleLarge,
        h5: TextStyle = MaterialTheme.typography.titleMedium,
        h6: TextStyle = MaterialTheme.typography.titleSmall,
        text: TextStyle = style,
        code: TextStyle =
            style.copy(
                background = MaterialTheme.colorScheme.surfaceContainer,
            ),
        inlineCode: TextStyle = style,
        quote: TextStyle = style,
        paragraph: TextStyle = style,
        ordered: TextStyle = style,
        bullet: TextStyle = style,
        list: TextStyle = style,
        link: TextStyle =
            style.copy(
                color = MaterialTheme.colorScheme.primary,
                textDecoration = TextDecoration.Underline,
            ),
    ) = MarkdownTextStyle(
        h1 = h1,
        h2 = h2,
        h3 = h3,
        h4 = h4,
        h5 = h5,
        h6 = h6,
        text = text,
        code = code,
        inlineCode = inlineCode,
        quote = quote,
        paragraph = paragraph,
        ordered = ordered,
        bullet = bullet,
        list = list,
        link = link,
    )

    @Composable
    fun colors(
        text: Color = MaterialTheme.colorScheme.onBackground,
        codeText: Color = MaterialTheme.colorScheme.onBackground,
        inlineCodeText: Color = MaterialTheme.colorScheme.onBackground,
        linkText: Color = MaterialTheme.colorScheme.primary,
        codeBackground: Color = MaterialTheme.colorScheme.background,
        inlineCodeBackground: Color = MaterialTheme.colorScheme.background,
        dividerColor: Color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.12f),
    ) = MarkdownColorsStyle(
        text = text,
        codeText = codeText,
        inlineCodeText = inlineCodeText,
        linkText = linkText,
        codeBackground = codeBackground,
        inlineCodeBackground = inlineCodeBackground,
        dividerColor = dividerColor,
    )
}
