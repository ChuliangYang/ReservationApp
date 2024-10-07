package com.example.reservationapp.ui.feature.availableProviders

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.ListItem
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.reservationapp.data.model.Provider

@Composable
fun ProviderListScreen(
    clientId: Int,
    onNavigateToReservation: (clientId: Int, providerId: Int) -> Unit,
    viewModel: ProviderListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    ProviderListScreenContent(
        uiState = uiState,
        onProviderSelected = { providerId ->
            onNavigateToReservation(clientId, providerId)
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProviderListScreenContent(
    uiState: ProviderListUiState,
    onProviderSelected: (providerId: Int) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Available Providers") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    ) { paddingValues ->
        when (uiState) {
            is ProviderListUiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ProviderListUiState.Success -> {
                ProviderList(
                    providerList = uiState.providerList,
                    onProviderSelected = onProviderSelected,
                    modifier = Modifier.padding(paddingValues)
                )
            }

            is ProviderListUiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = uiState.message, color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun ProviderList(
    providerList: List<Provider>,
    onProviderSelected: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(modifier = modifier.fillMaxSize()) {
        items(providerList) { provider ->
            ProviderListItem(provider, onProviderSelected)
            HorizontalDivider()
        }
    }
}

@Composable
fun ProviderListItem(provider: Provider, onProviderSelected: (Int) -> Unit) {
    ListItem(
        headlineContent = { Text(text = provider.description) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onProviderSelected(provider.id) }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
