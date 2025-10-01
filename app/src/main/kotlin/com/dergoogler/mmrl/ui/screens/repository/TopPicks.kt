package com.dergoogler.mmrl.ui.screens.repository

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.model.online.OnlineModule
import com.dergoogler.mmrl.ui.component.listItem.dsl.List
import com.dergoogler.mmrl.ui.component.listItem.dsl.ListItemSlot
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.ButtonItem
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Icon
import com.dergoogler.mmrl.ui.component.listItem.dsl.component.item.Title
import com.dergoogler.mmrl.ui.providable.LocalOnlineModule

@Composable
fun TopPicks(
    label: String,
    onMoreClick: () -> Unit,
    list: List<OnlineModule>,
) {
    val randomModules = remember(list) {
        list.shuffled()
            .sortedBy { if (it == OnlineModule.example()) 1 else 0 }
    }

    val pagerState =
        rememberPagerState(pageCount = { (randomModules.size + 2) / 3 })

    List {
        ButtonItem(
            onClick = onMoreClick
        ) {
            Title(label)
            Icon(
                slot = ListItemSlot.End,
                painter = painterResource(R.drawable.arrow_right)
            )
        }

        Spacer(Modifier.height(8.dp))

        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) { page ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                for (i in 0 until 3) {
                    val itemIndex = page * 3 + i
                    if (itemIndex < randomModules.size) {
                        val item = randomModules[itemIndex]

                        CompositionLocalProvider(
                            LocalOnlineModule provides item,
                        ) {
                            if (item.id == "##==online_example==##") {
                                Spacer(Modifier.height(96.dp))
                                return@CompositionLocalProvider
                            }

                            TopPickModule(
                                modifier = Modifier.height(96.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}