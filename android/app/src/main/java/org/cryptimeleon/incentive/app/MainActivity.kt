package org.cryptimeleon.incentive.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.ShoppingBasket
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import dagger.hilt.android.AndroidEntryPoint
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import timber.log.Timber
import kotlin.math.ln

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainNavigationScreens = listOf(
        MainNavigationScreen.Dashboard,
        MainNavigationScreen.Scan,
        MainNavigationScreen.Basket,
    )

    @OptIn(ExperimentalMaterial3Api::class)
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
                Scaffold(
                    bottomBar = {
                        CryptimeleonBottomBar(navController)
                    }
                ) { innerPadding ->
                    NavGraph(
                        navController = navController,
                        firstTimeOpening = firstTimeOpening,
                        onOnboardingFinished = {
                            with(sharedPref.edit()) {
                                putBoolean("first-time", false)
                                apply()
                            }
                        },
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
            // Private member of surface
            // TODO find less hacky way
            val alpha = ((4.5f * ln(4.dp.value + 1)) + 2f) / 100f
            val extensionColor = MaterialTheme.colorScheme.surfaceTint.copy(alpha = alpha)
                .compositeOver(MaterialTheme.colorScheme.surface)
            Box(modifier = Modifier.background(extensionColor)) {
                NavigationBar(
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
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
        } else {
            Box(modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars))
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
