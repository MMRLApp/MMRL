package com.dergoogler.mmrl.ui.component.button

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.ui.R

@Composable
fun DoubleButton(
    modifier: Modifier = Modifier,
    rowModifier: Modifier = Modifier,
    dropdownButtonWidth: Dp = 40.dp,
    onClick: () -> Unit,
    onDropdownClick: () -> Unit,
    enabled: Boolean = true,
    enabledDropdown: Boolean = true,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    @DrawableRes dropdownIcon: Int = R.drawable.caret_down,
    content:
        @Composable()
        (RowScope.() -> Unit),
) {
    Row(
        modifier = rowModifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val borderModifier = 2.5.dp

        Button(
            enabled = enabled,
            onClick = onClick,
            shape =
                RoundedCornerShape(
                    topStart = 24.dp,
                    bottomStart = 24.dp,
                    topEnd = borderModifier,
                    bottomEnd = borderModifier,
                ),
            modifier = modifier,
            contentPadding = contentPadding,
        ) {
            content()
        }

        Spacer(modifier = Modifier.width(borderModifier))

        Button(
            enabled = enabledDropdown,
            onClick = onDropdownClick,
            shape =
                RoundedCornerShape(
                    topStart = borderModifier,
                    bottomStart = borderModifier,
                    topEnd = 24.dp,
                    bottomEnd = 24.dp,
                ),
            contentPadding = PaddingValues(0.dp),
            modifier =
                Modifier
                    .width(dropdownButtonWidth)
                    .fillMaxHeight(),
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = dropdownIcon),
                contentDescription = null,
                tint = LocalContentColor.current,
            )
        }
    }
}
