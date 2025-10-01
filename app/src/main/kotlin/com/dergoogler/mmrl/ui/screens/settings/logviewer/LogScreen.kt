package com.dergoogler.mmrl.ui.screens.settings.logviewer

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.dergoogler.mmrl.R
import com.dergoogler.mmrl.ext.compose.providable.LocalActivity
import com.dergoogler.mmrl.ext.none
import com.dergoogler.mmrl.service.LogcatService
import com.dergoogler.mmrl.ui.activity.MMRLComponentActivity
import com.dergoogler.mmrl.ui.component.LocalScreenProvider
import com.dergoogler.mmrl.ui.component.scrollbar.VerticalFastScrollbar
import com.dergoogler.mmrl.ui.component.toolbar.BlurNavigateUpToolbar
import com.dergoogler.mmrl.ui.providable.LocalHazeState
import com.dergoogler.mmrl.ui.providable.LocalMainScreenInnerPaddings
import com.dergoogler.mmrl.ui.providable.LocalNavController
import com.dergoogler.mmrl.utils.log.LogText
import com.dergoogler.mmrl.utils.log.LogText.Companion.toTextPriority
import com.dergoogler.mmrl.utils.log.Logcat
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import dev.chrisbanes.haze.hazeSource
import kotlinx.coroutines.launch

private val priorities = listOf("VERBOSE", "DEBUG", "INFO", "WARN", "ERROR")

object LogColors {
    @Composable
    fun priorityContainer(priority: Int) = when (priority) {
        Log.VERBOSE -> Color(0xFFD6D6D6)
        Log.DEBUG -> Color(0xFF305D78)
        Log.INFO -> Color(0xFF6A8759)
        Log.WARN -> Color(0xFFBBB529)
        Log.ERROR -> Color(0xFFCF5B56)
        Log.ASSERT -> Color(0xFF8B3C3C)
        else -> MaterialTheme.colorScheme.primary
    }

    @Composable
    fun priorityContent(priority: Int) = when (priority) {
        Log.VERBOSE -> Color(0xFF000000)
        Log.DEBUG -> Color(0xFFBBBBBB)
        Log.INFO -> Color(0xFFE9F5E6)
        Log.WARN -> Color(0xFF000000)
        Log.ERROR -> Color(0xFF000000)
        Log.ASSERT -> Color(0xFFFFFFFF)
        else -> MaterialTheme.colorScheme.onPrimary
    }

    @Composable
    fun message(priority: Int) = when (priority) {
        Log.VERBOSE -> Color(0xFFBBBBBB)
        Log.DEBUG -> Color(0xFF299999)
        Log.INFO -> Color(0xFFABC023)
        Log.WARN -> Color(0xFFBBB529)
        Log.ERROR -> Color(0xFFFF6B68)
        Log.ASSERT -> Color(0xFFFF6B68)
        else -> LocalContentColor.current
    }
}

@Destination<RootGraph>
@Composable
fun LogScreen() = LocalScreenProvider {
    val context = LocalContext.current
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val state = rememberLazyListState()
    var priority by remember { mutableStateOf("DEBUG") }

    val paddingValues = LocalMainScreenInnerPaddings.current
    val activity = LocalActivity.current as MMRLComponentActivity

    val console by remember {
        derivedStateOf {
            LogcatService.console.filter {
                it.priority >= priorities.indexOf(priority) + 2
            }.asReversed()
        }
    }

    with(activity) {
        DisposableEffect(lifecycleScope) {
            lifecycleScope.launch {
                lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
                    LogcatService.isActive
                        .collect { isActive ->
                            if (!isActive) {
                                LogcatService.start(context)
                            }
                        }
                }
            }

            onDispose {
                LogcatService.stop(context)
            }
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                priority = priority,
                onPriority = { priority = it },
                scrollBehavior = scrollBehavior
            )
        },
        contentWindowInsets = WindowInsets.none
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier.hazeSource(LocalHazeState.current),
                state = state,
                reverseLayout = true,
                contentPadding = PaddingValues(
                    top = innerPadding.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding()
                )
            ) {
                items(console) { value ->
                    Column {
                        LogItem(value)
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                        )
                    }
                }
            }

            VerticalFastScrollbar(
                state = state,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(
                        top = innerPadding.calculateTopPadding(),
                        bottom = paddingValues.calculateBottomPadding()
                    )
            )
        }
    }
}

@Composable
private fun TopBar(
    priority: String,
    onPriority: (String) -> Unit,
    scrollBehavior: TopAppBarScrollBehavior,
) = BlurNavigateUpToolbar(
    title = stringResource(id = R.string.settings_log_viewer),
    navController = LocalNavController.current,
    actions = {
        val context = LocalContext.current
        IconButton(
            onClick = { Logcat.shareLogs(context) }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.share),
                contentDescription = null
            )
        }

        var prioritySelect by remember { mutableStateOf(false) }
        IconButton(
            onClick = { prioritySelect = true }
        ) {
            Icon(
                painter = painterResource(id = R.drawable.sort_outline),
                contentDescription = null
            )

            PrioritySelect(
                expanded = prioritySelect,
                selected = priority,
                onClose = { prioritySelect = false },
                onClick = onPriority
            )
        }
    },
    scrollBehavior = scrollBehavior
)

@Composable
private fun LogItem(
    value: LogText,
) = Row(
    modifier = Modifier
        .fillMaxWidth()
        .height(IntrinsicSize.Max),
    verticalAlignment = Alignment.CenterVertically
) {
    Box(
        modifier = Modifier
            .background(
                color = LogColors.priorityContainer(value.priority)
            )
            .fillMaxHeight()
            .padding(horizontal = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = value.priority.toTextPriority(),
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            textAlign = TextAlign.Center,
            color = LogColors.priorityContent(value.priority)
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(all = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = value.tag,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = value.message,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace
            ),
            color = LogColors.message(value.priority)
        )

        Text(
            text = "${value.time} ${value.process}",
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Serif
            ),
            color = MaterialTheme.colorScheme.outline
        )
    }
}

@Composable
private fun PrioritySelect(
    expanded: Boolean,
    selected: String,
    onClose: () -> Unit,
    onClick: (String) -> Unit,
) = DropdownMenu(
    expanded = expanded,
    onDismissRequest = onClose,
    offset = DpOffset(0.dp, 5.dp),
    shape = RoundedCornerShape(15.dp)
) {
    priorities.forEach {
        DropdownMenuItem(
            modifier = Modifier
                .background(
                    if (it == selected) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        Color.Unspecified
                    }
                ),
            text = { Text(text = it) },
            onClick = {
                if (it != selected) onClick(it)
                onClose()
            }
        )
    }
}