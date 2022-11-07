package com.example.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.common.*
import io.realm.kotlin.ext.asFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Database()
        setContent {
            val vm: AppViewModel = viewModel { AppViewModel(db) }
            val navController = rememberNavController()
            val scope = rememberCoroutineScope()
            Theme(
                themeColors = vm.themeColors,
                isDarkMode = vm.isDarkMode,
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
                    },
                    onSettingsClick = { navController.navigate("settings") }
                )
            ) {
                NavHost(
                    navController = navController,
                    startDestination = "app"
                ) {

                    composable("app") { App(vm = viewModel { TopicViewModel(vm.settingInformation) }) }

                    composable(
                        "repoReadMe" + "/{topic}",
                        arguments = listOf(navArgument("topic") { type = NavType.StringType })
                    ) {
                        GithubRepo(
                            vm = viewModel {
                                RepoViewModel(
                                    createSavedStateHandle().get<String>("topic").orEmpty()
                                )
                            },
                            backAction = { navController.popBackStack() }
                        )
                    }

                    composable("settings") {
                        SettingsScreen(
                            currentThemeColors = vm.themeColors,
                            setCurrentThemeColors = { scope.launch { db.changeTheme(it) } },
                            isDarkMode = vm.isDarkMode,
                            onModeChange = { scope.launch { db.changeMode(it) } },
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

class AppViewModel(db: Database) : ViewModel() {

    var themeColors by mutableStateOf(ThemeColors.Default)
    var isDarkMode by mutableStateOf(true)
    val settingInformation: MutableStateFlow<SettingInformation> = MutableStateFlow(SettingInformation())

    init {
        viewModelScope.launch {
            val f = db.realm.query<SettingInformation>(SettingInformation::class).first().find()
            val info = f ?: db.realm.write { copyToRealm(SettingInformation()) }

            val s = info.asFlow().mapNotNull { it.obj }

            s
                .map { it.isDarkMode }
                .distinctUntilChanged()
                .onEach { isDarkMode = it }
                .launchIn(viewModelScope)

            s
                .map { it.theme }
                .map { ThemeColors.values()[it] }
                .distinctUntilChanged()
                .onEach { themeColors = it }
                .launchIn(viewModelScope)

            s
                .onEach { settingInformation.tryEmit(it) }
                .launchIn(viewModelScope)
        }
    }
}