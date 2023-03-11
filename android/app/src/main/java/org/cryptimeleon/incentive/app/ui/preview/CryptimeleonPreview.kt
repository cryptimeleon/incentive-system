package org.cryptimeleon.incentive.app.ui.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.cryptimeleon.incentive.app.theme.CryptimeleonTheme

@Composable
fun CryptimeleonPreviewContainer(content: @Composable () -> Unit) {
    CryptimeleonTheme {
        Scaffold {
            Box(modifier = Modifier.padding(it)) {
                content()
            }
        }
    }
}
