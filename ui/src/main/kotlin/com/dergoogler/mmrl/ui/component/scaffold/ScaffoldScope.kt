package com.dergoogler.mmrl.ui.component.scaffold

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.LayoutScopeMarker
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

internal val MaxContentWidth: Dp = 840.dp

@LayoutScopeMarker
@Immutable
interface ScaffoldScope {
    @Composable
    fun ResponsiveContent(
        content: @Composable BoxScope.() -> Unit,
    )

    @Composable
    fun ResponsiveContent(
        maxContentWidth: Dp,
        content: @Composable BoxScope.() -> Unit,
    )

}

internal object ScaffoldScopeInstance : ScaffoldScope {
    @Composable
    override fun ResponsiveContent(
        content: @Composable BoxScope.() -> Unit,
    ) = this.ResponsiveContent(MaxContentWidth, content)

    @Composable
    override fun ResponsiveContent(
        maxContentWidth: Dp,
        content: @Composable BoxScope.() -> Unit,
    ) {
        val horizontalPadding = calculateHorizontalPadding(maxContentWidth)

        Box(
            Modifier
                .fillMaxSize()
        ) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = horizontalPadding)
                    .align(Alignment.TopCenter)
            ) {
                content()
            }
        }
    }

    @Composable
    private fun calculateHorizontalPadding(maxContentWidth: Dp): Dp {
        val configuration = LocalConfiguration.current
        val screenWidth = configuration.screenWidthDp.dp
        val excess = screenWidth - maxContentWidth
        return if (excess > 0.dp) excess / 2 else 0.dp
    }
}