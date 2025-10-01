package com.dergoogler.mmrl.ui.screens.moduleView.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R

@Composable
fun VersionsItem(
    enabled: Boolean = true,
    count: Int,
    onClick: () -> Unit,
) {
    val colors = ButtonDefaults.filledTonalButtonColors()
    val style = MaterialTheme.typography.labelLarge
    val iconSize =
        with(LocalDensity.current) { style.fontSize.toDp() * 1.2f }

    val containerColor = if (enabled) colors.containerColor else colors.disabledContainerColor
    val contentColor = if (enabled) colors.contentColor else colors.disabledContentColor

    Surface(
        enabled = enabled,
        modifier = Modifier.semantics { role = Role.Button },
        color =  containerColor,
        contentColor = contentColor,
        tonalElevation = 1.dp,
        shape = RoundedCornerShape(20.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(id = R.drawable.file_zip),
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(6.dp))

            Text(
                style = style,
                text = count.toString()
            )
        }
    }
}