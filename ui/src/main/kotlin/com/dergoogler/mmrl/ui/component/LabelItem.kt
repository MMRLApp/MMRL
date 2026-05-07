package com.dergoogler.mmrl.ui.component

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.text.toUpperCase
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dergoogler.mmrl.ext.iconSize
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.ui.R
import com.dergoogler.mmrl.ui.component.text.TextRow
import com.dergoogler.mmrl.ui.component.text.TextWIthIconIconScaling

/**
 * A composable function that displays a label item with optional icon and customizable style.
 *
 * This composable is used to render a label with a text and an optional icon. It provides
 * flexibility in styling through the [LabelItemStyle] parameter.
 *
 * @param modifier The modifier to be applied to the label item.
 * @param text A composable function that defines the text content of the label.
 * @param icon An optional composable function that defines the icon to be displayed alongside the text.
 *             If null, no icon will be shown.
 * @param style The style to be applied to the label item. Defaults to [LabelItemDefaults.style].
 *
 * Example Usage:
 *
 * ```
 * LabelItem(
 *     text = { Text("My Label") },
 *     icon = { Icon(Icons.Filled.Info, contentDescription = "Info") }
 * )
 *
 * LabelItem(
 *     text = { Text("Another Label") },
 *     style = LabelItemStyle(
 *          containerColor = Color.LightGray,
 *          shape = RoundedCornerShape(8.dp)
 *     )
 * )
 *
 * LabelItem(
 *      text = { Text("Label without icon") },
 *      icon = null,
 *      modifier = Modifier.padding(8.dp)
 * )
 * ```
 *
 * @see LabelItemStyle
 * @see LabelItemDefaults
 * @see TextRow
 */
@Composable
fun LabelItem(
    modifier: Modifier = Modifier,
    text: @Composable () -> Unit,
    icon: @Composable (() -> Unit)? = null,
    style: LabelItemStyle = LabelItemDefaults.style,
) {
    val density = LocalDensity.current

    val decoratedIconContent: @Composable (() -> Unit)? =
        icon.nullable {
            {
                Box(
                    modifier =
                        Modifier.iconSize(
                            density = density,
                            textStyle = style.textStyle,
                            scaling = TextWIthIconIconScaling,
                        ),
                ) {
                    it()
                }
            }
        }

    TextRow(
        modifier =
            Modifier
                .background(
                    color = style.containerColor,
                    shape = style.shape,
                ).padding(horizontal = 4.dp)
                .then(modifier),
        contentPadding = PaddingValues(start = 2.dp, end = 2.dp),
        horizontalArrangement = Arrangement.Center,
        leadingContent = decoratedIconContent,
        content = text,
    )
}

/**
 * A composable function that displays a label item with optional icon and custom style.
 *
 * @param text The text to be displayed in the label.
 * @param modifier The modifier to be applied to the label item.
 * @param icon The drawable resource ID for the optional icon. If null, no icon is displayed.
 * @param style The [LabelItemStyle] to customize the appearance of the label item.
 * @param upperCase Whether to display the text in uppercase. Defaults to true.
 *
 * Example Usage:
 * ```
 * LabelItem(text = "My Label", icon = R.drawable.ic_my_icon)
 *
 * LabelItem(text = "Another Label", style = LabelItemStyle(Color.Blue, TextStyle(fontSize = 16.sp)), upperCase = false)
 *
 * LabelItem(text = "Simple Label")
 * ```
 */
@Composable
fun LabelItem(
    text: String,
    modifier: Modifier = Modifier,
    @DrawableRes icon: Int? = null,
    style: LabelItemStyle = LabelItemDefaults.style,
    upperCase: Boolean = true,
) {
    if (text.isEmpty()) return

    LabelItem(
        modifier = modifier,
        text = {
            Text(
                text =
                    when {
                        upperCase -> text.toUpperCase(Locale.current)
                        else -> text
                    },
                style = style.textStyle.copy(color = style.contentColor),
            )
        },
        icon =
            icon.nullable<Int, @Composable () -> Unit> {
                {
                    Icon(
                        painter = painterResource(id = it),
                        contentDescription = text,
                        tint = style.contentColor,
                    )
                }
            },
        style = style,
    )
}

@Composable
fun KernelSuLabel() {
    LabelItem(
        icon = R.drawable.kernelsu_logo,
        text = "KernelSU",
    )
}

@Composable
fun KernelSuNextLabel() {
    LabelItem(
        icon = R.drawable.kernelsu_next_logo,
        text = "KernelSU Next",
    )
}

@Composable
fun MagiskLabel() {
    LabelItem(
        icon = R.drawable.magisk_logo,
        text = "KernelSU",
    )
}

@Composable
fun APatchLabel() {
    LabelItem(
        icon = R.drawable.brand_android,
        text = "APatch",
    )
}

@Composable
fun MMRLLabel() {
    LabelItem(
        icon = R.drawable.mmrl_logo,
        text = "MMRL",
    )
}

@Composable
fun SukiSU() {
    LabelItem(
        icon = R.drawable.sukisu_logo,
        text = "SukiSU Ultra",
    )
}

@Immutable
class LabelItemStyle(
    val containerColor: Color,
    val contentColor: Color,
    val shape: Shape,
    val textStyle: TextStyle,
) {
    @Suppress("RedundantIf")
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || other !is LabelItemStyle) return false

        if (containerColor != other.containerColor) return false
        if (contentColor != other.contentColor) return false
        if (shape != other.shape) return false
        if (textStyle != other.textStyle) return false

        return true
    }

    fun copy(
        containerColor: Color = this.containerColor,
        contentColor: Color = this.contentColor,
        shape: Shape = this.shape,
        textStyle: TextStyle = this.textStyle,
    ): LabelItemStyle =
        LabelItemStyle(
            containerColor,
            contentColor,
            shape,
            textStyle,
        )

    override fun hashCode(): Int {
        var result = containerColor.hashCode()
        result = 31 * result + contentColor.hashCode()
        result = 31 * result + shape.hashCode()
        result = 31 * result + textStyle.hashCode()
        return result
    }
}

object LabelItemDefaults {
    val style
        @Composable get() =
            LabelItemStyle(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(3.dp),
                textStyle = MaterialTheme.typography.labelSmall.copy(fontSize = 8.sp),
            )
}