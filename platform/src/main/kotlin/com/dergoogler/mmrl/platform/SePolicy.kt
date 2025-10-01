package com.dergoogler.mmrl.platform

import android.util.Log
import com.dergoogler.mmrl.platform.file.SuFile
import com.dergoogler.mmrl.platform.ksu.KsuNative
import java.io.IOException

object SePolicy {
    private const val TAG = "SePolicy"

    /**
     * Validates SEPolicy rules using the built-in parser instead of shell commands
     */
    fun isSepolicyValid(rules: String?): Boolean {
        if (rules == null) {
            return true
        }

        return try {
            val result = SePolicyParser.parseSepolicy(rules, strict = true)
            result.isSuccess
        } catch (e: Exception) {
            Log.w(TAG, "SEPolicy validation failed: ${e.message}")
            false
        }
    }

    fun getSepolicy(pkg: String): String {
        return try {
            val sepolicyFile = SuFile("/data/adb/ksu/profile/selinux/$pkg")
            if (sepolicyFile.exists()) {
                val content = sepolicyFile.readText()
                Log.i(TAG, "Retrieved sepolicy for $pkg: ${content.length} chars")
                return content
            }

            Log.i(TAG, "No sepolicy found for package: $pkg")
            ""
        } catch (e: IOException) {
            Log.e(TAG, "Failed to read sepolicy for $pkg: ${e.message}")
            ""
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error getting sepolicy for $pkg: ${e.message}")
            ""
        }
    }

    fun setSePolicy(pkg: String, rules: String): Boolean {
        return try {
            if (!isSepolicyValid(rules)) {
                Log.e(TAG, "Invalid sepolicy rules for $pkg")
                return false
            }

            val parseResult = SePolicyParser.parseSepolicy(rules, strict = false)
            parseResult.fold(
                onSuccess = { statements ->
                    Log.i(TAG, "Parsed ${statements.size} policy statements for $pkg")

                    statements.forEach { statement ->
                        when (statement) {
                            is PolicyStatement.NormalPermission ->
                                Log.d(TAG, "Setting permission: ${statement.perm.op}")

                            is PolicyStatement.TypeDefStmt ->
                                Log.d(TAG, "Setting type: ${statement.typeDef.name}")

                            else -> Log.d(TAG, "Setting policy: ${statement.javaClass.simpleName}")
                        }
                    }
                },
                onFailure = { error ->
                    Log.w(TAG, "Parse warning for $pkg: ${error.message}")
                }
            )

            val sepolicyDir = SuFile("/data/adb/ksu/profile/selinux")
            if (!sepolicyDir.exists()) {
                sepolicyDir.mkdirs()
            }

            val sepolicyFile = SuFile(sepolicyDir, pkg)
            sepolicyFile.writeText(rules)

            val success = applyPolicyRules(pkg, rules)

            Log.i(TAG, "Set sepolicy for $pkg: ${if (success) "SUCCESS" else "FAILED"}")
            success

        } catch (e: IOException) {
            Log.e(TAG, "Failed to write sepolicy for $pkg: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error setting sepolicy for $pkg: ${e.message}")
            false
        }
    }

    private fun applyPolicyRules(pkg: String, rules: String): Boolean {
        return try {
            val parseResult = SePolicyParser.parseSepolicy(rules, strict = false)
            parseResult.fold(
                onSuccess = { statements ->
                    // Convert to atomic statements for system application
                    val atomicStatements = statements.flatMap { it.toAtomicStatements() }

                    val success =
                        KsuNative.applyPolicyRules(atomicStatements.toTypedArray(), strict = false)
                    if (!success) return false

                    Log.i(
                        TAG,
                        "Successfully applied ${atomicStatements.size} atomic policy statements for $pkg"
                    )
                    true
                },
                onFailure = { error ->
                    Log.e(TAG, "Failed to parse policy for application: ${error.message}")
                    false
                }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to apply policy rules for $pkg: ${e.message}")
            false
        }
    }
}