package com.dergoogler.mmrl.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ext.fadingEdge

@Composable
fun HorizontalDividerWithText(
    text: String,
    textStyle: TextStyle =
        MaterialTheme.typography.titleMedium.copy(
            color = MaterialTheme.colorScheme.outline,
        ),
    paddingValues: PaddingValues = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
    thickness: Dp = DividerDefaults.Thickness,
    color: Color = DividerDefaults.color,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier.padding(paddingValues),
    ) {
        HorizontalDivider(
            color = color,
            modifier =
                Modifier
                    .weight(1f)
                    .fadingEdge(
                        Brush.horizontalGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    Color.Black,
                                ),
                            startX = 0f,
                            endX = Float.POSITIVE_INFINITY,
                        ),
                    ),
            thickness = thickness,
        )

        Box(
            modifier = Modifier.padding(horizontal = 24.dp),
        ) {
            Text(
                text = text,
                style = textStyle,
            )
        }

        HorizontalDivider(
            color = color,
            modifier =
                Modifier
                    .weight(1f)
                    .fadingEdge(
                        Brush.horizontalGradient(
                            colors =
                                listOf(
                                    Color.Transparent,
                                    Color.Black,
                                ),
                            startX = Float.POSITIVE_INFINITY,
                            endX = 0f,
                        ),
                    ),
            thickness = thickness,
        )
    }
}
