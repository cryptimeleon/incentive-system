package org.cryptimeleon.incentive.app.ui.common

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefaultTopAppBar(
    onOpenSettings: () -> Unit = {},
    onOpenBenchmark: () -> Unit = {},
    onOpenAttacker: () -> Unit = {},
    onDiscardBasket: () -> Unit = {},
    onSelectStore: () -> Unit = {},
    title: @Composable () -> Unit = { Text("Cryptimeleon Rewards") },
    navigationIcon: @Composable (() -> Unit) = {},
    menuEnabled: Boolean = true
) {
    // Reset status bar since we change it for fullscreen images
    val systemUiController = rememberSystemUiController()
    val darkTheme = isSystemInDarkTheme()
    SideEffect {
        systemUiController.setStatusBarColor(
            color = Color.Transparent,
            darkIcons = !darkTheme
        )
    }

    var showMenu by remember { mutableStateOf(false) }

    TopAppBar(
        title = title,
        navigationIcon = navigationIcon,
        // modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
        actions = {
            if (menuEnabled) {
                IconButton(onClick = { showMenu = !showMenu }) {
                    Icon(Icons.Default.Settings, "Menu Icon")
                }
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        onClick = onOpenSettings,
                        text = { Text("Settings") }
                    )
                    DropdownMenuItem(
                        onClick = onOpenBenchmark,
                        text = { Text("Benchmark") }
                    )
                    DropdownMenuItem(
                        onClick = onOpenAttacker,
                        text = { Text("Double-Spending Attack") }
                    )
                    if (onDiscardBasket != {})
                        DropdownMenuItem(
                            onClick = {
                                showMenu = false
                                onDiscardBasket()
                            },
                            text = { Text("Discard Basket") }
                        )
                    if (onSelectStore != {})
                        DropdownMenuItem(
                            onClick = {
                                showMenu = false
                                onSelectStore()
                            },
                            text = { Text("Select Store") }
                        )
                }
            }
        }
    )
}

@Preview
@Composable
fun TopAppBarPreview() {
    CryptimeleonTheme {
        DefaultTopAppBar()
    }
}