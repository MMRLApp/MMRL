package com.dergoogler.mmrl.model.terminal.commands

import com.dergoogler.mmrl.model.terminal.AlertBlock
import com.dergoogler.mmrl.model.terminal.AlertType
import com.dergoogler.mmrl.ui.activity.terminal.ActionCommand
import com.dergoogler.mmrl.ui.activity.terminal.Command
import com.dergoogler.mmrl.ui.activity.terminal.Terminal

class Notice : Alerts(AlertType.NOTICE) {
    override val name = "notice"
}

class Warning : Alerts(AlertType.WARNING) {
    override val name = "warning"
}

class Error : Alerts(AlertType.ERROR) {
    override val name = "error"
}

abstract class Alerts(
    private val type: AlertType,
) : Command {
    override fun run(
        action: ActionCommand,
        terminal: Terminal,
    ) {
        with(terminal) {
            val title = action.getProp<String>("title")

            action.data.takeIf { it.isNotBlank() }?.let {
                console += AlertBlock(
                    lineNumber = lineNumber,
                    type = type,
                    title = title,
                    text = it.fixNewLines.applyMasks
                )
            }
        }
    }
}