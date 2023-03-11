package org.cryptimeleon.incentive.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // To ensure onboarding is only displayed once
        val sharedPref = getSharedPreferences("ONBOARDING", Context.MODE_PRIVATE)
        val firstTimeOpening = sharedPref.getBoolean("first-time", true)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            // Update the system bars to be translucent
            val systemUiController = rememberSystemUiController()
            val darkTheme = isSystemInDarkTheme()

            SideEffect {
                systemUiController.setSystemBarsColor(
                    Color.Transparent,
                    darkIcons = !darkTheme // depends on what background color is used
                )
            }

            CryptimeleonTheme {
                val navController = rememberNavController()
                NavGraph(
                    navController = navController,
                    firstTimeOpening = firstTimeOpening,
                    onOnboardingFinished = {
                        with(sharedPref.edit()) {
                            putBoolean("first-time", false)
                            apply()
                        }
                    },
                )
            }
        }

        // Load mcl
        System.loadLibrary("mcljava")

        // For logging
        Timber.uprootAll() // Ensure there is only one tree to avoid duplicate logs
        Timber.plant(Timber.DebugTree())
    }
}
