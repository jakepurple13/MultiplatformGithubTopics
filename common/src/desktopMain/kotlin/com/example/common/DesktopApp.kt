// Copyright 2000-2022 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.example.common

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import kotlinx.coroutines.flow.flow
import me.friwi.jcefmaven.CefAppBuilder
import me.friwi.jcefmaven.MavenCefAppHandlerAdapter
import me.friwi.jcefmaven.impl.progress.ConsoleProgressHandler
import org.cef.CefApp
import org.cef.CefClient
import org.cef.browser.CefBrowser
import java.awt.Component
import java.io.File
import javax.swing.BoxLayout
import javax.swing.JPanel

@Preview
@Composable
fun AppPreview() {
    val scope = rememberCoroutineScope()
    App(remember { TopicViewModel(scope, flow { }) }, remember { FavoritesViewModel(scope, Database()) })
}

@Composable
fun WebView(
    component: Component,
    modifier: Modifier = Modifier
) {
    SwingPanel(
        background = MaterialTheme.colorScheme.background,
        modifier = modifier,
        factory = {
            JPanel().apply {
                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                add(component)
            }
        }
    )
}

val LocalBrowserHandler = staticCompositionLocalOf { BrowserHandler() }

class BrowserHandler {
    val app: CefApp by lazy {
        val builder = CefAppBuilder()
        //Configure the builder instance
        builder.setInstallDir(File("jcef-bundle")) //Default
        builder.setProgressHandler(ConsoleProgressHandler()) //Default
        builder.cefSettings.windowless_rendering_enabled = true //Default - select OSR mode
        builder.addJcefArgs("--force-dark-mode")

        //Set an app handler. Do not use CefApp.addAppHandler(...), it will break your code on MacOSX!
        builder.setAppHandler(object : MavenCefAppHandlerAdapter() {})
        //Build a CefApp instance using the configuration above
        builder.build()
    }

    val client: CefClient by lazy { app.createClient() }
    fun createBrowser(url: String): CefBrowser = client.createBrowser(url, false, false)
}