package org.cryptimeleon.incentive.app

import androidx.activity.compose.BackHandler
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import org.cryptimeleon.incentive.app.basket.Basket
import org.cryptimeleon.incentive.app.dashboard.Dashboard
import org.cryptimeleon.incentive.app.scan.ScanScreen
import org.cryptimeleon.incentive.app.settings.Settings
import org.cryptimeleon.incentive.app.setup.SetupUi

object MainDestination {
    const val LOADING_ROUTE = "loading"
    const val DASHBOARD_ROUTE = "dashboard"
    const val SCANNER_ROUTE = "scanner"
    const val BASKET_ROUTE = "basket"
    const val SETTINGS_ROUTE = "settings"
    const val BENCHMARK_ROUTE = "benchmark"
}


@ExperimentalPermissionsApi
@ExperimentalAnimationApi
@Composable
fun NavGraph(
    navController: NavHostController,
    finishActivity: () -> Unit = {},
    innerPadding: PaddingValues
) {
    val loadingComplete = remember { mutableStateOf(false) }
    val actions = remember(navController) { MainActions(navController) }


    NavHost(
        navController,
        startDestination = MainNavigationScreen.Dashboard.route,
        Modifier.padding(innerPadding)
    ) {
        composable(MainDestination.LOADING_ROUTE) {
            BackHandler {
                finishActivity()
            }

            SetupUi {
                loadingComplete.value = true
                actions.onLoadingComplete()
            }
        }
        composable(MainDestination.DASHBOARD_ROUTE) {
            LaunchedEffect(loadingComplete) {
                if (!loadingComplete.value) {
                    navController.navigate(MainDestination.LOADING_ROUTE)
                }
            }
            if (loadingComplete.value) {
                Dashboard(
                    actions.openSettings,
                    actions.openBenchmark
                )
            }
        }
        composable(MainDestination.SCANNER_ROUTE) {
            ScanScreen(
                actions.openSettings,
                actions.openBenchmark
            )
        }
        composable(MainDestination.BASKET_ROUTE) {
            Basket(
                actions.openSettings,
                actions.openBenchmark
            )
        }
        composable(MainDestination.SETTINGS_ROUTE) { Settings(actions.onExitSettings) }
        composable(MainDestination.BENCHMARK_ROUTE) { Text("Benchmark") }
    }
}

class MainActions(navController: NavHostController) {
    val onLoadingComplete: () -> Unit = {
        navController.popBackStack()
    }

    val onExitSettings: () -> Unit = {
        navController.popBackStack()
    }

    val openSettings: () -> Unit = {
        navController.navigate(MainDestination.SETTINGS_ROUTE)
    }

    val openBenchmark: () -> Unit = {
        navController.navigate(MainDestination.BENCHMARK_ROUTE)
    }
}


/**
 * Fix from OWL app:
 *
 * If the lifecycle is not resumed it means this NavBackStackEntry already processed a nav event.
 * This is used to de-duplicate navigation events.
 */
private fun NavBackStackEntry.lifecycleIsResumed() =
    this.lifecycle.currentState == Lifecycle.State.RESUMED