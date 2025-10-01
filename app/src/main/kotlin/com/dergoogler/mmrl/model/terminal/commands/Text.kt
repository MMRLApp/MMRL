package com.dergoogler.mmrl.model.terminal.commands

import com.dergoogler.mmrl.model.terminal.AlertBlock
import com.dergoogler.mmrl.model.terminal.AlertType
import com.dergoogler.mmrl.model.terminal.TextBlock
import com.dergoogler.mmrl.ui.activity.terminal.ActionCommand
import com.dergoogler.mmrl.ui.activity.terminal.Command
import com.dergoogler.mmrl.ui.activity.terminal.Terminal

class AddMask : Command {
    override val name: String = "add-mask"

    override fun run(action: ActionCommand, terminal: Terminal) {
        with(terminal) {
            var char = action.getProp<String>("char", "•")
            var flag = action.getProp<RegexOption>("flag", RegexOption.IGNORE_CASE)

            if (char.length != 1) {
                console += AlertBlock(
                    lineNumber = lineNumber,
                    type = AlertType.ERROR,
                    title = "Mask Error",
                    text = "Can't use a mask character that has a length of more or less than one characters."
                )

                char = "•"
            }

            action.data.takeIf { it.isNotBlank() }?.let {
                masks += Terminal.Mask(
                    char = char,
                    value = it,
                    flag = flag
                )
            }
        }
    }
}

class ReplaceSelf : Command {
    override val name: String = "replace-self"

    override fun run(action: ActionCommand, terminal: Terminal) {
        with(terminal) {
            val key = action.getProp<String>("key")
            if (key.isNullOrBlank()) return

            action.data.takeIf { it.isNotBlank() }?.let { data ->
                val index = console.indexOfFirst { it is TextBlock && it.key == key }

                if (index >= 0) {
                    val oldBlock = console[index] as TextBlock
                    val newBlock = TextBlock(oldBlock.lineNumber, data.fixNewLines, key = key)
                    if (oldBlock != newBlock) {
                        console[index] = newBlock
                    }
                    lineAdded = false // replaced existing line → no new line added
                } else {
                    console += TextBlock(lineNumber, data.fixNewLines, key = key)
                    lineAdded = true  // new line added
                }
            } ?: run {
                lineAdded = false // no data, no new line
            }
        }
    }
}

class RemoveLine : Command {
    override val name: String = "remove-line"

    override fun run(action: ActionCommand, terminal: Terminal) {
        with(terminal) {
            val key = action.getProp<String>("key")
            val data = action.data.takeIf { it.isNotBlank() }

            when {
                data != null -> {
                    val reg = Regex(data)
                    console.removeAll { it is TextBlock && it.text.matches(reg) }
                }
                !key.isNullOrBlank() -> {
                    console.removeAll { it is TextBlock && it.key == key }
                }
            }

            lineAdded = false
        }
    }
}