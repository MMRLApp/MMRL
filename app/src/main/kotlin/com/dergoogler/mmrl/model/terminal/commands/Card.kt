package com.dergoogler.mmrl.model.terminal.commands

import com.dergoogler.mmrl.model.terminal.CardBlock
import com.dergoogler.mmrl.ui.activity.terminal.ActionCommand
import com.dergoogler.mmrl.ui.activity.terminal.Command
import com.dergoogler.mmrl.ui.activity.terminal.Terminal

class Card : Command {
    override val name: String = "card"

    override fun run(action: ActionCommand, terminal: Terminal) {
        with(terminal) {
            currentCard = CardBlock()
        }
    }
}

class EndCard : Command {
    override val name: String = "endcard"

    override fun run(action: ActionCommand, terminal: Terminal) {
        with(terminal) {
            currentCard?.let {
                console += it
            }
            currentCard = null
        }
    }
}