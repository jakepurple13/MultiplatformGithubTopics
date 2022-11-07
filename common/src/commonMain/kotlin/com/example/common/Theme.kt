package com.example.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.MaterialTheme
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.material3.MaterialTheme as M3MaterialTheme

@Composable
fun Theme(appActions: AppActions, content: @Composable () -> Unit) {
    M3MaterialTheme(colorScheme = if (isSystemInDarkTheme()) darkColorScheme() else lightColorScheme()) {
        MaterialTheme(colors = if (isSystemInDarkTheme()) darkColors() else lightColors()) {
            CompositionLocalProvider(
                LocalAppActions provides appActions
            ) { content() }
        }
    }
}

val LocalAppActions = staticCompositionLocalOf<AppActions> { error("No Actions") }