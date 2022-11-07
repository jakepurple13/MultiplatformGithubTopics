package com.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.common.*

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Theme(
                appActions = AppActions(
                    onCardClick = {},
                    onShareClick = {}
                )
            ) {
                App(vm = viewModel { TopicViewModel() })
                var themeColors by remember { mutableStateOf(ThemeColors.Default) }
                var isDarkMode by remember { mutableStateOf(true) }
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
                                    .background(MaterialTheme.colorScheme.onBackground, RoundedCornerShape(4.dp))
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