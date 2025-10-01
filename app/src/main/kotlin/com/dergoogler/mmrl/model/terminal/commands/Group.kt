package com.dergoogler.mmrl.model.terminal.commands

import com.dergoogler.mmrl.model.terminal.GroupBlock
import com.dergoogler.mmrl.ui.activity.terminal.ActionCommand
import com.dergoogler.mmrl.ui.activity.terminal.Command
import com.dergoogler.mmrl.ui.activity.terminal.Terminal

class Group : Command {
    override val name: String = "group"

    override fun run(action: ActionCommand, terminal: Terminal) {
        with(terminal) {
            currentGroup = GroupBlock(
                title = action.data.ifBlank { action.getProp<String>("title") }?.fixNewLines,
                startLine = lineNumber,
                initiallyExpanded = false
            )
        }
    }
}

class EndGroup : Command {
    override val name: String = "endgroup"

    override fun run(action: ActionCommand, terminal: Terminal) {

        with(terminal) {
            currentGroup?.let {
                console += it
            }
            currentGroup = null
        }
    }
}