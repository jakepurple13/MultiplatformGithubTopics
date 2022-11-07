package com.example.common

import androidx.compose.foundation.layout.BoxScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

expect fun getPlatformName(): String

@Composable
expect fun ChipLayout(modifier: Modifier = Modifier, content: @Composable () -> Unit)

@Composable
expect fun BoxScope.LoadingIndicator(vm: BaseTopicVM)