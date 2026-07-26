package com.expenseai.manager.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.rememberNavController
import com.expenseai.manager.data.datastore.UserPreferencesDataStore
import com.expenseai.manager.presentation.navigation.ExpenseNavGraph
import com.expenseai.manager.presentation.navigation.Screen
import com.expenseai.manager.presentation.settings.SettingsViewModel
import com.expenseai.manager.ui.theme.ExpenseAITheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesDataStore: UserPreferencesDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settingsViewModel: SettingsViewModel = hiltViewModel()
            val prefs by settingsViewModel.preferences.collectAsState()

            ExpenseAITheme(
                darkTheme = prefs.isDarkMode,
                dynamicColor = prefs.isDynamicColor
            ) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    val startDestination = if (prefs.isBiometricEnabled || prefs.isPinEnabled) {
                        Screen.Auth.route
                    } else {
                        Screen.Dashboard.route
                    }

                    ExpenseNavGraph(
                        navController = navController,
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}
