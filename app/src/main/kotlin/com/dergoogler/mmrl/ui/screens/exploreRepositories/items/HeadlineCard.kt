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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.text.BBCodeText
import dev.mmrlx.compose.ui.theme.MMRLXTheme

@Composable
fun HeadlineCard(
    repoCount: Int,
    moduleCount: Int,
) {
    val colors = MMRLXTheme.colors

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        colors.secondary,
                        colors.background,
                    ),
                    start = Offset.Zero,
                    end = Offset(
                        Float.POSITIVE_INFINITY,
                        Float.POSITIVE_INFINITY,
                    ),
                ),
                shape = RoundedCornerShape(20.dp),
            )
            .border(
                MMRLXTheme.border.dp,
                MMRLXTheme.border.color,
                RoundedCornerShape(20.dp),
            )
            .padding(24.dp),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = stringResource(R.string.module_repositories),
                style = MMRLXTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colors.foreground,
            )

            BBCodeText(
                text = stringResource(
                    R.string.explore_repos_discover_text,
                    repoCount,
                    moduleCount,
                ),
                style = MMRLXTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = colors.foreground,
                lineHeight = 20.sp,
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                StatBox(
                    repoCount.toString(),
                    stringResource(R.string.page_repos),
                )

                StatBox(
                    stringResource(R.string.count_with_plus, moduleCount),
                    stringResource(R.string.available_modules),
                )
            }
        }
    }
}

@Composable
fun StatBox(
    value: String,
    label: String,
) {
    val colors = MMRLXTheme.colors

    Column(
        modifier = Modifier
            .background(
                colors.background,
                RoundedCornerShape(20.dp),
            )
            .border(
                MMRLXTheme.border.dp,
                MMRLXTheme.border.color,
                RoundedCornerShape(20.dp),
            )
            .padding(
                horizontal = 24.dp,
                vertical = 12.dp,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MMRLXTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = colors.foreground,
        )

        Text(
            text = label,
            style = MMRLXTheme.typography.bodyMedium,
            color = colors.mutedForeground,
        )
    }
}