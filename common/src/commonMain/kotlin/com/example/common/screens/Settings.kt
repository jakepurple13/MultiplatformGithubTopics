package com.example.common.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.common.*
import com.example.common.components.GroupButton
import com.example.common.components.GroupButtonModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeColors: ThemeColors,
    setCurrentThemeColors: (ThemeColors) -> Unit,
    isDarkMode: Boolean,
    onModeChange: (Boolean) -> Unit,
    defaultTheme: ColorScheme? = null,
    topPull: @Composable ColumnScope.() -> Unit = {},
    customSettings: @Composable ColumnScope.() -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        topPull()
        Scaffold(
            topBar = { TopAppBar(title = { Text("Settings") }) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = MaterialTheme.spacing.l)
                    .padding(padding)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.l)
            ) {
                NavigationDrawerItem(
                    label = { Text("Select Theme Mode") },
                    badge = {
                        GroupButton(
                            selected = isDarkMode,
                            options = listOf(
                                GroupButtonModel(false) { Text("Day") },
                                GroupButtonModel(true) { Text("Night") },
                            ),
                            onClick = onModeChange
                        )
                    },
                    onClick = {},
                    selected = true,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                TopAppBar(title = { Text("Select Theme", style = MaterialTheme.typography.bodyLarge) })

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.s),
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.l)
                ) {
                    items(ThemeColors.values()) { theme ->
                        Row(
                            Modifier
                                .clip(RoundedCornerShape(MaterialTheme.spacing.s))
                                .clickable { setCurrentThemeColors(theme) }
                                .width(80.dp)
                                .border(
                                    4.dp,
                                    animateColorAsState(
                                        if (currentThemeColors == theme) Color.Green
                                        else MaterialTheme.colorScheme.onBackground
                                    ).value,
                                    RoundedCornerShape(MaterialTheme.spacing.s)
                                )
                        ) {
                            theme
                                .getTheme(!androidx.compose.material.MaterialTheme.colors.isLight)
                                .let { c ->
                                    Column {
                                        ColorBox(color = c.background.animate().value)
                                        ColorBox(color = c.primary.animate().value)
                                        ColorBox(color = c.surface.animate().value)
                                        ColorBox(color = c.primaryVariant.animate().value)
                                    }
                                }

                            if (theme == ThemeColors.Default) {
                                defaultTheme ?: theme.getThemeScheme(isDarkMode)
                            } else {
                                theme.getThemeScheme(isDarkMode)
                            }.let { c ->
                                Column {
                                    ColorBox(color = c.background.animate().value)
                                    ColorBox(color = c.primary.animate().value)
                                    ColorBox(color = c.surface.animate().value)
                                    ColorBox(color = c.secondary.animate().value)
                                }
                            }
                        }
                    }
                }
                Divider()
                customSettings()
                NavigationDrawerItem(
                    label = { Text("View Libraries Used") },
                    onClick = LocalAppActions.current.showLibrariesUsed,
                    selected = true,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )

                Divider()
                Column(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Version: ${AppInfo.VERSION}")
                    Text(getPlatformName())
                }
            }
        }
    }
}

@Composable
fun ColorBox(color: Color) {
    Box(
        Modifier
            .background(color)
            .size(40.dp)
    )
}

@Composable
fun VerticalSpacer(height: Dp) = Spacer(modifier = Modifier.height(height))
