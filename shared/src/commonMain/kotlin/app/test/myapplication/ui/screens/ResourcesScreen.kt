package app.test.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.test.myapplication.ui.components.ColorResourceCard
import app.test.myapplication.ui.viewmodel.ResourcesUiState
import app.test.myapplication.ui.viewmodel.ResourcesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResourcesScreen(
    viewModel: ResourcesViewModel = remember { ResourcesViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(12.dp))

        // Header & Page Switcher
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "ReqRes Colors",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "reqres.in/api/unknown",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = currentPage == 1,
                    onClick = { viewModel.loadResources(1) },
                    label = { Text("Page 1") }
                )
                FilterChip(
                    selected = currentPage == 2,
                    onClick = { viewModel.loadResources(2) },
                    label = { Text("Page 2") }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (val state = uiState) {
            is ResourcesUiState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Loading colors from ReqRes...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            is ResourcesUiState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Button(onClick = { viewModel.loadResources() }) {
                            Text("Retry")
                        }
                    }
                }
            }

            is ResourcesUiState.Success -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(
                        items = state.resources,
                        key = { it.id }
                    ) { colorItem ->
                        ColorResourceCard(resource = colorItem)
                    }
                }
            }
        }
    }
}
