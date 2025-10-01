package com.dergoogler.mmrl.ui.screens.moduleView.sections

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.ifNotEmpty
import com.dergoogler.mmrl.ext.ifNotNullOrBlank
import com.dergoogler.mmrl.ui.component.AntiFeaturesItem
import com.dergoogler.mmrl.ui.component.LabelItem
import com.dergoogler.mmrl.ui.component.PermissionItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.CollapseItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.Item
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Icon
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Labels
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.screens.moduleView.items.OtherSourcesItem
import com.dergoogler.mmrl.ui.screens.moduleView.listItemContentPaddingValues
import com.dergoogler.mmrl.ui.screens.moduleView.providable.LocalModuleViewModel
import com.dergoogler.mmrl.ui.screens.moduleView.providable.LocalRequireModules
import com.dergoogler.mmrl.ui.screens.moduleView.subListItemContentPaddingValues

@Composable
internal fun Information0() {
    val viewModel = LocalModuleViewModel.current
    val module = LocalOnlineModule.current
    val browser = LocalUriHandler.current
    val requires = LocalRequireModules.current

    List(
        contentPadding = listItemContentPaddingValues
    ) {
        CollapseItem(
            meta = { icon, rotation ->
                Title(R.string.view_module_module_support)
                Icon(
                    slot = ListItemSlot.End,
                    modifier = Modifier
                        .graphicsLayer(rotationZ = rotation),
                    painter = painterResource(id = icon),
                )
            },
        ) {
            module.donate.ifNotNullOrBlank {
                ButtonItem(
                    contentPadding = subListItemContentPaddingValues,
                    onClick = {
                        browser.openUri(it)
                    }
                ) {
                    Icon(painter = painterResource(id = R.drawable.currency_dollar))
                    Title(R.string.view_module_donate)
                    Description(R.string.view_module_donate_desc)
                }
            }

            ButtonItem(
                contentPadding = subListItemContentPaddingValues,
                onClick = {
                    browser.openUri(module.track.source)
                }
            ) {
                Icon(painter = painterResource(id = R.drawable.brand_git))
                Title(R.string.view_module_source)
            }

            module.homepage.ifNotNullOrBlank {
                ButtonItem(
                    contentPadding = subListItemContentPaddingValues,
                    onClick = {
                        browser.openUri(it)
                    }
                ) {
                    Icon(painter = painterResource(id = R.drawable.world_www))
                    Title(R.string.view_module_homepage)
                }
            }

            module.support?.ifNotNullOrBlank {
                ButtonItem(
                    contentPadding = subListItemContentPaddingValues,
                    onClick = {
                        browser.openUri(it)
                    }
                ) {
                    Icon(painter = painterResource(id = R.drawable.heart_handshake))
                    Title(R.string.view_module_support)
                }
            }
        }

        module.permissions.ifNotEmpty {
            CollapseItem(
                meta = { icon, rotation ->
                    Title(R.string.view_module_permissions)
                    Icon(
                        slot = ListItemSlot.End,
                        modifier = Modifier
                            .graphicsLayer(rotationZ = rotation),
                        painter = painterResource(id = icon),
                    )
                    Labels {
                        LabelItem(
                            text = stringResource(
                                R.string.view_module_section_count,
                                it.size
                            )
                        )
                    }
                },
            ) {
                PermissionItem(
                    contentPadding = subListItemContentPaddingValues,
                    permissions = it
                )
            }
        }

        module.track.antifeatures.ifNotEmpty {
            CollapseItem(
                meta = { icon, rotation ->
                    Title(R.string.view_module_antifeatures)
                    Icon(
                        slot = ListItemSlot.End,
                        modifier = Modifier
                            .graphicsLayer(rotationZ = rotation),
                        painter = painterResource(id = icon),
                    )
                    Labels {
                        LabelItem(
                            text = stringResource(
                                R.string.view_module_section_count,
                                it.size
                            )
                        )
                    }
                },
            ) {
                AntiFeaturesItem(
                    contentPadding = subListItemContentPaddingValues,
                    antifeatures = it
                )
            }
        }

        requires.ifNotEmpty { requiredIds ->
            CollapseItem(
                meta = { icon, rotation ->
                    Title(R.string.view_module_dependencies)
                    Icon(
                        slot = ListItemSlot.End,
                        modifier = Modifier
                            .graphicsLayer(rotationZ = rotation),
                        painter = painterResource(id = icon),
                    )
                    Labels {
                        LabelItem(
                            text = stringResource(
                                R.string.view_module_section_count,
                                requiredIds.size
                            )
                        )
                    }
                },
            ) {
                requiredIds.forEach { onlineModule ->
                    // val parts = requiredId.split("@")

                    // val id = parts[0]
                    // val version = (parts.getOrElse(1) { "-1" }).toInt()

                    ButtonItem(
                        contentPadding = subListItemContentPaddingValues,
                        onClick = {
                            //                                navController.navigateSingleTopTo(
//                                    ModuleViewModel.putModule(onlineModule, moduleArgs.url),
//                                    launchSingleTop = false
//                                )
                        }
                    ) {
                        Title(onlineModule.name)
                        Description(onlineModule.versionCode.toString())
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