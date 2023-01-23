package org.cryptimeleon.incentive.app.ui.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer

data class Page(
    val title: String,
    val description: String,
    val emoji: String,
    val isUserNamePage: Boolean = false
)

val onboardingPages = listOf(
    Page(
        "Go Shopping",
        "Scan products when you put them in your shopping cart.",
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
        "Registration",
        "During registration, we associate your name with a cryptographic credential. "
                + "This credential and thus your identity stays private, unless you try to attack the system.",
        "🙋",
        true
    ),
    Page(
        "Designed for Privacy",
        "We protect your privacy data by only disclosing minimal data and hiding your identity. Powered by cryptography!",
        "🛡"
    )
)

@OptIn(ExperimentalPagerApi::class, ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun OnboardingScreen(navigateToApp: () -> Unit = {}) {
    val onboardingViewModel = hiltViewModel<OnboardingViewModel>()
    val pagerState = rememberPagerState()
    var userName: String by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    Column {
        HorizontalPager(
            count = onboardingPages.size,
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) { i ->
            val page = onboardingPages[i]
            OnboardingPage(page = page) {
                if (page.isUserNamePage) {
                    OutlinedTextField(
                        userName,
                        { userName = it },
                        singleLine = true,
                        placeholder = { Text("Type your name") },
                        keyboardActions = KeyboardActions(onDone = {
                            keyboardController?.hide()
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(i+1)
                            }
                        }),
                        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp)
                    )
                }
            }
        }
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(16.dp)
        )
        AnimatedVisibility(visible = pagerState.currentPage == onboardingPages.lastIndex) {
            Button(
                onClick = {
                    onboardingViewModel.setUserData(userName)
                    navigateToApp()
                },
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
fun OnboardingPage(page: Page, optionalEditableArea: @Composable () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(32.dp)
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
        optionalEditableArea()
    }
}

@Preview
@Composable
private fun OnboardingScreenPreview() {
    CryptimeleonPreviewContainer {
        OnboardingScreen()
    }
}
