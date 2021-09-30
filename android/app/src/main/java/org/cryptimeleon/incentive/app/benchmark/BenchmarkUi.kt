package org.cryptimeleon.incentive.app.benchmark

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun BenchmarkUi(onUpClicked: () -> Unit) {

    val viewModel = hiltViewModel<BenchmarkViewModel>()
    val state by viewModel.state.observeAsState(
        BenchmarkState(
            state = BenchmarkViewState.NOT_STARTED,
            iteration = 0,
            benchmarkResult = null
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Benchmark") },
                navigationIcon = {
                    IconButton(onClick = onUpClicked) {
                        Icon(Icons.Filled.ArrowBack, "Up Icon")
                    }
                }
            )
        }
    ) {
        when (state.state) {
            BenchmarkViewState.NOT_STARTED -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Button(
                        onClick = { viewModel.runBenchmark() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Start Benchmark")
                    }
                }
            }
            BenchmarkViewState.FINISHED -> {
                state.benchmarkResult?.let {
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_SUBJECT, "Cryptimeleon Benchmark Result")
                        putExtra(Intent.EXTRA_TEXT, state.shareData())
                        type = "text/plain"
                    }
                    val shareIntent = Intent.createChooser(sendIntent, null)
                    val context = LocalContext.current

                    Box(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text("Benchmark Results", style = MaterialTheme.typography.h4)
                            BenchmarkResultItem(
                                title = "Issue-Join",
                                description = state.joinText ?: ""
                            )
                            BenchmarkResultItem(
                                title = "Credit-Earn",
                                description = state.earnText ?: ""
                            )
                            BenchmarkResultItem(
                                title = "Spend-Deduct",
                                description = state.spendText ?: ""
                            )
                            BenchmarkResultItem(
                                title = "Total",
                                description = state.totalText ?: ""
                            )
                        }
                        FloatingActionButton(
                            onClick = { context.startActivity(shareIntent) },
                            modifier = Modifier.align(
                                Alignment.BottomEnd
                            )
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = "Share Benchmark Results")
                        }
                    }
                }
            }
            else -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = state.stateText)
                }
            }
        }
    }
}

@Composable
fun BenchmarkResultItem(title: String, description: String) {
    Column {
        Text(title, style = MaterialTheme.typography.h5)
        Text(description, style = MaterialTheme.typography.body1)
    }
}
