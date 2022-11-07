package com.example.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.common.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            var themeColors by remember { mutableStateOf(ThemeColors.Default) }
            val defaultMode = isSystemInDarkTheme()
            var isDarkMode by remember { mutableStateOf(defaultMode) }
            Theme(
                themeColors = themeColors,
                isDarkMode = isDarkMode,
                appActions = AppActions(
                    onCardClick = { navController.navigate("repoReadMe" + "/${Uri.encode(Json.encodeToString(it))}") },
                    onShareClick = {
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, it.htmlUrl)
                            putExtra(Intent.EXTRA_TITLE, it.name)
                            type = "text/plain"
                        }

                        val shareIntent = Intent.createChooser(sendIntent, null)
                        startActivity(shareIntent)
                    }
                )
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "app"
                ) {

                    composable("app") { App(vm = viewModel { TopicViewModel() }) }

                    composable(
                        "repoReadMe" + "/{topic}",
                        arguments = listOf(navArgument("topic") { type = NavType.StringType })
                    ) {
                        GithubRepo(
                            vm = viewModel { RepoViewModel(createSavedStateHandle().get<String>("topic").orEmpty()) },
                            backAction = { navController.popBackStack() }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            currentThemeColors = themeColors,
                            setCurrentThemeColors = { themeColors = it },
                            isDarkMode = isDarkMode,
                            onModeChange = { isDarkMode = it },
                            topPull = {
                                VerticalSpacer(MaterialTheme.spacing.l)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        Modifier
                                            .background(
                                                MaterialTheme.colorScheme.onBackground,
                                                RoundedCornerShape(4.dp)
                                            )
                                            .size(width = 100.dp, height = 8.dp)
                                    )
                                }
                                VerticalSpacer(MaterialTheme.spacing.l)
                            }
                        )
                    }
                }

            }
        }
    }
}