package com.dergoogler.mmrl.ui.screens.appProfile.items

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.core.text.isDigitsOnly
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.nullable
import com.dergoogler.mmrl.platform.SePolicy.isSepolicyValid
import com.dergoogler.mmrl.platform.ksu.Capabilities
import com.dergoogler.mmrl.platform.ksu.Groups
import com.dergoogler.mmrl.platform.ksu.Profile
import com.dergoogler.mmrl.platform.ksu.Profile.Namespace
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.CheckboxDialogItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.CheckboxItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.RadioDialogItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Section
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.TextEditDialogItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.DialogSupportingText
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Labels
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title

@Composable
fun ListScope.RootProfileConfig(
    modifier: Modifier = Modifier,
    fixedName: Boolean,
    profile: Profile,
    onProfileChange: (Profile) -> Unit,
) {
    Section(
        title = stringResource(R.string.profile_custom)
    ) {
        RadioDialogItem(
            selection = profile.namespace,
            options = NamespaceOptions,
            onConfirm = {
                onProfileChange(profile.copy(namespace = it.value))
            }
        ) {
            Title(R.string.profile_namespace)
            it.title.nullable { t ->
                Description(t)
            }
        }

        UidPanel(
            uid = profile.uid,
            label = "User Identifier (UID)",
            onUidChange = {
                onProfileChange(
                    profile.copy(
                        uid = it,
                        rootUseDefault = false
                    )
                )
            }
        )

        UidPanel(
            uid = profile.gid,
            label = "Group Identifier (GID)",
            onUidChange = {
                onProfileChange(
                    profile.copy(
                        gid = it,
                        rootUseDefault = false
                    )
                )
            }
        )

        val selectedGroups = profile.groups.ifEmpty { listOf(0) }.let { e ->
            e.mapNotNull { g ->
                Groups.entries.find { it.gid == g }
            }
        }

        GroupsPanel(selectedGroups) {
            onProfileChange(
                profile.copy(
                    groups = it.map { group -> group.gid }.ifEmpty { listOf(0) },
                    rootUseDefault = false
                )
            )
        }

        val selectedCaps = profile.capabilities.mapNotNull { e ->
            Capabilities.entries.find { it.cap == e }
        }

        CapsPanel(selectedCaps) {
            onProfileChange(
                profile.copy(
                    capabilities = it.map { cap -> cap.cap },
                    rootUseDefault = false
                )
            )
        }
    }

    Section(
        stringResource(R.string.profile_selinux)
    ) {
        SELinuxPanel(
            profile = profile,
            onSELinuxChange = { domain, rules ->
                onProfileChange(
                    profile.copy(
                        context = domain,
                        rules = rules,
                        rootUseDefault = false
                    )
                )
            }
        )
    }
}

@Composable
fun ListScope.GroupsPanel(
    selected: List<Groups>,
    closeSelection: (selection: List<Groups>) -> Unit,
) {
    val groups = remember(selected) {
        Groups.entries.toTypedArray().sortedWith(
            compareBy<Groups> { if (selected.contains(it)) 0 else 1 }
                .then(compareBy {
                    when (it) {
                        Groups.ROOT -> 0
                        Groups.SYSTEM -> 1
                        Groups.SHELL -> 2
                        else -> Int.MAX_VALUE
                    }
                })
                .then(compareBy { it.name })

        )
    }

    val options = remember(groups) {
        groups.map { value ->
            CheckboxItem(
                value = value,
                title = value.display,
                desc = value.desc,
                checked = selected.contains(value),
            )
        }
    }

    CheckboxDialogItem(
        strict = false,
        multiple = true,
        maxChoices = 32,
        options = options,
        onConfirm = {
            closeSelection(it.map { item -> item.value })
        }
    ) {
        Title(R.string.profile_groups)
        Labels {
            selected.forEach {
                LabelItem(it.display)
            }
        }
    }
}

@Composable
fun ListScope.CapsPanel(
    selected: Collection<Capabilities>,
    closeSelection: (selection: List<Capabilities>) -> Unit,
) {
    val caps = remember(selected) {
        Capabilities.entries.toTypedArray().sortedWith(
            compareBy<Capabilities> { if (selected.contains(it)) 0 else 1 }
                .then(compareBy { it.name })
        )
    }

    val options = remember(caps) {
        caps.map { value ->
            CheckboxItem(
                value = value,
                title = value.display,
                desc = value.desc,
                checked = selected.contains(value),
            )
        }
    }

    CheckboxDialogItem(
        strict = false,
        multiple = true,
        options = options,
        onConfirm = {
            closeSelection(it.map { item -> item.value })
        }
    ) {
        Title(R.string.profile_capabilities)
        Labels {
            selected.forEach {
                LabelItem(it.display)
            }
        }
    }
}

@Composable
private fun ListScope.UidPanel(uid: Int, label: String, onUidChange: (Int) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current

    var lastValidUid by remember {
        mutableIntStateOf(uid)
    }

    TextEditDialogItem(
        value = uid.toString(),
        onValid = {
            isTextValidUid(it)
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(onDone = {
            keyboardController?.hide()
        }),
        onConfirm = {
            if (it.isEmpty()) {
                onUidChange(0)
                return@TextEditDialogItem
            }
            val valid = isTextValidUid(it)

            val targetUid = if (valid) it.toInt() else lastValidUid
            if (valid) {
                lastValidUid = it.toInt()
            }

            onUidChange(targetUid)
        }
    ) {
        Title(label)
        Description(it.value)
        if (it.isError) {
            DialogSupportingText("Invalid Input!")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ListScope.SELinuxPanel(
    profile: Profile,
    onSELinuxChange: (domain: String, rules: String) -> Unit,
) {
    TextEditDialogItem(
        value = profile.context,
        strict = false,
        onValid = {
            it.matches(Regex("^[a-z_]+:[a-z0-9_]+:[a-z0-9_]+(:[a-z0-9_]+)?$"))
        },
        onConfirm = {
            onSELinuxChange(it, profile.rules)
        }
    ) {
        if (it.isError) {
            DialogSupportingText(stringResource(R.string.profile_selinux_context_invalid))
        }

        Title(stringResource(R.string.profile_selinux_context))
        Description(it.value)
    }

    TextEditDialogItem(
        value = profile.rules,
        strict = false,
        onValid = {
            isSepolicyValid(it)
        },
        onConfirm = {
            onSELinuxChange(profile.context, it)
        }
    ) {
        Title(stringResource(R.string.profile_selinux_rules))

        if (it.isError) {
            DialogSupportingText(stringResource(R.string.profile_selinux_rules_invalid))
        }

        if (it.value.isBlank()) {
            Description(stringResource(R.string.empty))
        } else {
            Description(it.value)
        }
    }
}


@get:Composable
private val NamespaceOptions: List<RadioDialogItem<Int>>
    get() {
        return enumValues<Namespace>().map {
            val title = when (it) {
                Namespace.INHERITED -> stringResource(R.string.profile_namespace_inherited)
                Namespace.GLOBAL -> stringResource(R.string.profile_namespace_global)
                Namespace.INDIVIDUAL -> stringResource(R.string.profile_namespace_individual)
            }

            RadioDialogItem(
                value = it.ordinal,
                title = title
            )
        }
    }

private fun isTextValidUid(text: String): Boolean {
    return text.isNotEmpty() && text.isDigitsOnly() && text.toInt() >= 0 && text.toInt() <= Int.MAX_VALUE
}