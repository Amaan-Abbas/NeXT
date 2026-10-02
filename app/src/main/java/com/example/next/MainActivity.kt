package com.example.next

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.example.next.core.di.AppContainer
import com.example.next.core.navigation.AppNavigation
import com.example.next.ui.theme.NeXTTheme

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = (application as NextApplication).container
        enableEdgeToEdge()

        setContent {
            val settingsResult by appContainer.getSettingsUseCase().collectAsState(
                initial = com.example.next.core.common.Result.Loading
            )

            val isDarkMode = when (settingsResult) {
                is com.example.next.core.common.Result.Success -> {
                    (settingsResult as com.example.next.core.common.Result.Success<com.example.next.features.settings.domain.model.UserSettings>).data.isDarkMode
                }
                else -> isSystemInDarkTheme()
            }

            val isDynamicColor = when (settingsResult) {
                is com.example.next.core.common.Result.Success -> {
                    (settingsResult as com.example.next.core.common.Result.Success<com.example.next.features.settings.domain.model.UserSettings>).data.isDynamicColor
                }
                else -> true
            }

            NeXTTheme(
                darkTheme = isDarkMode,
                dynamicColor = isDynamicColor
            ) {
                AppNavigation(appContainer = appContainer)
            }
        }
    }
}