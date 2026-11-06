package com.dergoogler.mmrl.ui.screens.moduleView.sections

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.ifNotEmpty
import com.dergoogler.mmrl.ext.ifNotNullOrBlank
import com.dergoogler.mmrl.ui.component.AntiFeaturesItem
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.PermissionItem
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.screens.moduleView.items.OtherSourcesItem
import com.dergoogler.mmrl.ui.screens.moduleView.listItemContentPaddingValues
import com.dergoogler.mmrl.ui.screens.moduleView.providable.LocalModuleViewModel
import com.dergoogler.mmrl.ui.screens.moduleView.providable.LocalRequireModules
import com.dergoogler.mmrl.ui.screens.moduleView.subListItemContentPaddingValues
import dev.mmrlx.compose.ui.list.List
import dev.mmrlx.compose.ui.list.ListItemSlot
import dev.mmrlx.compose.ui.list.component.ButtonItem
import dev.mmrlx.compose.ui.list.component.CollapseItem
import dev.mmrlx.compose.ui.list.component.Item
import dev.mmrlx.compose.ui.list.component.item.Description
import dev.mmrlx.compose.ui.list.component.item.Icon
import dev.mmrlx.compose.ui.list.component.item.Supporting
import dev.mmrlx.compose.ui.list.component.item.Title

@Composable
internal fun Information0() {
    val viewModel = LocalModuleViewModel.current
    val module = LocalOnlineModule.current
    val browser = LocalUriHandler.current
    val requires = LocalRequireModules.current

    List(
        contentPadding = listItemContentPaddingValues,
    ) {
        CollapseItem(
            first = { icon, rotation ->
                Title(R.string.view_module_module_support)
                Icon(
                    slot = ListItemSlot.End,
                    modifier =
                        Modifier
                            .graphicsLayer(rotationZ = rotation),
                    painter = painterResource(id = icon),
                )
            },
        ) {
            List(
                contentPadding = subListItemContentPaddingValues,
            ) {
                module.donate.ifNotNullOrBlank {
                    ButtonItem(
                        onClick = {
                            browser.openUri(it)
                        },
                    ) {
                        Icon(painter = painterResource(id = R.drawable.currency_dollar))
                        Title(R.string.view_module_donate)
                        Description(R.string.view_module_donate_desc)
                    }
                }

                ButtonItem(
                    onClick = {
                        browser.openUri(module.track.source)
                    },
                ) {
                    Icon(painter = painterResource(id = R.drawable.brand_git))
                    Title(R.string.view_module_source)
                }

                module.homepage.ifNotNullOrBlank {
                    ButtonItem(
                        onClick = {
                            browser.openUri(it)
                        },
                    ) {
                        Icon(painter = painterResource(id = R.drawable.world_www))
                        Title(R.string.view_module_homepage)
                    }
                }

                module.support?.ifNotNullOrBlank {
                    ButtonItem(
                        onClick = {
                            browser.openUri(it)
                        },
                    ) {
                        Icon(painter = painterResource(id = R.drawable.heart_handshake))
                        Title(R.string.view_module_support)
                    }
                }
            }
        }

        module.permissions.ifNotEmpty {
            CollapseItem(
                first = { icon, rotation ->
                    Title(R.string.view_module_permissions)
                    Icon(
                        slot = ListItemSlot.End,
                        modifier =
                            Modifier
                                .graphicsLayer(rotationZ = rotation),
                        painter = painterResource(id = icon),
                    )
                    Supporting {
                        LabelItem(
                            text =
                                stringResource(
                                    R.string.view_module_section_count,
                                    it.size,
                                ),
                        )
                    }
                },
            ) {
                List(
                    contentPadding = subListItemContentPaddingValues,
                ) {
                    PermissionItem(
                        contentPadding = subListItemContentPaddingValues,
                        permissions = it,
                    )
                }
            }
        }

        module.track.antifeatures.ifNotEmpty {
            CollapseItem(
                first = { icon, rotation ->
                    Title(R.string.view_module_antifeatures)
                    Icon(
                        slot = ListItemSlot.End,
                        modifier =
                            Modifier
                                .graphicsLayer(rotationZ = rotation),
                        painter = painterResource(id = icon),
                    )
                    Supporting {
                        LabelItem(
                            text =
                                stringResource(
                                    R.string.view_module_section_count,
                                    it.size,
                                ),
                        )
                    }
                },
            ) {
                List(
                    contentPadding = subListItemContentPaddingValues,
                ) {
                    AntiFeaturesItem(
                        contentPadding = subListItemContentPaddingValues,
                        antifeatures = it,
                    )
                }
            }
        }

        requires.ifNotEmpty { requiredIds ->
            CollapseItem(
                first = { icon, rotation ->
                    Title(R.string.view_module_dependencies)
                    Icon(
                        slot = ListItemSlot.End,
                        modifier =
                            Modifier
                                .graphicsLayer(rotationZ = rotation),
                        painter = painterResource(id = icon),
                    )
                    Supporting {
                        LabelItem(
                            text =
                                stringResource(
                                    R.string.view_module_section_count,
                                    requiredIds.size,
                                ),
                        )
                    }
                },
            ) {
                List(
                    contentPadding = subListItemContentPaddingValues,
                ) {
                    requiredIds.forEach { onlineModule ->
                        ButtonItem(
                            onClick = {},
                        ) {
                            Title(onlineModule.name)
                            Description(onlineModule.versionCode.toString())
                        }
                    }
                }
            }
        }

        viewModel.otherSources.ifNotEmpty {
            Item {
                Title(R.string.from_other_repositories)
            }

            OtherSourcesItem(viewModel.otherSources)
        }
    }
}
