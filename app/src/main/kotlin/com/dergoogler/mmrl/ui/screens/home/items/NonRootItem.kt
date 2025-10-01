package com.dergoogler.mmrl.ui.screens.home.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.takeTrue
import com.dergoogler.mmrl.ui.component.card.Card

@Composable
internal fun NonRootItem(
    developerMode: Boolean = false,
) = Card(
    color = MaterialTheme.colorScheme.secondaryContainer,
) {
    developerMode.takeTrue {
        Surface(
            shape = RoundedCornerShape(
                topEnd = 20.dp,
                bottomStart = 15.dp,
                bottomEnd = 0.dp
            ),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .absolute(Alignment.TopEnd)
        ) {
            Text(
                text = "USER!DEV",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }

    Row(
        modifier = Modifier.relative().padding(20.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier.size(45.dp),
            painter = painterResource(id = R.drawable.info_circle_filled),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = stringResource(id = R.string.settings_non_root),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )

            Text(
                text = stringResource(id = R.string.settings_non_root_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}