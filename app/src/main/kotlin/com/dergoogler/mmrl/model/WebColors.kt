package com.dergoogler.mmrl.model

import androidx.compose.material3.ButtonColors
import androidx.compose.material3.CardColors
import androidx.compose.material3.ColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.toCssValue
import com.dergoogler.mmrl.ui.component.button.defaultFilledTonalButtonColors
import com.dergoogler.mmrl.ui.component.card.defaultCardColors
import com.dergoogler.mmrl.ui.token.surfaceColorAtElevation
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

data class WebColors(val colorScheme: ColorScheme) {
    val filledTonalButtonColors = colorScheme.defaultFilledTonalButtonColors
    val cardColors = colorScheme.defaultCardColors

    val colorSchemeMap: Map<String, Color> =
        (colorScheme::class as KClass<ColorScheme>).memberProperties
            .filter { it.returnType.classifier == Color::class }
            .associateBy({ it.name }, { it.get(colorScheme) as Color })

    val filledTonalButtonColorsMap =
        (filledTonalButtonColors::class as KClass<ButtonColors>).memberProperties
            .filter { it.returnType.classifier == Color::class }
            .associateBy({ it.name }, { it.get(filledTonalButtonColors) as Color })

    val cardColorsMap = (cardColors::class as KClass<CardColors>).memberProperties
        .filter { it.returnType.classifier == Color::class }
        .associateBy({ it.name }, { it.get(cardColors) as Color })

    val allCssColors = buildString {
        appendLine(":root {")

        appendTabbedLine("/* App Base Colors */")
        colorSchemeMap.forEach { (name, color) ->
            appendTabbedLine("--$name: ${color.toCssValue()};")

            // Manual inject this because it not a default
            if (name == "surface") {
                appendTabbedLine(
                    "--tonalSurface: ${
                        colorScheme.surfaceColorAtElevation(1.dp).toCssValue()
                    };"
                )
            }
        }

        appendTabbedLine("/* Filled Tonal Button Colors */")
        filledTonalButtonColorsMap.forEach { (name, color) ->
            appendTabbedLine("--filledTonalButton${name.capitalize()}: ${color.toCssValue()};")
        }

        appendTabbedLine("/* Filled Card Colors */")
        cardColorsMap.forEach { (name, color) ->
            appendTabbedLine("--filledCard${name.capitalize()}: ${color.toCssValue()};")
        }

        appendLine("}")
    }

    fun String.capitalize() =
        this.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

    fun StringBuilder.appendTabbedLine(line: String, count: Int = 1) {
        val tabs = "\t".repeat(count)
        appendLine("$tabs$line")
    }
}