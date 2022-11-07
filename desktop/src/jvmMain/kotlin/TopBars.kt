import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
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
        visible = visible
    ) {
        frameWindowScope()
        Surface(
            shape = when (hostOs) {
                OS.Linux -> RoundedCornerShape(8.dp)
                OS.Windows -> RectangleShape
                OS.MacOS -> RoundedCornerShape(8.dp)
                else -> RoundedCornerShape(8.dp)
            },
            modifier = Modifier.animateContentSize()
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
                                backgroundColor = M3MaterialTheme.colorScheme.surface,
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
                bottomBar = bottomBar
            ) { padding -> Surface(modifier = Modifier.padding(padding)) { content() } }
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
            IconButton(onClick = onExit) {
                Icon(
                    Icons.Default.Close,
                    null
                )
            }
            IconButton(onClick = {
                state.isMinimized = !state.isMinimized
            }) { Icon(Icons.Default.Minimize, null) }
            IconButton(
                onClick = {
                    state.placement = if (state.placement != WindowPlacement.Maximized) WindowPlacement.Maximized
                    else WindowPlacement.Floating
                }
            ) { Icon(Icons.Default.Maximize, null) }
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
            IconButton(onClick = onExit) {
                Icon(
                    Icons.Default.Close,
                    null
                )
            }
            IconButton(onClick = {
                state.isMinimized = !state.isMinimized
            }) { Icon(Icons.Default.Minimize, null) }
            IconButton(
                onClick = {
                    state.placement = if (state.placement != WindowPlacement.Maximized) WindowPlacement.Maximized
                    else WindowPlacement.Floating
                }
            ) { Icon(Icons.Default.Maximize, null) }
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
            IconButton(onClick = onExit) {
                Icon(
                    Icons.Default.Close,
                    null
                )
            }
            IconButton(onClick = {
                state.isMinimized = !state.isMinimized
            }) { Icon(Icons.Default.Minimize, null) }
            IconButton(
                onClick = {
                    state.placement = if (state.placement != WindowPlacement.Fullscreen) WindowPlacement.Fullscreen
                    else WindowPlacement.Floating
                }
            ) {
                Icon(
                    if (state.placement != WindowPlacement.Fullscreen) Icons.Default.Fullscreen else Icons.Default.FullscreenExit,
                    null
                )
            }
        }

        Text(
            windowTitle,
            modifier = Modifier.align(Alignment.Center),
        )
    }
}
