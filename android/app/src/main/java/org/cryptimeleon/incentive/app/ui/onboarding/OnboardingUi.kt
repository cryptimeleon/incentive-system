package org.cryptimeleon.incentive.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer

data class Page(
    val title: String,
    val description: String,
    val emoji: String,
)

val onboardingPages = listOf(
    Page(
        "Go Shopping",
        "Scan product when you put them in you shopping cart.",
        "🛒"
    ),
    Page(
        "Collect Points",
        "Collect points in various promotions for your purchases!",
        "🎟"
    ),
    Page(
        "Unlock Rewards",
        "Trade your points to claim marvellous rewards!",
        "🎁"
    ),
    Page(
        "Designed for Privacy",
        "We protect your privacy data by only disclosing minimal data and hiding your identity. Powered by cryptography!",
        "🛡"
    )
)

@OptIn(ExperimentalPagerApi::class)
@Composable
fun OnboardingScreen(navigateToApp: () -> Unit = {}) {
    val pagerState = rememberPagerState()

    Column(
        Modifier.fillMaxSize()
    ) {
        HorizontalPager(
            count = onboardingPages.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { i ->
            OnboardingPage(page = onboardingPages[i])
        }
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        )
        AnimatedVisibility(visible = pagerState.currentPage == onboardingPages.size - 1) {
            Button(
                onClick = navigateToApp,
                Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp)
            ) {
                Text("Get Started")
            }
        }
    }
}


@Composable
fun OnboardingPage(page: Page) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp)
    ) {
        Text(text = page.emoji, fontSize = 80.sp)
        Spacer(
            modifier = Modifier
                .height(32.dp)
                .fillMaxWidth()
        )
        Text(text = page.title, style = MaterialTheme.typography.headlineLarge)
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    CryptimeleonPreviewContainer {
        OnboardingScreen()
    }
}
