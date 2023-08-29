package com.example.common.screens

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.common.*
import com.example.common.components.IconsButton
import com.example.common.viewmodels.FavoritesVM
import com.example.common.viewmodels.RepoVM

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
@Composable
fun GithubRepo(
    vm: RepoVM,
    favoritesVM: FavoritesVM,
    backAction: () -> Unit
) {
    val appActions = LocalAppActions.current
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(Unit) { vm.load() }

    if (vm.error) {
        AlertDialog(
            onDismissRequest = { vm.error = false },
            title = { Text("Something went wrong") },
            text = { Text("Something went wrong. Either something happened with the connection or this repo has no readme") },
            confirmButton = { TextButton(onClick = { vm.error = false }) { Text("Dismiss") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = { IconsButton(onClick = backAction, icon = Icons.Default.ArrowBack) },
                title = {
                    ListItem(
                        headlineContent = { Text(vm.item.name, style = MaterialTheme.typography.titleLarge) },
                        overlineContent = { Text(vm.item.fullName) },
                        colors = ListItemDefaults.colors(containerColor = Color.Transparent)
                    )
                },
            )
        },
        bottomBar = {
            BottomAppBar(
                floatingActionButton = {
                    ExtendedFloatingActionButton(
                        text = { Text("Open in Browser") },
                        icon = { Icon(Icons.Default.OpenInBrowser, null) },
                        onClick = { uriHandler.openUri(vm.item.htmlUrl) })
                },
                actions = {
                    RepoViewToggle(repoVM = vm)

                    NavigationBarItem(
                        selected = false,
                        onClick = { appActions.onShareClick(vm.item) },
                        icon = { Icon(Icons.Default.Share, null) },
                        label = { Text("Share") }
                    )

                    val isFavorite by remember {
                        derivedStateOf { favoritesVM.items.any { it.htmlUrl == vm.item.htmlUrl } }
                    }

                    NavigationBarItem(
                        selected = false,
                        onClick = {
                            if (isFavorite) favoritesVM.removeFavorite(vm.item)
                            else favoritesVM.addFavorite(vm.item)
                        },
                        icon = {
                            Icon(if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder, null)
                        },
                        label = { Text("Favorite") }
                    )
                }
            )
        }
    ) { padding ->
        Crossfade(targetState = vm.repoContent) { content ->
            when (content) {
                is ReadMeResponse.Failed -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(
                            content.message + "\nThis repo may not have a ReadMe file. Please visit in browser",
                            textAlign = TextAlign.Center
                        )
                    }
                }

                ReadMeResponse.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is ReadMeResponse.Success -> {
                    RepoContentView(
                        repoVM = vm,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(padding)
                                .verticalScroll(rememberScrollState())
                        ) {
                            MarkdownText(
                                text = content.content,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}