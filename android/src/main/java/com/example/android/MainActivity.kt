package com.example.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
            ) { App(vm = viewModel { TopicViewModel() }) }
        }
    }
}