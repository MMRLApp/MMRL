package com.dergoogler.mmrl.ui.component.scrollbar

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.lazy.LazyListItemInfo
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.LazyGridItemInfo
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.runtime.Composable

/**
 * Calculates a [ScrollbarState] driven by the changes in a [LazyListState].
 *
 * @param itemsAvailable the total amount of items available to scroll in the lazy list.
 * @param itemIndex a lookup function for index of an item in the list relative to [itemsAvailable].
 */
@Composable
fun LazyListState.scrollbarState(
    itemsAvailable: Int = layoutInfo.totalItemsCount,
    itemIndex: (LazyListItemInfo) -> Int = LazyListItemInfo::index,
): ScrollbarState =
    scrollbarState(
        itemsAvailable = itemsAvailable,
        visibleItems = { layoutInfo.visibleItemsInfo },
        firstVisibleItemIndex = { visibleItems ->
            interpolateFirstItemIndex(
                visibleItems = visibleItems,
                itemSize = { it.size },
                offset = { it.offset },
                nextItemOnMainAxis = { first -> visibleItems.find { it != first } },
                itemIndex = itemIndex,
            )
        },
        itemPercentVisible = itemPercentVisible@{ itemInfo ->
            itemVisibilityPercentage(
                itemSize = itemInfo.size,
                itemStartOffset = itemInfo.offset,
                viewportStartOffset = layoutInfo.viewportStartOffset,
                viewportEndOffset = layoutInfo.viewportEndOffset,
            )
        },
    )

/**
 * Calculates a [ScrollbarState] driven by the changes in a [LazyGridState]
 *
 * @param itemsAvailable the total amount of items available to scroll in the grid.
 * @param itemIndex a lookup function for index of an item in the grid relative to [itemsAvailable].
 */
@Composable
fun LazyGridState.scrollbarState(
    itemsAvailable: Int,
    itemIndex: (LazyGridItemInfo) -> Int = LazyGridItemInfo::index,
): ScrollbarState =
    scrollbarState(
        itemsAvailable = itemsAvailable,
        visibleItems = { layoutInfo.visibleItemsInfo },
        firstVisibleItemIndex = { visibleItems ->
            interpolateFirstItemIndex(
                visibleItems = visibleItems,
                itemSize = {
                    layoutInfo.orientation.valueOf(it.size)
                },
                offset = { layoutInfo.orientation.valueOf(it.offset) },
                nextItemOnMainAxis = { first ->
                    when (layoutInfo.orientation) {
                        Orientation.Vertical ->
                            visibleItems.find {
                                it != first && it.row != first.row
                            }

                        Orientation.Horizontal ->
                            visibleItems.find {
                                it != first && it.column != first.column
                            }
                    }
                },
                itemIndex = itemIndex,
            )
        },
        itemPercentVisible = itemPercentVisible@{ itemInfo ->
            itemVisibilityPercentage(
                itemSize = layoutInfo.orientation.valueOf(itemInfo.size),
                itemStartOffset = layoutInfo.orientation.valueOf(itemInfo.offset),
                viewportStartOffset = layoutInfo.viewportStartOffset,
                viewportEndOffset = layoutInfo.viewportEndOffset,
            )
        },
    )
