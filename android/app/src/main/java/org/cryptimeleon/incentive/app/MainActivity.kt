package org.cryptimeleon.incentive.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import dagger.hilt.android.AndroidEntryPoint
import org.cryptimeleon.incentive.app.basket.Basket
import org.cryptimeleon.incentive.app.dashboard.Dashboard
import org.cryptimeleon.incentive.app.scan.ScanScreen
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val items = listOf(
        Screen.Dashboard,
        Screen.Scan,
        Screen.Basket,
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
                    topBar = {
                        var showMenu by remember { mutableStateOf(false) }

                        TopAppBar(
                            title = { Text("Cryptimeleon Rewards") },
                            actions = {
                                IconButton(onClick = { showMenu = !showMenu }) {
                                    Icon(Icons.Default.MoreVert, "Menu Icon")
                                }
                                DropdownMenu(
                                    expanded = showMenu,
                                    onDismissRequest = { showMenu = false }
                                ) {
                                    DropdownMenuItem(onClick = { /*TODO*/ }) {
                                        Icon(Icons.Filled.Speed, "Benchmark Icon")
                                    }
                                    DropdownMenuItem(onClick = { /*TODO*/ }) {
                                        Icon(Icons.Filled.Settings, "Settings Icon")
                                    }
                                }
                            }
                        )
                    },
                    bottomBar = {
                        BottomNavigation {
                            val navBackStackEntry by navController.currentBackStackEntryAsState()
                            val currentDestination = navBackStackEntry?.destination
                            items.forEach { screen ->
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
                ) { innerPadding ->
                    NavHost(
                        navController,
                        startDestination = Screen.Dashboard.route,
                        Modifier.padding(innerPadding)
                    ) {
                        composable(Screen.Dashboard.route) { Dashboard() }
                        composable(Screen.Scan.route) { ScanScreen() }
                        composable(Screen.Basket.route) { Basket() }
                    }
                }
            }
        }


        // Load mcl
        System.loadLibrary("mcljava")

        // For logging
        Timber.uprootAll() // Ensure there is only one tree to avoid duplicate logs
        Timber.plant(Timber.DebugTree())
    }

}

sealed class Screen(val route: String, @StringRes val resourceId: Int, val icon: ImageVector) {
    object Dashboard : Screen("dashbaord", R.string.dashboard, Icons.Default.Home)
    object Scan : Screen("scan", R.string.scan, Icons.Default.QrCodeScanner)
    object Basket : Screen("basket", R.string.basket, Icons.Default.ShoppingBasket)
}