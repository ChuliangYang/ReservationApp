import android.widget.Toast
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.reservationapp.core.ui.ScheduleList
import com.example.reservationapp.ui.feature.providerSchedule.ProviderEventState
import com.example.reservationapp.ui.feature.providerSchedule.ProviderScheduleDetailViewModel
import com.example.reservationapp.ui.feature.providerSchedule.ProviderUIState
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderScheduleDetailScreen(
    viewModel: ProviderScheduleDetailViewModel = hiltViewModel(),
    navigateToScheduling: (providerId: Int) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val localContext = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.onEach {
            it?.let {
                when (it) {
                    is ProviderEventState.Warning -> {
                        Toast.makeText(localContext, it.message, Toast.LENGTH_SHORT).show()
                    }

                    is ProviderEventState.NavigateToScheduling -> {
                        navigateToScheduling(it.providerId)
                    }
                }
                viewModel.onEventHandled()
            }
        }.collect()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (uiState !is ProviderUIState.Loading) {
                        Text("Check Your Schedule")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        floatingActionButton = {
            if (uiState !is ProviderUIState.Loading) {
                FloatingActionButton(
                    onClick = { viewModel.deleteSchedule() },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Schedule")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .padding(8.dp)
                .fillMaxSize()
        ) {
            when (val currentUIState = uiState) {
                is ProviderUIState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                is ProviderUIState.Error -> {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = "Error: ${currentUIState.message}"
                    )
                }

                is ProviderUIState.Scheduled -> {
                    ScheduledScreen(currentUIState, viewModel)
                }
            }
        }
    }
}

@Composable
fun ScheduledScreen(
    uiState: ProviderUIState.Scheduled,
    viewModel: ProviderScheduleDetailViewModel,
    modifier: Modifier = Modifier,
) {
    ScheduleList(uiState.schedules)
}