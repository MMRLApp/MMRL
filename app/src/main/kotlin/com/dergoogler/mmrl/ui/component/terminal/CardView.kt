package com.dergoogler.mmrl.ui.component.terminal

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.dergoogler.mmrl.model.terminal.CardBlock
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.component.text.BBCodeText

@Composable
fun CardView(card: CardBlock) {
    Card {
        Column(
            modifier =
                Modifier
                    .relative(),
        ) {
            card.lines.forEach { (index, line) ->
                Line(index = index) {
                    BBCodeText(text = line)
                }
            }
        }
    }
}
