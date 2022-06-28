package org.cryptimeleon.incentive.app.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme

@Composable
fun DefaultTopAppBar(
    onOpenSettings: () -> Unit = {},
    onOpenBenchmark: () -> Unit = {},
    title: @Composable () -> Unit = { Text("Cryptimeleon Rewards") },
    navigationIcon: @Composable (() -> Unit) = {},
    menuEnabled: Boolean = true
) {
    var showMenu by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier.background(
            TopAppBarDefaults.smallTopAppBarColors().containerColor(
                scrollFraction = 0f
            ).value
        )
    ) {
        SmallTopAppBar(
            title = title,
            navigationIcon = navigationIcon,
            modifier = Modifier.windowInsetsPadding(WindowInsets.statusBars),
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
                    }
                }
            }
        )
    }
}

@Preview
@Composable
fun TopAppBarPreview() {
    CryptimeleonTheme {
        DefaultTopAppBar()
    }
}
