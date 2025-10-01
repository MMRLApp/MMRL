package com.dergoogler.mmrl.ui.activity.terminal

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.dergoogler.mmrl.app.Event
import com.dergoogler.mmrl.model.terminal.Block
import com.dergoogler.mmrl.model.terminal.CardBlock
import com.dergoogler.mmrl.model.terminal.GroupBlock
import com.dergoogler.mmrl.utils.createRootShell

interface Command {
    val name: String
    fun run(
        action: ActionCommand,
        terminal: Terminal,
    )
}

@Immutable
data class ParsedCommand(
    val command: Command,
    val action: ActionCommand,
)

class Terminal {
    @Immutable
    data class Mask(
        val char: String = "•",
        val value: String,
        val flag: RegexOption = RegexOption.IGNORE_CASE,
    )

    val logs = mutableListOf<String>()
    val console = mutableStateListOf<Block>()
    val shell by mutableStateOf(createRootShell())
    var currentGroup: GroupBlock? = null
    var currentCard: CardBlock? = null
    // var lineNumbersEnabled: Boolean by mutableStateOf(true)
    var lineNumber = 1
    var lineAdded: Boolean by mutableStateOf(true)
    val masks = mutableListOf<Mask>()
    var event by mutableStateOf(Event.LOADING)

    val String.applyMasks
        get(): String {
            var maskedString = this

            for (mask in masks) {
                if (mask.value.isEmpty()) continue

                val reg = Regex(mask.value, mask.flag)

                maskedString =
                    maskedString.replace(reg, mask.char.repeat(8))
            }

            return maskedString
        }

    val String.fixNewLines
        get(): String =
            this.replace("""\\n""".toRegex(), "\n").replace(Regex("\r\n|\r|\n"), "\n")
}

class ActionCommand private constructor(val command: String) {
    val properties = mutableMapOf<String, String>()
    var data: String = ""

    inline fun <reified T> getProp(key: String): T? {
        val value = properties[key]
        return value as? T
    }

    inline fun <reified T> getProp(key: String, def: T): T {
        val value = properties[key]
        return if (value is T) {
            value
        } else {
            def
        }
    }

    companion object {
        private const val COMMAND_KEY = "::"

        private val escapeDataMappings = listOf(
            EscapeMapping("\r", "%0D"),
            EscapeMapping("\n", "%0A"),
            EscapeMapping("%", "%25")
        )

        private val escapePropertyMappings = listOf(
            EscapeMapping("\r", "%0D"),
            EscapeMapping("\n", "%0A"),
            EscapeMapping(":", "%3A"),
            EscapeMapping(",", "%2C"),
            EscapeMapping("%", "%25")
        )

        fun tryParseV2(message: String?, registeredCommands: List<Command>): ParsedCommand? {
            if (message.isNullOrBlank()) return null

            try {
                val trimmed = message.trimStart()
                if (!trimmed.startsWith(COMMAND_KEY)) return null

                val endIndex = trimmed.indexOf(COMMAND_KEY, COMMAND_KEY.length)
                if (endIndex < 0) return null

                val cmdInfo = trimmed.substring(COMMAND_KEY.length, endIndex)
                val spaceIndex = cmdInfo.indexOf(' ')
                val commandName = if (spaceIndex < 0) cmdInfo else cmdInfo.substring(0, spaceIndex)

                val matched = registeredCommands.find { it.name == commandName } ?: return null

                val cmd = ActionCommand(commandName)

                if (spaceIndex > 0) {
                    val propertiesStr = cmdInfo.substring(spaceIndex + 1).trim()
                    val splitProperties = propertiesStr.split(",").filter { it.isNotEmpty() }
                    for (propertyStr in splitProperties) {
                        val pair = propertyStr.split("=", limit = 2)
                        if (pair.size == 2) {
                            cmd.properties[pair[0]] = unescapeProperty(pair[1])
                        }
                    }
                }

                cmd.data = unescapeData(trimmed.substring(endIndex + COMMAND_KEY.length))

                return ParsedCommand(matched, cmd)
            } catch (_: Exception) {
                return null
            }
        }

        fun tryParseV2AndRun(
            message: String?,
            terminal: Terminal,
            registeredCommands: List<Command>,
        ): Boolean {
            val parsed = tryParseV2(message, registeredCommands) ?: return false
            parsed.command.run(
                action = parsed.action,
                terminal = terminal
            )
            return true
        }

        private fun unescapeProperty(escaped: String?): String {
            var unescaped = escaped ?: return ""
            for (mapping in escapePropertyMappings) {
                unescaped = unescaped.replace(mapping.replacement, mapping.token)
            }
            return unescaped
        }

        private fun unescapeData(escaped: String?): String {
            var unescaped = escaped ?: return ""
            for (mapping in escapeDataMappings) {
                unescaped = unescaped.replace(mapping.replacement, mapping.token)
            }
            return unescaped
        }

        private data class EscapeMapping(val token: String, val replacement: String)
    }
}
