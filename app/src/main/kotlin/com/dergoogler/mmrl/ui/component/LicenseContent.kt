package com.dergoogler.mmrl.ui.component

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.app.Const
import com.dergoogler.mmrl.app.Event.Companion.isFailed
import com.dergoogler.mmrl.app.Event.Companion.isLoading
import com.dergoogler.mmrl.app.Event.Companion.isSucceeded
import com.dergoogler.mmrl.model.json.License
import com.dergoogler.mmrl.network.compose.requestJson
import timber.log.Timber

@Composable
fun LicenseContent(
    licenseId: String,
    modifier: Modifier = Modifier,
) {
    var license: License? by remember { mutableStateOf(null) }
    var message: String? by remember { mutableStateOf(null) }
    val event =
        requestJson<License>(
            url = Const.SPDX_URL.format(licenseId),
            onSuccess = { license = it },
            onFailure = {
                message = it.message
                Timber.e(it, "getLicense: $licenseId")
            },
        )

    Crossfade(
        targetState = event,
        label = "LicenseContent",
    ) {
        when {
            it.isLoading ->
                Loading(
                    minHeight = 200.dp,
                )
            it.isSucceeded ->
                ViewLicense(
                    license = checkNotNull(license),
                    modifier = modifier,
                )
            it.isFailed ->
                Failed(
                    message = message,
                    minHeight = 200.dp,
                )
        }
    }
}

@Composable
private fun ViewLicense(
    license: License,
    modifier: Modifier = Modifier,
) = Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(16.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
) {
    Surface(
        shape = RoundedCornerShape(15.dp),
        tonalElevation = 6.dp,
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.outline),
    ) {
        Column(
            modifier =
                Modifier
                    .padding(all = 16.dp)
                    .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(
                text = license.name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
            )

            if (license.seeAlso.isNotEmpty()) {
                MarkdownText(
                    text =
                        license.seeAlso.joinToString(separator = "\n") {
                            " - [$it]($it)"
                        },
                    style = MaterialTheme.typography.bodyMedium,
                )
            }

            if (license.hasLabel()) {
                LabelsItem(license = license)
            }
        }
    }

    Text(
        modifier =
            Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 18.dp)
                .fillMaxWidth(),
        text = license.licenseText,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.outline,
    )
}

@Composable
private fun LabelsItem(license: License) =
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))

        if (license.isFsfLibre) {
            LabelItem(
                painter = painterResource(id = R.drawable.users),
                text = stringResource(id = R.string.license_fsf_libre),
            )
        }

        if (license.isOsiApproved) {
            LabelItem(
                painter = painterResource(id = R.drawable.brand_open_source),
                text = stringResource(id = R.string.license_osi_approved),
            )
        }
    }

@Composable
private fun LabelItem(
    painter: Painter,
    text: String,
    containerColor: Color = Color.Transparent,
    shape: Shape = RoundedCornerShape(10.dp),
) = Surface(
    shape = shape,
    color = containerColor,
    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
) {
    Row(
        modifier = Modifier.padding(all = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painter,
            contentDescription = null,
            modifier = Modifier.size(ButtonDefaults.IconSize),
        )

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = text,
            maxLines = 1,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}
