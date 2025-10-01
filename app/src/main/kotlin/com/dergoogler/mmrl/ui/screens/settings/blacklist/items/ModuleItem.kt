package com.dergoogler.mmrl.ui.screens.settings.blacklist.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.online.Blacklist
import com.dergoogler.mmrl.ui.component.AntiFeaturesItem
import com.dergoogler.mmrl.ui.component.BottomSheet
import com.dergoogler.mmrl.ui.component.MarkdownText
import com.dergoogler.mmrl.ui.component.NavigationBarsSpacer
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListScope
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Description
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title

@Composable
fun ListScope.ModuleItem(
    module: Blacklist,
) {
    var open by remember { mutableStateOf(false) }
    if (open) {
        BlacklistBottomSheet(
            module = module,
            onClose = { open = false })
    }

    ButtonItem(
        onClick = { open = true }
    ) {
        Title(module.id)
        Description(module.source)
    }
}


@Composable
fun BlacklistBottomSheet(
    module: Blacklist, onClose: () -> Unit,
) = BottomSheet(onDismissRequest = onClose) {
    val browser = LocalUriHandler.current

    Column(
        modifier = Modifier.padding(bottom = 18.dp),
    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                style = MaterialTheme.typography.titleLarge, text = module.id
            )

            Spacer(modifier = Modifier.height(16.dp))

            module.notes?.let {
                if (it.isNotEmpty()) {
                    Surface(
                        color = MaterialTheme.colorScheme.surface,
                        tonalElevation = 1.dp,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = stringResource(R.string.additional_notes),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                            MarkdownText(
                                text = it,
                                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.outline),
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }


//        module.antifeatures?.let {
//            if (it.isNotEmpty()) {
//                AntiFeaturesItem(antifeatures = it)
//            }
//        }

        Spacer(modifier = Modifier.height(10.dp))

        Button(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            onClick = {
                browser.openUri(module.source)
            }
        ) {
            Text(stringResource(R.string.open_source))
        }

        NavigationBarsSpacer()
    }
}