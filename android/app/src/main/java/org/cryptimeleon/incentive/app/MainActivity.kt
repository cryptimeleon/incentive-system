package org.cryptimeleon.incentive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainNavigationScreens = listOf(
        MainNavigationScreen.Dashboard,
        MainNavigationScreen.Scan,
        MainNavigationScreen.Basket,
    )

    @ExperimentalPermissionsApi
    @ExperimentalAnimationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO implement startup screen like https://github.com/android/compose-samples/blob/22eea87c898c8cc76e0c1042274e0962f0e0ba52/Owl/app/src/main/java/com/example/owl/ui/NavGraph.kt#L74
        setContent {
            CryptimeleonTheme {
                val navController = rememberNavController()
                Scaffold(
                    bottomBar = {
                        CryptimeleonBottomBar(navController)
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        finishActivity = { finish() },
                        innerPadding = innerPadding
                    )
                }
            }
        }


        // Load mcl
        System.loadLibrary("mcljava")

        // For logging
        Timber.uprootAll() // Ensure there is only one tree to avoid duplicate logs
        Timber.plant(Timber.DebugTree())
    }

    @Composable
    private fun CryptimeleonBottomBar(navController: NavHostController) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
            ?: MainNavigationScreen.Dashboard.route

        val mainNavigationRoutes = remember {
            mainNavigationScreens.map { it.route }
        }

        // Only display bottom navigation on mainNavigation Routes
        if (currentRoute in mainNavigationRoutes) {
            BottomNavigation {
                val currentDestination = navBackStackEntry?.destination
                mainNavigationScreens.forEach { screen ->
                    BottomNavigationItem(
                        icon = {
                            Icon(
                                screen.icon,
                                contentDescription = "${screen.resourceId} Icon"
                            )
                        },
                        label = { Text(stringResource(screen.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = false
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = false
                            }
                        }
                    )
                }
            }
        }
    }
}


sealed class MainNavigationScreen(
    val route: String,
    @StringRes val resourceId: Int,
    val icon: ImageVector
) {
    object Dashboard : MainNavigationScreen(
        MainDestination.DASHBOARD_ROUTE,
        R.string.dashboard,
        Icons.Default.Home
    )

    object Scan : MainNavigationScreen(
        MainDestination.SCANNER_ROUTE,
        R.string.scan,
        Icons.Default.QrCodeScanner
    )

    object Basket : MainNavigationScreen(
        MainDestination.BASKET_ROUTE,
        R.string.basket,
        Icons.Default.ShoppingBasket
    )
}
