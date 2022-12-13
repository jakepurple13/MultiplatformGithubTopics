import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.key.KeyEvent
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import org.jetbrains.skiko.OS
import org.jetbrains.skiko.hostOs
import androidx.compose.material3.MaterialTheme as M3MaterialTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ApplicationScope.WindowWithBar(
    onCloseRequest: () -> Unit,
    visible: Boolean = true,
    windowTitle: String = "",
    onPreviewKeyEvent: (KeyEvent) -> Boolean = { false },
    onKeyEvent: (KeyEvent) -> Boolean = { false },
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    bottomBar: @Composable () -> Unit = {},
    frameWindowScope: @Composable FrameWindowScope.() -> Unit = {},
    content: @Composable () -> Unit
) {
    val state = rememberWindowState()

    Window(
        state = state,
        undecorated = true,
        transparent = true,
        onCloseRequest = onCloseRequest,
        visible = visible,
        icon = painterResource("logo.png"),
        onPreviewKeyEvent = onPreviewKeyEvent,
        onKeyEvent = onKeyEvent
    ) {
        CompositionLocalProvider(LocalWindow provides this) {
            frameWindowScope()
            val hasFocus = LocalWindowInfo.current.isWindowFocused
            Surface(
                shape = when (hostOs) {
                    OS.Linux -> RoundedCornerShape(8.dp)
                    OS.Windows -> RectangleShape
                    OS.MacOS -> RoundedCornerShape(8.dp)
                    else -> RoundedCornerShape(8.dp)
                },
                modifier = Modifier.animateContentSize(),
                border = ButtonDefaults.outlinedButtonBorder,
            ) {
                Scaffold(
                    topBar = {
                        Column {
                            WindowDraggableArea(
                                modifier = Modifier.combinedClickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() },
                                    onClick = {},
                                    onDoubleClick = {
                                        state.placement =
                                            if (state.placement != WindowPlacement.Maximized) {
                                                WindowPlacement.Maximized
                                            } else {
                                                WindowPlacement.Floating
                                            }
                                    }
                                )
                            ) {
                                TopAppBar(
                                    backgroundColor = animateColorAsState(
                                        if (hasFocus) M3MaterialTheme.colorScheme.surface
                                        else M3MaterialTheme.colorScheme.surfaceVariant
                                    ).value,
                                    elevation = 0.dp,
                                ) {
                                    when (hostOs) {
                                        OS.Linux -> LinuxTopBar(state, onCloseRequest, windowTitle)
                                        OS.Windows -> WindowsTopBar(state, onCloseRequest, windowTitle)
                                        OS.MacOS -> MacOsTopBar(state, onCloseRequest, windowTitle)
                                        else -> {}
                                    }
                                }
                            }
                            Divider(color = M3MaterialTheme.colorScheme.onSurface)
                        }
                    },
                    containerColor = M3MaterialTheme.colorScheme.surface,
                    bottomBar = bottomBar,
                    snackbarHost = {
                        SnackbarHost(
                            hostState = snackbarHostState,
                            snackbar = { Snackbar(snackbarData = it) }
                        )
                    }
                ) { padding -> Surface(modifier = Modifier.padding(padding)) { content() } }
            }
        }
    }
}

@Composable
fun ApplicationScope.LinuxTopBar(state: WindowState, onExit: () -> Unit, windowTitle: String = "") {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.Start
        ) {
            val hoverInteraction = remember { MutableInteractionSource() }
            val isHovering by hoverInteraction.collectIsHoveredAsState()
            IconButton(
                onClick = onExit,
                modifier = Modifier.hoverable(hoverInteraction)
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    tint = animateColorAsState(if (isHovering) Color.Red else LocalContentColor.current).value
                )
            }
            IconButton(
                onClick = { state.isMinimized = !state.isMinimized },
                modifier = Modifier.hoverable(hoverInteraction)
            ) {
                Icon(
                    Icons.Default.Minimize,
                    null,
                    tint = animateColorAsState(if (isHovering) Color.Yellow else LocalContentColor.current).value
                )
            }
            IconButton(
                onClick = {
                    state.placement = if (state.placement != WindowPlacement.Maximized) WindowPlacement.Maximized
                    else WindowPlacement.Floating
                },
                modifier = Modifier.hoverable(hoverInteraction)
            ) {
                Icon(
                    Icons.Default.Maximize,
                    null,
                    tint = animateColorAsState(if (isHovering) Color.Green else LocalContentColor.current).value
                )
            }
        }

        Text(
            windowTitle,
            modifier = Modifier.align(Alignment.CenterStart),
        )
    }
}

@Composable
fun ApplicationScope.WindowsTopBar(state: WindowState, onExit: () -> Unit, windowTitle: String = "") {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.align(Alignment.CenterEnd),
            horizontalArrangement = Arrangement.Start
        ) {
            val hoverInteraction = remember { MutableInteractionSource() }
            val isHovering by hoverInteraction.collectIsHoveredAsState()
            IconButton(
                onClick = onExit,
                modifier = Modifier.hoverable(hoverInteraction)
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    tint = animateColorAsState(if (isHovering) Color.Red else LocalContentColor.current).value
                )
            }
            IconButton(
                onClick = { state.isMinimized = !state.isMinimized },
                modifier = Modifier.hoverable(hoverInteraction)
            ) {
                Icon(
                    Icons.Default.Minimize,
                    null,
                    tint = animateColorAsState(if (isHovering) Color.Yellow else LocalContentColor.current).value
                )
            }
            IconButton(
                onClick = {
                    state.placement = if (state.placement != WindowPlacement.Maximized) WindowPlacement.Maximized
                    else WindowPlacement.Floating
                },
                modifier = Modifier.hoverable(hoverInteraction)
            ) {
                Icon(
                    Icons.Default.Maximize,
                    null,
                    tint = animateColorAsState(if (isHovering) Color.Green else LocalContentColor.current).value
                )
            }
        }

        Text(
            windowTitle,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}

@Composable
fun ApplicationScope.MacOsTopBar(state: WindowState, onExit: () -> Unit, windowTitle: String = "") {
    Box(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.align(Alignment.CenterStart),
            horizontalArrangement = Arrangement.Start
        ) {
            val hoverInteraction = remember { MutableInteractionSource() }
            val isHovering by hoverInteraction.collectIsHoveredAsState()
            IconButton(
                onClick = onExit,
                modifier = Modifier.hoverable(hoverInteraction)
            ) {
                Icon(
                    Icons.Default.Close,
                    null,
                    tint = animateColorAsState(if (isHovering) Color.Red else LocalContentColor.current).value
                )
            }
            IconButton(
                onClick = { state.isMinimized = !state.isMinimized },
                modifier = Modifier.hoverable(hoverInteraction)
            ) {
                Icon(
                    Icons.Default.Minimize,
                    null,
                    tint = animateColorAsState(if (isHovering) Color.Yellow else LocalContentColor.current).value
                )
            }
            IconButton(
                onClick = {
                    state.placement = if (state.placement != WindowPlacement.Fullscreen) WindowPlacement.Fullscreen
                    else WindowPlacement.Floating
                },
                modifier = Modifier.hoverable(hoverInteraction)
            ) {
                Icon(
                    if (state.placement != WindowPlacement.Fullscreen) Icons.Default.Fullscreen else Icons.Default.FullscreenExit,
                    null,
                    tint = animateColorAsState(if (isHovering) Color.Green else LocalContentColor.current).value
                )
            }
        }

        Text(
            windowTitle,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
