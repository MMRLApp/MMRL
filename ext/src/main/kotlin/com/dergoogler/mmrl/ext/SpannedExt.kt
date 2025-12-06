package com.dergoogler.mmrl.ext

import android.graphics.Typeface
import android.text.Spanned
import android.text.style.BackgroundColorSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StrikethroughSpan
import android.text.style.StyleSpan
import android.text.style.SubscriptSpan
import android.text.style.SuperscriptSpan
import android.text.style.URLSpan
import android.text.style.UnderlineSpan
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextDecoration

@Composable
fun Spanned.toAnnotatedString(): AnnotatedString =
    buildAnnotatedString {
        val spanned = this@toAnnotatedString
        append(spanned.toString())
        getSpans(0, spanned.length, Any::class.java).forEach { span ->
            val start = getSpanStart(span)
            val end = getSpanEnd(span)
            when (span) {
                is StyleSpan ->
                    when (span.style) {
                        Typeface.BOLD -> addStyle(SpanStyle(fontWeight = FontWeight.Bold), start, end)
                        Typeface.ITALIC -> addStyle(SpanStyle(fontStyle = FontStyle.Italic), start, end)
                        Typeface.BOLD_ITALIC ->
                            addStyle(
                                SpanStyle(
                                    fontWeight = FontWeight.Bold,
                                    fontStyle = FontStyle.Italic,
                                ),
                                start,
                                end,
                            )
                    }

                is UnderlineSpan ->
                    addStyle(
                        SpanStyle(textDecoration = TextDecoration.Underline),
                        start,
                        end,
                    )

                is ForegroundColorSpan ->
                    addStyle(
                        SpanStyle(color = Color(span.foregroundColor)),
                        start,
                        end,
                    )

                is StrikethroughSpan ->
                    addStyle(
                        SpanStyle(textDecoration = TextDecoration.LineThrough),
                        start,
                        end,
                    )

                is SuperscriptSpan ->
                    addStyle(
                        SpanStyle(baselineShift = BaselineShift.Superscript),
                        start,
                        end,
                    )

                is SubscriptSpan ->
                    addStyle(
                        SpanStyle(baselineShift = BaselineShift.Subscript),
                        start,
                        end,
                    )

                is URLSpan ->
                    addStyle(
                        SpanStyle(
                            color = MaterialTheme.colorScheme.surfaceTint,
                        ),
                        start,
                        end,
                    )

//            is android.text.style.ImageSpan -> addStyle(
//                SpanStyle(textDecoration = TextDecoration.Underline),
//                start,
//                end
//            )
//
//            is android.text.style.QuoteSpan -> addStyle(
//                SpanStyle(textDecoration = TextDecoration.Underline),
//                start,
//                end
//            )
//
//            is android.text.style.BulletSpan -> addStyle(
//                SpanStyle(textDecoration = TextDecoration.Underline),
//                start,
//                end
//            )
//
//            is android.text.style.LeadingMarginSpan -> addStyle(
//                SpanStyle(textDecoration = TextDecoration.Underline),
//                start,
//                end
//            )
//
//            is android.text.style.AlignmentSpan -> addStyle(
//                SpanStyle(textDecoration = TextDecoration.Underline),
//                start,
//                end
//            )
//
//            is android.text.style.ScaleXSpan -> addStyle(
//                SpanStyle(textDecoration = TextDecoration.Underline),
//                start,
//                end
//            )
//
//            is android.text.style.LineHeightSpan -> addStyle(
//                SpanStyle(textDecoration = TextDecoration.Underline),
//                start,
//                end
//            )
//
//            is android.text.style.DynamicDrawableSpan -> addStyle(
//                SpanStyle(textDecoration = TextDecoration.Underline),
//                start,
//                end
//            )
//
//            is android.text.style.TextAppearanceSpan -> addStyle(
//                SpanStyle(textDecoration = TextDecoration.Underline),
//                start,
//                end
//            )

                is BackgroundColorSpan ->
                    addStyle(
                        SpanStyle(background = Color(span.backgroundColor)),
                        start,
                        end,
                    )
            }
        }
    }
