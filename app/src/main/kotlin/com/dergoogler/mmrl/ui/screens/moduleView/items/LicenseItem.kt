package com.dergoogler.mmrl.ui.screens.moduleView.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.BottomSheet
import com.dergoogler.mmrl.ui.component.LicenseContent

@Composable
fun LicenseItem(
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    licenseId: String,
) = Box {
    var open by rememberSaveable { mutableStateOf(false) }

    Text(
        style = style.copy(color = MaterialTheme.colorScheme.surfaceTint),
        modifier = Modifier.clickable(
            onClick = { open = true }
        ),
        text = licenseId,
    )

    if (open) {
        BottomSheet(
            onDismissRequest = { open = false },
        ) {
            Text(
                text = stringResource(id = R.string.license_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            LicenseContent(
                licenseId = licenseId,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .padding(horizontal = 16.dp)
            )
        }
    }
}