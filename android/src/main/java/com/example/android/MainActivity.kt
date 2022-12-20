package com.example.android

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.common.*
import com.example.common.screens.*
import com.fragula2.compose.FragulaNavHost
import com.fragula2.compose.rememberSwipeBackNavigator
import com.fragula2.compose.swipeable
import com.google.accompanist.navigation.material.ExperimentalMaterialNavigationApi
import com.google.accompanist.navigation.material.ModalBottomSheetLayout
import com.google.accompanist.navigation.material.bottomSheet
import com.google.accompanist.navigation.material.rememberBottomSheetNavigator
import io.noties.prism4j.annotations.PrismBundle
import io.realm.kotlin.ext.asFlow
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@PrismBundle(includeAll = true)
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterialNavigationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val db = Database()
        setContent {
            val vm: AppViewModel = viewModel { AppViewModel(db) }
            val favoritesVM: FavoritesViewModel = viewModel { FavoritesViewModel(db) }
            val bottomSheetNavigator = rememberBottomSheetNavigator()
            val swipableNavigator = rememberSwipeBackNavigator()
            val navController = rememberNavController(bottomSheetNavigator, swipableNavigator)
            val scope = rememberCoroutineScope()
            val topicDrawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
            Theme(
                themeColors = vm.themeColors,
                isDarkMode = vm.isDarkMode,
                appActions = AppActions(
                    onCardClick = {
                        navController.navigate(Screen.RepoReadMe.route + "/${Uri.encode(Json.encodeToString(it))}")
                    },
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
                    onSettingsClick = { navController.navigate(Screen.Settings.route) },
                    showLibrariesUsed = { navController.navigate(Screen.LibrariesUsed.route) },
                    showFavorites = {
                        scope.launch { topicDrawerState.close() }
                        navController.navigate(Screen.Favorites.route)
                    },
                )
            ) {
                val view = LocalView.current
                val primary = MaterialTheme.colorScheme.primary
                if (!view.isInEditMode) {
                    SideEffect {
                        window.statusBarColor = primary.toArgb()
                        WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = vm.isDarkMode
                    }
                }

                ModalBottomSheetLayout(
                    bottomSheetNavigator,
                    sheetBackgroundColor = MaterialTheme.colorScheme.background,
                    sheetShape = RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp)
                ) {
                    FragulaNavHost(
                        navController = navController,
                        startDestination = Screen.App.route
                    ) {

                        swipeable(Screen.App.route) {
                            CompositionLocalProvider(
                                LocalTopicDrawerState provides topicDrawerState
                            ) {
                                App(
                                    vm = viewModel { TopicViewModel(vm.settingInformation) },
                                    favoritesVM = favoritesVM
                                )
                            }
                        }

                        swipeable(
                            Screen.RepoReadMe.route + "/{topic}",
                            arguments = listOf(navArgument("topic") { type = NavType.StringType })
                        ) {
                            GithubRepo(
                                vm = viewModel {
                                    RepoViewModel(
                                        createSavedStateHandle().get<String>("topic").orEmpty()
                                    )
                                },
                                favoritesVM = favoritesVM,
                                backAction = { navController.popBackStack() }
                            )
                        }

                        swipeable(Screen.LibrariesUsed.route) { LibrariesUsed { navController.popBackStack() } }

                        swipeable(Screen.Favorites.route) {
                            FavoritesUi(
                                favoritesVM = favoritesVM,
                                backAction = { navController.popBackStack() }
                            )
                        }

                        bottomSheet(Screen.Settings.route) {
                            val context = LocalContext.current
                            SettingsScreen(
                                currentThemeColors = vm.themeColors,
                                setCurrentThemeColors = { scope.launch { db.changeTheme(it) } },
                                isDarkMode = vm.isDarkMode,
                                onModeChange = { scope.launch { db.changeMode(it) } },
                                defaultTheme = when {
                                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                                        if (vm.isDarkMode) dynamicDarkColorScheme(context)
                                        else dynamicLightColorScheme(context)
                                    }

                                    else -> ThemeColors.Default.getThemeScheme(vm.isDarkMode)
                                },
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
}

class AppViewModel(db: Database) : ViewModel() {

    var themeColors by mutableStateOf(ThemeColors.Default)
    var isDarkMode by mutableStateOf(true)
    val settingInformation: MutableStateFlow<SettingInformation> = MutableStateFlow(SettingInformation())

    init {
        viewModelScope.launch {
            val f = db.realm.query(SettingInformation::class).first().find()
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

sealed class Screen(val route: String) {
    object App : Screen("app")
    object RepoReadMe : Screen("repoReadMe")
    object Settings : Screen("settings")
    object LibrariesUsed : Screen("librariesUsed")
    object Favorites : Screen("favorites")
}