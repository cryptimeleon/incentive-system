package org.cryptimeleon.incentive.app.ui.onboarding

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
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
import org.cryptimeleon.incentive.app.BuildConfig
import org.cryptimeleon.incentive.app.ui.preview.CryptimeleonPreviewContainer

data class Page(
    val title: String,
    val description: String,
    val emoji: String,
    val isUserNamePage: Boolean = false,
    val isServerUrlPage: Boolean = false
)

val shoppingPage = Page(
    "Go Shopping",
    "Scan products when you put them in your shopping cart.",
    "🛒",
    isServerUrlPage = false
)
val collectPage = Page(
    "Collect Points",
    "Collect points in various promotions for your purchases!",
    "🎟"
)
val rewardsPage = Page(
    "Unlock Rewards",
    "Trade your points to claim marvellous rewards!",
    "🎁"
)
val registrationPage = Page(
    "Registration",
    "During registration, we associate your name with a cryptographic credential. " +
            "The name is public. However, your actions cannot be linked to your name (unless you explicitly try to double spend)\n\n" +
            "To protect personal data, you are assigned the following name:",
    "🙋",
    isUserNamePage = true
)
val urlPage = Page(
    "Custom Server",
    "You can change the incentive-system server (only for advanced users).",
    "📡",
    isServerUrlPage = true
)
val finalPage = Page(
    "Designed for Privacy",
    "We protect your privacy data by only disclosing minimal data and hiding your identity. Powered by cryptography!",
    "🛡"
)

val onboardingPages = listOf(
    shoppingPage,
    collectPage,
    rewardsPage,
    registrationPage,
    urlPage,
    finalPage
)

@Composable
fun OnboardingScreen(navigateToApp: () -> Unit = {}) {
    val onboardingViewModel = hiltViewModel<OnboardingViewModel>()
    val name = onboardingViewModel.name
    val serverUrl by onboardingViewModel.serverUrl.collectAsState()
    val serverUrlValid by onboardingViewModel.serverUrlValid.collectAsState()

    val done = {
        onboardingViewModel.storeData()
        navigateToApp()
    }

    OnboardingScreen(serverUrlValid, name, serverUrl, onboardingViewModel::setServerUrl, done)
}

@Composable
@OptIn(ExperimentalPagerApi::class, ExperimentalComposeUiApi::class)
private fun OnboardingScreen(
    serverUrlValid: Boolean,
    name: String,
    serverUrl: String,
    setServerUrl: (String) -> Unit,
    done: () -> Unit
) {
    Scaffold {
        Column(modifier = Modifier.padding(it)) {
            val coroutineScope = rememberCoroutineScope()
            val keyboardController = LocalSoftwareKeyboardController.current
            val pagerState = rememberPagerState()

            HorizontalPager(
                count = onboardingPages.size,
                state = pagerState,
                userScrollEnabled = !(pagerState.currentPage == 4 && !serverUrlValid) && !(pagerState.currentPage == 3 && name == ""),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { i ->
                val page = onboardingPages[i]
                OnboardingPage(page = page) {
                    if (page.isUserNamePage) {
                        Text(
                            name,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.bodyLarge,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    if (page.isServerUrlPage) {
                        OutlinedTextField(
                            serverUrl,
                            setServerUrl,
                            singleLine = true,
                            placeholder = { Text("Type the server url") },
                            keyboardActions = KeyboardActions(onDone = {
                                keyboardController?.hide()
                                coroutineScope.launch {
                                    pagerState.animateScrollToPage(i + 1)
                                }
                            }),
                            isError = !serverUrlValid,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp)
                        )
                    }
                }
            }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(16.dp),
                activeColor = MaterialTheme.colorScheme.onBackground
            )
            AnimatedVisibility(visible = pagerState.currentPage == onboardingPages.lastIndex) {
                Button(
                    onClick = done,
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
@Preview(uiMode = UI_MODE_NIGHT_YES)
@Composable
private fun OnboardingScreenPreview() {
    CryptimeleonPreviewContainer {
        OnboardingScreen(true, generateName(), BuildConfig.SERVER_URL, {}, {})
    }
}
