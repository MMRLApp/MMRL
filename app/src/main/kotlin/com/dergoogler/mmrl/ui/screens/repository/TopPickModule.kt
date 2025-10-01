package com.dergoogler.mmrl.ui.screens.repository

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.iconSize
import com.dergoogler.mmrl.ui.component.Logo
import com.dergoogler.mmrl.ui.component.card.Card
import com.dergoogler.mmrl.ui.providable.LocalDestinationsNavigator
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule
import com.dergoogler.mmrl.ui.providable.LocalRepo
import com.dergoogler.mmrl.ui.providable.LocalUserPreferences
import com.ramcosta.composedestinations.generated.destinations.NewViewScreenDestination

@Composable
fun TopPickModule(
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val navigator = LocalDestinationsNavigator.current
    val module = LocalOnlineModule.current
    val repo = LocalRepo.current
    val userPreferences = LocalUserPreferences.current
    val menu = userPreferences.repositoryMenu
    val isVerified = module.isVerified && menu.showVerified

    Card(
        onClick = {
            navigator.navigate(NewViewScreenDestination(repo, module))
        },
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = Dp.Hairline,
                color = MaterialTheme.colorScheme.outlineVariant,
                shape = RoundedCornerShape(20.dp)
            ),
        enabled = enabled,
        shape = RoundedCornerShape(20.dp)
    ) {

        Row(
            modifier = Modifier
                .padding(all = 16.dp)
                .relative(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Logo(
                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(64.dp),
                shape = RoundedCornerShape(20),
                icon = {
                    if (module.icon != null) {
                        AsyncImage(
                            model = module.icon,
                            contentScale = ContentScale.Crop,
                            contentDescription = null,
                        )
                    } else {
                        Icon(
                            modifier = it,
                            painter = painterResource(id = R.drawable.box),
                            contentDescription = null,
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.width(10.dp))


            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    val style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = module.name,
                        style = style,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isVerified) {
                        Icon(
                            modifier = Modifier.iconSize(LocalDensity.current, style, 1.0f),
                            painter = painterResource(id = R.drawable.rosette_discount_check),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.surfaceTint
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = module.author,
                    style = MaterialTheme.typography.bodyMedium.copy(),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    maxLines = 1,
                    text = module.versionDisplay,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}