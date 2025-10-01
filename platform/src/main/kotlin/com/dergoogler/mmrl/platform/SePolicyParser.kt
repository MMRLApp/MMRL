@file:Suppress("unused", "UNCHECKED_CAST")

package com.dergoogler.mmrl.platform

import com.dergoogler.mmrl.platform.file.SuFile
import java.nio.file.Path

// Type aliases for clarity
typealias SeObject = List<String>

// Parser result wrapper
sealed class ParseResult<T> {
    data class Success<T>(val value: T, val remaining: String) : ParseResult<T>()
    data class Error<T>(val message: String) : ParseResult<T>()
}

// Utility functions for parsing
object ParserUtils {
    fun isSepolicyChar(c: Char): Boolean =
        c.isLetterOrDigit() || c == '_' || c == '-'

    fun parseWhitespace(input: String): ParseResult<Unit> {
        val trimmed = input.dropWhile { it.isWhitespace() }
        return ParseResult.Success(Unit, trimmed)
    }

    fun parseSingleWord(input: String): ParseResult<String> {
        val word = input.takeWhile { isSepolicyChar(it) }
        return if (word.isEmpty()) {
            ParseResult.Error("Expected word")
        } else {
            ParseResult.Success(word, input.drop(word.length))
        }
    }

    fun parseTag(tag: String, input: String): ParseResult<String> {
        return if (input.startsWith(tag)) {
            ParseResult.Success(tag, input.drop(tag.length))
        } else {
            ParseResult.Error("Expected tag: $tag")
        }
    }

    fun parseBracketObjs(input: String): ParseResult<SeObject> {
        if (!input.startsWith("{")) {
            return ParseResult.Error("Expected opening brace")
        }

        val afterBrace = input.drop(1)
        val closingBraceIndex = afterBrace.indexOf('}')
        if (closingBraceIndex == -1) {
            return ParseResult.Error("Expected closing brace")
        }

        val content = afterBrace.take(closingBraceIndex)
        val words = content.split(Regex("\\s+")).filter { it.isNotEmpty() }
        val remaining = afterBrace.drop(closingBraceIndex + 1)

        return ParseResult.Success(words, remaining)
    }

    fun parseSingleObj(input: String): ParseResult<SeObject> {
        return when (val result = parseSingleWord(input)) {
            is ParseResult.Success -> ParseResult.Success(listOf(result.value), result.remaining)
            is ParseResult.Error -> result
        } as ParseResult<SeObject>
    }

    fun parseStar(input: String): ParseResult<SeObject> {
        return if (input.startsWith("*")) {
            ParseResult.Success(listOf("*"), input.drop(1))
        } else {
            ParseResult.Error("Expected *")
        }
    }

    fun parseSeobj(input: String): ParseResult<SeObject> {
        val trimmed = parseWhitespace(input)
        if (trimmed !is ParseResult.Success) return ParseResult.Error("Failed to parse whitespace")

        return parseSingleObj(trimmed.remaining)
            .takeIf { it is ParseResult.Success }
            ?: parseBracketObjs(trimmed.remaining)
                .takeIf { it is ParseResult.Success }
            ?: parseStar(trimmed.remaining)
    }

    fun parseSeObjNoStar(input: String): ParseResult<SeObject> {
        val trimmed = parseWhitespace(input)
        if (trimmed !is ParseResult.Success) return ParseResult.Error("Failed to parse whitespace")

        return parseSingleObj(trimmed.remaining)
            .takeIf { it is ParseResult.Success }
            ?: parseBracketObjs(trimmed.remaining)
    }
}

// Data classes for policy statements
data class NormalPerm(
    val op: String,
    val source: SeObject,
    val target: SeObject,
    val objectClass: SeObject,
    val perm: SeObject,
) {
    companion object {
        fun parse(input: String): ParseResult<NormalPerm> {
            var remaining = input.trim()

            // Parse operation
            val opResult = listOf("allow", "deny", "auditallow", "dontaudit")
                .firstNotNullOfOrNull { op ->
                    if (remaining.startsWith(op)) {
                        ParseResult.Success(op, remaining.drop(op.length))
                    } else null
                } ?: return ParseResult.Error("Expected operation")

            remaining = ParserUtils.parseWhitespace(opResult.remaining).let {
                if (it is ParseResult.Success) it.remaining else return ParseResult.Error("Parse error")
            }

            // Parse source
            val sourceResult = ParserUtils.parseSeobj(remaining)
            if (sourceResult !is ParseResult.Success) return ParseResult.Error("Failed to parse source")
            remaining = ParserUtils.parseWhitespace(sourceResult.remaining).let {
                if (it is ParseResult.Success) it.remaining else return ParseResult.Error("Parse error")
            }

            // Parse target
            val targetResult = ParserUtils.parseSeobj(remaining)
            if (targetResult !is ParseResult.Success) return ParseResult.Error("Failed to parse target")
            remaining = ParserUtils.parseWhitespace(targetResult.remaining).let {
                if (it is ParseResult.Success) it.remaining else return ParseResult.Error("Parse error")
            }

            // Parse class
            val classResult = ParserUtils.parseSeobj(remaining)
            if (classResult !is ParseResult.Success) return ParseResult.Error("Failed to parse class")
            remaining = ParserUtils.parseWhitespace(classResult.remaining).let {
                if (it is ParseResult.Success) it.remaining else return ParseResult.Error("Parse error")
            }

            // Parse perm
            val permResult = ParserUtils.parseSeobj(remaining)
            if (permResult !is ParseResult.Success) return ParseResult.Error("Failed to parse perm")

            return ParseResult.Success(
                NormalPerm(
                    opResult.value,
                    sourceResult.value,
                    targetResult.value,
                    classResult.value,
                    permResult.value
                ),
                permResult.remaining
            )
        }
    }
}

data class XPerm(
    val op: String,
    val source: SeObject,
    val target: SeObject,
    val objectClass: SeObject,
    val operation: String,
    val permSet: String,
) {
    companion object {
        fun parse(input: String): ParseResult<XPerm> {
            var remaining = input.trim()

            // Parse operation
            val opResult = listOf("allowxperm", "auditallowxperm", "dontauditxperm")
                .firstNotNullOfOrNull { op ->
                    if (remaining.startsWith(op)) {
                        ParseResult.Success(op, remaining.drop(op.length))
                    } else null
                } ?: return ParseResult.Error("Expected xperm operation")

            remaining = ParserUtils.parseWhitespace(opResult.remaining).let {
                if (it is ParseResult.Success) it.remaining else return ParseResult.Error("Parse error")
            }

            // Parse source
            val sourceResult = ParserUtils.parseSeobj(remaining)
            if (sourceResult !is ParseResult.Success) return ParseResult.Error("Failed to parse source")
            remaining = ParserUtils.parseWhitespace(sourceResult.remaining).let {
                if (it is ParseResult.Success) it.remaining else return ParseResult.Error("Parse error")
            }

            // Parse target
            val targetResult = ParserUtils.parseSeobj(remaining)
            if (targetResult !is ParseResult.Success) return ParseResult.Error("Failed to parse target")
            remaining = ParserUtils.parseWhitespace(targetResult.remaining).let {
                if (it is ParseResult.Success) it.remaining else return ParseResult.Error("Parse error")
            }

            // Parse class
            val classResult = ParserUtils.parseSeobj(remaining)
            if (classResult !is ParseResult.Success) return ParseResult.Error("Failed to parse class")
            remaining = ParserUtils.parseWhitespace(classResult.remaining).let {
                if (it is ParseResult.Success) it.remaining else return ParseResult.Error("Parse error")
            }

            // Parse operation
            val operationResult = ParserUtils.parseSingleWord(remaining)
            if (operationResult !is ParseResult.Success) return ParseResult.Error("Failed to parse operation")
            remaining = ParserUtils.parseWhitespace(operationResult.remaining).let {
                if (it is ParseResult.Success) it.remaining else return ParseResult.Error("Parse error")
            }

            // Parse perm set
            val permSetResult = ParserUtils.parseSingleWord(remaining)
            if (permSetResult !is ParseResult.Success) return ParseResult.Error("Failed to parse perm set")

            return ParseResult.Success(
                XPerm(
                    opResult.value,
                    sourceResult.value,
                    targetResult.value,
                    classResult.value,
                    operationResult.value,
                    permSetResult.value
                ),
                permSetResult.remaining
            )
        }
    }
}

data class TypeState(
    val op: String,
    val stype: SeObject,
) {
    companion object {
        fun parse(input: String): ParseResult<TypeState> {
            var remaining = input.trim()

            val opResult = listOf("permissive", "enforce")
                .firstNotNullOfOrNull { op ->
                    if (remaining.startsWith(op)) {
                        ParseResult.Success(op, remaining.drop(op.length))
                    } else null
                } ?: return ParseResult.Error("Expected type state operation")

            remaining = remaining.dropWhile { it.isWhitespace() }
            if (remaining.isEmpty()) return ParseResult.Error("Expected type after operation")

            val typeResult = ParserUtils.parseSeObjNoStar(remaining)
            if (typeResult !is ParseResult.Success) return ParseResult.Error("Failed to parse type")

            return ParseResult.Success(
                TypeState(opResult.value, typeResult.value),
                typeResult.remaining
            )
        }
    }
}

data class TypeDef(
    val name: String,
    val attrs: SeObject,
) {
    companion object {
        fun parse(input: String): ParseResult<TypeDef> {
            var remaining = input.trim()

            if (!remaining.startsWith("type")) {
                return ParseResult.Error("Expected 'type'")
            }
            remaining = remaining.drop(4).dropWhile { it.isWhitespace() }

            val nameResult = ParserUtils.parseSingleWord(remaining)
            if (nameResult !is ParseResult.Success) return ParseResult.Error("Failed to parse type name")

            remaining = nameResult.remaining.dropWhile { it.isWhitespace() }

            if (remaining.isEmpty()) {
                return ParseResult.Success(
                    TypeDef(nameResult.value, listOf("domain")),
                    remaining
                )
            }

            val attrsResult = ParserUtils.parseSeObjNoStar(remaining)
            if (attrsResult !is ParseResult.Success) return ParseResult.Error("Failed to parse attributes")

            return ParseResult.Success(
                TypeDef(nameResult.value, attrsResult.value),
                attrsResult.remaining
            )
        }
    }
}

data class TypeAttr(
    val stype: SeObject,
    val sattr: SeObject,
) {
    companion object {
        fun parse(input: String): ParseResult<TypeAttr> {
            var remaining = input.trim()

            val opResult = listOf("typeattribute", "attradd")
                .firstNotNullOfOrNull { op ->
                    if (remaining.startsWith(op)) {
                        ParseResult.Success(op, remaining.drop(op.length))
                    } else null
                } ?: return ParseResult.Error("Expected typeattribute operation")

            remaining = remaining.dropWhile { it.isWhitespace() }

            val typeResult = ParserUtils.parseSeObjNoStar(remaining)
            if (typeResult !is ParseResult.Success) return ParseResult.Error("Failed to parse type")
            remaining = typeResult.remaining.dropWhile { it.isWhitespace() }

            val attrResult = ParserUtils.parseSeObjNoStar(remaining)
            if (attrResult !is ParseResult.Success) return ParseResult.Error("Failed to parse attribute")

            return ParseResult.Success(
                TypeAttr(typeResult.value, attrResult.value),
                attrResult.remaining
            )
        }
    }
}

data class Attr(
    val name: String,
) {
    companion object {
        fun parse(input: String): ParseResult<Attr> {
            var remaining = input.trim()

            if (!remaining.startsWith("attribute")) {
                return ParseResult.Error("Expected 'attribute'")
            }
            remaining = remaining.drop(9).dropWhile { it.isWhitespace() }

            val attrResult = ParserUtils.parseSingleWord(remaining)
            if (attrResult !is ParseResult.Success) return ParseResult.Error("Failed to parse attribute name")

            return ParseResult.Success(
                Attr(attrResult.value),
                attrResult.remaining
            )
        }
    }
}

data class TypeTransition(
    val source: String,
    val target: String,
    val objectClass: String,
    val defaultType: String,
    val objectName: String?,
) {
    companion object {
        fun parse(input: String): ParseResult<TypeTransition> {
            var remaining = input.trim()

            val opResult = listOf("type_transition", "name_transition")
                .firstNotNullOfOrNull { op ->
                    if (remaining.startsWith(op)) {
                        ParseResult.Success(op, remaining.drop(op.length))
                    } else null
                } ?: return ParseResult.Error("Expected transition operation")

            remaining = remaining.dropWhile { it.isWhitespace() }

            val sourceResult = ParserUtils.parseSingleWord(remaining)
            if (sourceResult !is ParseResult.Success) return ParseResult.Error("Failed to parse source")
            remaining = sourceResult.remaining.dropWhile { it.isWhitespace() }

            val targetResult = ParserUtils.parseSingleWord(remaining)
            if (targetResult !is ParseResult.Success) return ParseResult.Error("Failed to parse target")
            remaining = targetResult.remaining.dropWhile { it.isWhitespace() }

            val classResult = ParserUtils.parseSingleWord(remaining)
            if (classResult !is ParseResult.Success) return ParseResult.Error("Failed to parse class")
            remaining = classResult.remaining.dropWhile { it.isWhitespace() }

            val defaultResult = ParserUtils.parseSingleWord(remaining)
            if (defaultResult !is ParseResult.Success) return ParseResult.Error("Failed to parse default type")
            remaining = defaultResult.remaining.dropWhile { it.isWhitespace() }

            val objectName = if (remaining.isNotEmpty()) {
                val objectResult = ParserUtils.parseSingleWord(remaining)
                if (objectResult is ParseResult.Success) {
                    remaining = objectResult.remaining
                    objectResult.value
                } else null
            } else null

            return ParseResult.Success(
                TypeTransition(
                    sourceResult.value,
                    targetResult.value,
                    classResult.value,
                    defaultResult.value,
                    objectName
                ),
                remaining
            )
        }
    }
}

data class TypeChange(
    val op: String,
    val source: String,
    val target: String,
    val objectClass: String,
    val defaultType: String,
) {
    companion object {
        fun parse(input: String): ParseResult<TypeChange> {
            var remaining = input.trim()

            val opResult = listOf("type_change", "type_member")
                .firstNotNullOfOrNull { op ->
                    if (remaining.startsWith(op)) {
                        ParseResult.Success(op, remaining.drop(op.length))
                    } else null
                } ?: return ParseResult.Error("Expected type change operation")

            remaining = remaining.dropWhile { it.isWhitespace() }

            val sourceResult = ParserUtils.parseSingleWord(remaining)
            if (sourceResult !is ParseResult.Success) return ParseResult.Error("Failed to parse source")
            remaining = sourceResult.remaining.dropWhile { it.isWhitespace() }

            val targetResult = ParserUtils.parseSingleWord(remaining)
            if (targetResult !is ParseResult.Success) return ParseResult.Error("Failed to parse target")
            remaining = targetResult.remaining.dropWhile { it.isWhitespace() }

            val classResult = ParserUtils.parseSingleWord(remaining)
            if (classResult !is ParseResult.Success) return ParseResult.Error("Failed to parse class")
            remaining = classResult.remaining.dropWhile { it.isWhitespace() }

            val defaultResult = ParserUtils.parseSingleWord(remaining)
            if (defaultResult !is ParseResult.Success) return ParseResult.Error("Failed to parse default type")

            return ParseResult.Success(
                TypeChange(
                    opResult.value,
                    sourceResult.value,
                    targetResult.value,
                    classResult.value,
                    defaultResult.value
                ),
                defaultResult.remaining
            )
        }
    }
}

data class GenFsCon(
    val fsName: String,
    val partialPath: String,
    val fsContext: String,
) {
    companion object {
        fun parse(input: String): ParseResult<GenFsCon> {
            var remaining = input.trim()

            if (!remaining.startsWith("genfscon")) {
                return ParseResult.Error("Expected 'genfscon'")
            }
            remaining = remaining.drop(8).dropWhile { it.isWhitespace() }

            val fsResult = ParserUtils.parseSingleWord(remaining)
            if (fsResult !is ParseResult.Success) return ParseResult.Error("Failed to parse fs name")
            remaining = fsResult.remaining.dropWhile { it.isWhitespace() }

            val pathResult = ParserUtils.parseSingleWord(remaining)
            if (pathResult !is ParseResult.Success) return ParseResult.Error("Failed to parse path")
            remaining = pathResult.remaining.dropWhile { it.isWhitespace() }

            val contextResult = ParserUtils.parseSingleWord(remaining)
            if (contextResult !is ParseResult.Success) return ParseResult.Error("Failed to parse context")

            return ParseResult.Success(
                GenFsCon(fsResult.value, pathResult.value, contextResult.value),
                contextResult.remaining
            )
        }
    }
}

// Main policy statement enum
sealed class PolicyStatement {
    data class NormalPermission(val perm: NormalPerm) : PolicyStatement()
    data class XPermission(val perm: XPerm) : PolicyStatement()
    data class TypeStateStmt(val state: TypeState) : PolicyStatement()
    data class TypeDefStmt(val typeDef: TypeDef) : PolicyStatement()
    data class TypeAttrStmt(val attr: TypeAttr) : PolicyStatement()
    data class AttrStmt(val attr: Attr) : PolicyStatement()
    data class TypeTransitionStmt(val transition: TypeTransition) : PolicyStatement()
    data class TypeChangeStmt(val change: TypeChange) : PolicyStatement()
    data class GenFsConStmt(val genfs: GenFsCon) : PolicyStatement()

    companion object {
        fun parse(input: String): ParseResult<PolicyStatement> {
            val trimmed = input.trim()

            // Try each parser in order
            val parsers = listOf(
                { s: String ->
                    NormalPerm.parse(s).let {
                        if (it is ParseResult.Success) ParseResult.Success(
                            NormalPermission(it.value),
                            it.remaining
                        ) else it as ParseResult<PolicyStatement>
                    }
                },
                { s: String ->
                    XPerm.parse(s).let {
                        if (it is ParseResult.Success) ParseResult.Success(
                            XPermission(it.value),
                            it.remaining
                        ) else it as ParseResult<PolicyStatement>
                    }
                },
                { s: String ->
                    TypeState.parse(s).let {
                        if (it is ParseResult.Success) ParseResult.Success(
                            TypeStateStmt(it.value),
                            it.remaining
                        ) else it as ParseResult<PolicyStatement>
                    }
                },
                { s: String ->
                    TypeDef.parse(s).let {
                        if (it is ParseResult.Success) ParseResult.Success(
                            TypeDefStmt(it.value),
                            it.remaining
                        ) else it as ParseResult<PolicyStatement>
                    }
                },
                { s: String ->
                    TypeAttr.parse(s).let {
                        if (it is ParseResult.Success) ParseResult.Success(
                            TypeAttrStmt(it.value),
                            it.remaining
                        ) else it as ParseResult<PolicyStatement>
                    }
                },
                { s: String ->
                    Attr.parse(s).let {
                        if (it is ParseResult.Success) ParseResult.Success(
                            AttrStmt(it.value),
                            it.remaining
                        ) else it as ParseResult<PolicyStatement>
                    }
                },
                { s: String ->
                    TypeTransition.parse(s).let {
                        if (it is ParseResult.Success) ParseResult.Success(
                            TypeTransitionStmt(it.value),
                            it.remaining
                        ) else it as ParseResult<PolicyStatement>
                    }
                },
                { s: String ->
                    TypeChange.parse(s).let {
                        if (it is ParseResult.Success) ParseResult.Success(
                            TypeChangeStmt(it.value),
                            it.remaining
                        ) else it as ParseResult<PolicyStatement>
                    }
                },
                { s: String ->
                    GenFsCon.parse(s).let {
                        if (it is ParseResult.Success) ParseResult.Success(
                            GenFsConStmt(it.value),
                            it.remaining
                        ) else it as ParseResult<PolicyStatement>
                    }
                }
            )

            for (parser in parsers) {
                val result = parser(trimmed)
                if (result is ParseResult.Success) {
                    // Handle optional semicolon
                    var remaining = result.remaining.trim()
                    while (remaining.startsWith(";")) {
                        remaining = remaining.drop(1).trim()
                    }
                    return ParseResult.Success(result.value, remaining)
                }
            }

            return ParseResult.Error("No matching parser found for: $trimmed")
        }
    }
}

// Constants for command types
object PolicyCommands {
    const val CMD_NORMAL_PERM = 1u
    const val CMD_XPERM = 2u
    const val CMD_TYPE_STATE = 3u
    const val CMD_TYPE = 4u
    const val CMD_TYPE_ATTR = 5u
    const val CMD_ATTR = 6u
    const val CMD_TYPE_TRANSITION = 7u
    const val CMD_TYPE_CHANGE = 8u
    const val CMD_GENFSCON = 9u
    const val SEPOLICY_MAX_LEN = 128
}

// Policy object representation
sealed class PolicyObject {
    object All : PolicyObject() // for "*"
    data class One(val value: ByteArray) : PolicyObject() {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            other as One
            return value.contentEquals(other.value)
        }

        override fun hashCode(): Int = value.contentHashCode()
    }

    object None : PolicyObject()

    companion object {
        fun fromString(s: String): PolicyObject {
            if (s.length > PolicyCommands.SEPOLICY_MAX_LEN) {
                throw IllegalArgumentException("Policy object too long")
            }
            if (s == "*") return All

            val buf = ByteArray(PolicyCommands.SEPOLICY_MAX_LEN)
            s.toByteArray().copyInto(buf, 0, 0, s.length)
            return One(buf)
        }


    }
}

// Atomic statement for FFI
data class AtomicStatement(
    val cmd: UInt,
    val subcmd: UInt,
    val sepol1: PolicyObject,
    val sepol2: PolicyObject,
    val sepol3: PolicyObject,
    val sepol4: PolicyObject,
    val sepol5: PolicyObject,
    val sepol6: PolicyObject,
    val sepol7: PolicyObject,
)

// Main parser class
class SePolicyParser {
    companion object {
        fun parseSepolicy(input: String, strict: Boolean = false): Result<List<PolicyStatement>> {
            val statements = mutableListOf<PolicyStatement>()

            for (line in input.split('\n', ';')) {
                val trimmedLine = line.trim()
                if (trimmedLine.isEmpty() || trimmedLine.startsWith('#')) {
                    continue
                }

                when (val result = PolicyStatement.parse(trimmedLine)) {
                    is ParseResult.Success -> statements.add(result.value)
                    is ParseResult.Error -> {
                        if (strict) {
                            return Result.failure(Exception("Failed to parse policy statement: $line - ${result.message}"))
                        }
                    }
                }
            }

            return Result.success(statements)
        }

        fun checkRule(policy: String): Result<Unit> {
            val path = SuFile(policy)
            val policyContent = if (path.exists()) {
                path.readText()
            } else {
                policy
            }

            return parseSepolicy(policyContent.trim(), true).map { }
        }

        fun applyFile(path: Path): Result<Unit> {
            val input = SuFile(path.toString()).readText()
            return livePatch(input)
        }

        fun livePatch(policy: String): Result<Unit> {
            return parseSepolicy(policy.trim(), false).fold(
                onSuccess = { statements ->
                    for (statement in statements) {
                        println(statement)
                        // Apply rule logic would go here
                        // applyOneRule(statement, false)
                    }
                    Result.success(Unit)
                },
                onFailure = { Result.failure(it) }
            )
        }
    }
}

// Extension functions for easier conversion to atomic statements
fun PolicyStatement.toAtomicStatements(): List<AtomicStatement> {
    return when (this) {
        is PolicyStatement.NormalPermission -> perm.toAtomicStatements()
        is PolicyStatement.XPermission -> perm.toAtomicStatements()
        is PolicyStatement.TypeStateStmt -> state.toAtomicStatements()
        is PolicyStatement.TypeDefStmt -> typeDef.toAtomicStatements()
        is PolicyStatement.TypeAttrStmt -> attr.toAtomicStatements()
        is PolicyStatement.AttrStmt -> attr.toAtomicStatements()
        is PolicyStatement.TypeTransitionStmt -> transition.toAtomicStatements()
        is PolicyStatement.TypeChangeStmt -> change.toAtomicStatements()
        is PolicyStatement.GenFsConStmt -> genfs.toAtomicStatements()
    }
}

fun NormalPerm.toAtomicStatements(): List<AtomicStatement> {
    val result = mutableListOf<AtomicStatement>()
    val subcmd = when (op) {
        "allow" -> 1u
        "deny" -> 2u
        "auditallow" -> 3u
        "dontaudit" -> 4u
        else -> 0u
    }

    for (s in source) {
        for (t in target) {
            for (c in objectClass) {
                for (p in perm) {
                    result.add(
                        AtomicStatement(
                            cmd = PolicyCommands.CMD_NORMAL_PERM,
                            subcmd = subcmd,
                            sepol1 = PolicyObject.fromString(s),
                            sepol2 = PolicyObject.fromString(t),
                            sepol3 = PolicyObject.fromString(c),
                            sepol4 = PolicyObject.fromString(p),
                            sepol5 = PolicyObject.None,
                            sepol6 = PolicyObject.None,
                            sepol7 = PolicyObject.None
                        )
                    )
                }
            }
        }
    }
    return result
}

fun XPerm.toAtomicStatements(): List<AtomicStatement> {
    val result = mutableListOf<AtomicStatement>()
    val subcmd = when (op) {
        "allowxperm" -> 1u
        "auditallowxperm" -> 2u
        "dontauditxperm" -> 3u
        else -> 0u
    }

    for (s in source) {
        for (t in target) {
            for (c in objectClass) {
                result.add(
                    AtomicStatement(
                        cmd = PolicyCommands.CMD_XPERM,
                        subcmd = subcmd,
                        sepol1 = PolicyObject.fromString(s),
                        sepol2 = PolicyObject.fromString(t),
                        sepol3 = PolicyObject.fromString(c),
                        sepol4 = PolicyObject.fromString(operation),
                        sepol5 = PolicyObject.fromString(permSet),
                        sepol6 = PolicyObject.None,
                        sepol7 = PolicyObject.None
                    )
                )
            }
        }
    }
    return result
}

fun TypeState.toAtomicStatements(): List<AtomicStatement> {
    val result = mutableListOf<AtomicStatement>()
    val subcmd = when (op) {
        "permissive" -> 1u
        "enforcing" -> 2u
        else -> 0u
    }

    for (t in stype) {
        result.add(
            AtomicStatement(
                cmd = PolicyCommands.CMD_TYPE_STATE,
                subcmd = subcmd,
                sepol1 = PolicyObject.fromString(t),
                sepol2 = PolicyObject.None,
                sepol3 = PolicyObject.None,
                sepol4 = PolicyObject.None,
                sepol5 = PolicyObject.None,
                sepol6 = PolicyObject.None,
                sepol7 = PolicyObject.None
            )
        )
    }
    return result
}

fun TypeDef.toAtomicStatements(): List<AtomicStatement> {
    val result = mutableListOf<AtomicStatement>()
    for (attr in attrs) {
        result.add(
            AtomicStatement(
                cmd = PolicyCommands.CMD_TYPE,
                subcmd = 0u,
                sepol1 = PolicyObject.fromString(name),
                sepol2 = PolicyObject.fromString(attr),
                sepol3 = PolicyObject.None,
                sepol4 = PolicyObject.None,
                sepol5 = PolicyObject.None,
                sepol6 = PolicyObject.None,
                sepol7 = PolicyObject.None
            )
        )
    }
    return result
}

fun TypeAttr.toAtomicStatements(): List<AtomicStatement> {
    val result = mutableListOf<AtomicStatement>()
    for (t in stype) {
        for (attr in sattr) {
            result.add(
                AtomicStatement(
                    cmd = PolicyCommands.CMD_TYPE_ATTR,
                    subcmd = 0u,
                    sepol1 = PolicyObject.fromString(t),
                    sepol2 = PolicyObject.fromString(attr),
                    sepol3 = PolicyObject.None,
                    sepol4 = PolicyObject.None,
                    sepol5 = PolicyObject.None,
                    sepol6 = PolicyObject.None,
                    sepol7 = PolicyObject.None
                )
            )
        }
    }
    return result
}

fun Attr.toAtomicStatements(): List<AtomicStatement> {
    return listOf(
        AtomicStatement(
            cmd = PolicyCommands.CMD_ATTR,
            subcmd = 0u,
            sepol1 = PolicyObject.fromString(name),
            sepol2 = PolicyObject.None,
            sepol3 = PolicyObject.None,
            sepol4 = PolicyObject.None,
            sepol5 = PolicyObject.None,
            sepol6 = PolicyObject.None,
            sepol7 = PolicyObject.None
        )
    )
}

fun TypeTransition.toAtomicStatements(): List<AtomicStatement> {
    val obj = objectName?.let { PolicyObject.fromString(it) } ?: PolicyObject.None
    return listOf(
        AtomicStatement(
            cmd = PolicyCommands.CMD_TYPE_TRANSITION,
            subcmd = 0u,
            sepol1 = PolicyObject.fromString(source),
            sepol2 = PolicyObject.fromString(target),
            sepol3 = PolicyObject.fromString(objectClass),
            sepol4 = PolicyObject.fromString(defaultType),
            sepol5 = obj,
            sepol6 = PolicyObject.None,
            sepol7 = PolicyObject.None
        )
    )
}

fun TypeChange.toAtomicStatements(): List<AtomicStatement> {
    val subcmd = when (op) {
        "type_change" -> 1u
        "type_member" -> 2u
        else -> 0u
    }
    return listOf(
        AtomicStatement(
            cmd = PolicyCommands.CMD_TYPE_CHANGE,
            subcmd = subcmd,
            sepol1 = PolicyObject.fromString(source),
            sepol2 = PolicyObject.fromString(target),
            sepol3 = PolicyObject.fromString(objectClass),
            sepol4 = PolicyObject.fromString(defaultType),
            sepol5 = PolicyObject.None,
            sepol6 = PolicyObject.None,
            sepol7 = PolicyObject.None
        )
    )
}

fun GenFsCon.toAtomicStatements(): List<AtomicStatement> {
    return listOf(
        AtomicStatement(
            cmd = PolicyCommands.CMD_GENFSCON,
            subcmd = 0u,
            sepol1 = PolicyObject.fromString(fsName),
            sepol2 = PolicyObject.fromString(partialPath),
            sepol3 = PolicyObject.fromString(fsContext),
            sepol4 = PolicyObject.None,
            sepol5 = PolicyObject.None,
            sepol6 = PolicyObject.None,
            sepol7 = PolicyObject.None
        )
    )
}

// FFI Policy structure for C interop
data class FfiPolicy(
    val cmd: UInt,
    val subcmd: UInt,
    val sepol1: ByteArray?,
    val sepol2: ByteArray?,
    val sepol3: ByteArray?,
    val sepol4: ByteArray?,
    val sepol5: ByteArray?,
    val sepol6: ByteArray?,
    val sepol7: ByteArray?,
) {
    companion object {
        fun fromAtomicStatement(statement: AtomicStatement): FfiPolicy {
            fun toCPtr(pol: PolicyObject): ByteArray? = when (pol) {
                PolicyObject.None, PolicyObject.All -> null
                is PolicyObject.One -> pol.value
            }

            return FfiPolicy(
                cmd = statement.cmd,
                subcmd = statement.subcmd,
                sepol1 = toCPtr(statement.sepol1),
                sepol2 = toCPtr(statement.sepol2),
                sepol3 = toCPtr(statement.sepol3),
                sepol4 = toCPtr(statement.sepol4),
                sepol5 = toCPtr(statement.sepol5),
                sepol6 = toCPtr(statement.sepol6),
                sepol7 = toCPtr(statement.sepol7)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FfiPolicy

        if (cmd != other.cmd) return false
        if (subcmd != other.subcmd) return false
        if (!sepol1.contentEquals(other.sepol1)) return false
        if (!sepol2.contentEquals(other.sepol2)) return false
        if (!sepol3.contentEquals(other.sepol3)) return false
        if (!sepol4.contentEquals(other.sepol4)) return false
        if (!sepol5.contentEquals(other.sepol5)) return false
        if (!sepol6.contentEquals(other.sepol6)) return false
        if (!sepol7.contentEquals(other.sepol7)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cmd.hashCode()
        result = 31 * result + subcmd.hashCode()
        result = 31 * result + (sepol1?.contentHashCode() ?: 0)
        result = 31 * result + (sepol2?.contentHashCode() ?: 0)
        result = 31 * result + (sepol3?.contentHashCode() ?: 0)
        result = 31 * result + (sepol4?.contentHashCode() ?: 0)
        result = 31 * result + (sepol5?.contentHashCode() ?: 0)
        result = 31 * result + (sepol6?.contentHashCode() ?: 0)
        result = 31 * result + (sepol7?.contentHashCode() ?: 0)
        return result
    }
}
