package org.cryptimeleon.incentive.app.ui.common

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import org.cryptimeleon.incentive.app.MainDestination
import org.cryptimeleon.incentive.app.R


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


private val mainNavigationScreens = listOf(
    MainNavigationScreen.Dashboard,
    MainNavigationScreen.Scan,
    MainNavigationScreen.Basket,
)


@Composable
fun CryptimeleonBottomBar(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
        ?: MainNavigationScreen.Dashboard.route

    NavigationBar()
    {
        val currentDestination = navBackStackEntry?.destination
        mainNavigationScreens.forEach { screen ->
            NavigationBarItem(
                icon = {
                    Icon(
                        screen.icon,
                        contentDescription = "${screen.resourceId} Icon"
                    )
                },
                label = { Text(stringResource(screen.resourceId)) },
                selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                onClick = {
                    if (screen.route != currentRoute) {
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
                }
            )
        }
    }
}
