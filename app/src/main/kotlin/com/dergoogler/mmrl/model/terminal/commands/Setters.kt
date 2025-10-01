package com.dergoogler.mmrl.model.terminal.commands

import com.dergoogler.mmrl.ui.activity.terminal.ActionCommand
import com.dergoogler.mmrl.ui.activity.terminal.Command
import com.dergoogler.mmrl.ui.activity.terminal.Terminal

class SetLines : Command {
    override val name: String = "set-lines"

    override fun run(action: ActionCommand, terminal: Terminal) {
        val enabled = action.getProp<Boolean>("enabled") ?: return
//        terminal.lineNumbersEnabled = enabled
    }
}