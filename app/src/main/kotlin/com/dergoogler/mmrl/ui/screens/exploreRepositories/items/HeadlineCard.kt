package com.dergoogler.mmrl.ui.screens.exploreRepositories.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.text.BBCodeText

@Composable
fun HeadlineCard(
    repoCount: Int,
    moduleCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp),
                        MaterialTheme.colorScheme.background
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                RoundedCornerShape(20.dp)
            )
            .border(
                Dp.Hairline,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(20.dp)
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = stringResource(R.string.module_repositories),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
            )

            BBCodeText(
                text = stringResource(R.string.explore_repos_discover_text, repoCount, moduleCount),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.outlineVariant,
                lineHeight = 20.sp
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                StatBox(repoCount.toString(), stringResource(R.string.page_repos))
                StatBox(stringResource(R.string.count_with_plus, moduleCount),
                    stringResource(R.string.available_modules)
                )
            }
        }
    }
}

@Composable
fun StatBox(value: String, label: String) {
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background, RoundedCornerShape(20.dp))
            .border(
                Dp.Hairline,
                MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(20.dp)
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.surfaceTint
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}