package com.dergoogler.mmrl.ui.screens.settings.appearance.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ui.component.Logo

@Composable
fun ExampleItem() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        OutlinedCard(
            modifier =
                Modifier
                    .padding(vertical = 16.dp),
        ) {
            Column(
                modifier =
                    Modifier
                        .fillMaxSize(0.5f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Logo(
                        modifier =
                            Modifier
                                .padding(horizontal = 10.dp)
                                .size(20.dp),
                        icon = R.drawable.launcher_outline,
                    )

                    Text(text = stringResource(id = R.string.app_name))
                }

                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Spacer(
                        modifier =
                            Modifier
                                .height(60.dp)
                                .fillMaxWidth(0.9f),
                    )
                }

                Surface(
                    shape = RoundedCornerShape(15.dp),
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Spacer(
                        modifier =
                            Modifier
                                .height(60.dp)
                                .fillMaxWidth(0.9f),
                    )
                }

                Spacer(modifier = Modifier.height(160.dp))

                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 2.dp,
                ) {
                    Spacer(
                        modifier =
                            Modifier
                                .height(45.dp)
                                .fillMaxWidth(),
                    )
                }
            }
        }
    }
}
