package org.cryptimeleon.incentive.app

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import org.cryptimeleon.incentive.app.ui.attacker.AttackerUi
import org.cryptimeleon.incentive.app.ui.basket.BasketUi
import org.cryptimeleon.incentive.app.ui.benchmark.BenchmarkUi
import org.cryptimeleon.incentive.app.ui.checkout.CheckoutUi
import org.cryptimeleon.incentive.app.ui.dashboard.Dashboard
import org.cryptimeleon.incentive.app.ui.promotion.PromotionDetailUi
import org.cryptimeleon.incentive.app.ui.scan.ScanScreen
import org.cryptimeleon.incentive.app.ui.settings.Settings
import org.cryptimeleon.incentive.app.ui.setup.SetupUi
import java.math.BigInteger

object MainDestination {
    const val ATTACKER_ROUTE = "attacker"
    const val LOADING_ROUTE = "loading"
    const val DASHBOARD_ROUTE = "dashboard"
    const val SCANNER_ROUTE = "scanner"
    const val BASKET_ROUTE = "basket"
    const val CHECKOUT_ROUTE = "checkout"
    const val SETTINGS_ROUTE = "settings"
    const val BENCHMARK_ROUTE = "benchmark"
    const val PROMOTION_DETAIL_ROUTE = "promotionDetail"
}

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
                    actions.openBenchmark,
                    actions.openAttacker,
                    actions.navigateToPromotionDetail,
                )
            }
        }
        composable(MainDestination.SCANNER_ROUTE) {
            ScanScreen(
                actions.openSettings,
                actions.openBenchmark,
                actions.openAttacker
            )
        }
        composable(MainDestination.BASKET_ROUTE) {
            BasketUi(
                actions.openScanner,
                actions.openSettings,
                actions.openBenchmark,
                actions.openAttacker,
                actions.openCheckout
            )
        }
        composable(MainDestination.CHECKOUT_ROUTE) {
            CheckoutUi(actions.navigateToDashboard)
        }
        composable(MainDestination.SETTINGS_ROUTE) { Settings(actions.onExitSettings) }
        composable(MainDestination.BENCHMARK_ROUTE) { BenchmarkUi(actions.onExitBenchmark) }
        composable(MainDestination.ATTACKER_ROUTE) { AttackerUi(actions.onExitAttackerScreen) }
        composable(
            route = "${MainDestination.PROMOTION_DETAIL_ROUTE}/{promotionId}",
            arguments = listOf(
                navArgument("promotionId") {
                    type = NavType.StringType
                }
            )
        ) { entry ->
            val promotionIdString: String = entry.arguments?.getString("promotionId")
                ?: throw RuntimeException("Could not find argument")
            val promotionId = BigInteger(promotionIdString)
            PromotionDetailUi(
                promotionId = promotionId,
                onUpClicked = actions.onExitPromotionDetails
            )
        }
    }
}

class MainActions(navController: NavHostController) {
    val onExitAttackerScreen: () -> Unit = {
        navController.popBackStack()
    }
    val onLoadingComplete: () -> Unit = {
        navController.popBackStack()
    }

    val onExitSettings: () -> Unit = {
        navController.popBackStack()
    }

    val onExitBenchmark: () -> Unit = {
        navController.popBackStack()
    }

    val onExitPromotionDetails: () -> Unit = {
        navController.popBackStack()
    }

    val openScanner: () -> Unit = {
        navController.navigate(MainDestination.SCANNER_ROUTE)
    }

    val openSettings: () -> Unit = {
        navController.navigate(MainDestination.SETTINGS_ROUTE)
    }

    val openAttacker: () -> Unit = {
        navController.navigate(MainDestination.ATTACKER_ROUTE)
    }

    val openBenchmark: () -> Unit = {
        navController.navigate(MainDestination.BENCHMARK_ROUTE)
    }

    val navigateToDashboard: () -> Unit = {
        navController.navigate(MainDestination.DASHBOARD_ROUTE)
    }

    val openCheckout: () -> Unit = {
        navController.navigate(MainDestination.CHECKOUT_ROUTE)
    }

    val navigateToPromotionDetail: (promotionId: BigInteger) -> Unit = {
        navController.navigate("${MainDestination.PROMOTION_DETAIL_ROUTE}/$it")
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
