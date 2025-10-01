package com.dergoogler.mmrl.ui.screens.moduleView.items

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.size
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun TagItem(
    @DrawableRes icon: Int,
    onClick: () -> Unit
) = FilledTonalIconButton(
    onClick = onClick,
    colors = IconButtonDefaults.filledTonalIconButtonColors(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ),
    modifier = Modifier.size(35.dp),
) {
    Icon(
        painter = painterResource(id = icon),
        contentDescription = null
    )
}