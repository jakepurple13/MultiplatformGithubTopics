// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.example.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.flow.flow

@Preview
@Composable
fun AppPreview() {
    val scope = rememberCoroutineScope()
    App(remember { TopicViewModel(scope, flow { }) }, remember { FavoritesViewModel(scope, Database()) })
}