package com.example.common

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect fun getPlatformName(): String

@Composable
expect fun LoadImage(model: Any?, modifier: Modifier = Modifier)

@Composable
expect fun ChipLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit)