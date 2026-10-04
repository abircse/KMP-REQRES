package app.test.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.test.myapplication.ui.components.*
import app.test.myapplication.ui.viewmodel.UsersUiState
import app.test.myapplication.ui.viewmodel.UsersViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UsersScreen(
    viewModel: UsersViewModel = remember { UsersViewModel() }
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val currentPage by viewModel.currentPage.collectAsState()
    val perPage by viewModel.perPage.collectAsState()
    val isInfiniteScroll by viewModel.isInfiniteScroll.collectAsState()
    val isLoadingNextPage by viewModel.isLoadingNextPage.collectAsState()
    val selectedUser by viewModel.selectedUser.collectAsState()
    val isCreateDialogOpen by viewModel.isCreateDialogOpen.collectAsState()
    val isEditDialogOpen by viewModel.isEditDialogOpen.collectAsState()
    val isSubmitting by viewModel.isSubmitting.collectAsState()
    val feedback by viewModel.feedback.collectAsState()

    val listState = rememberLazyListState()

    Scaffold(
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.showCreateDialog(true) },
                icon = { Text("+", style = MaterialTheme.typography.titleLarge) },
                text = { Text("Create User") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search users...") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Pagination Controls Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Mode Switcher: Page Nav vs Infinite Scroll
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    FilterChip(
                        selected = !isInfiniteScroll,
                        onClick = { viewModel.toggleInfiniteScroll(false) },
                        label = { Text("Pages") }
                    )
                    FilterChip(
                        selected = isInfiniteScroll,
                        onClick = { viewModel.toggleInfiniteScroll(true) },
                        label = { Text("Infinite") }
                    )
                }

                // Per-Page Size Selector
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Size:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    listOf(3, 6, 12).forEach { size ->
                        FilterChip(
                            selected = perPage == size,
                            onClick = { viewModel.setPerPage(size) },
                            label = { Text("$size") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Content Area
            when (val state = uiState) {
                is UsersUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Loading users (Page $currentPage)...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                is UsersUiState.Error -> {
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
                            Button(onClick = { viewModel.loadUsers() }) {
                                Text("Retry")
                            }
                        }
                    }
                }

                is UsersUiState.Success -> {
                    if (state.filteredUsers.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No users found.",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        // Page Navigator Bar (if not infinite scroll mode)
                        if (!isInfiniteScroll) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedButton(
                                    onClick = { viewModel.loadUsers(currentPage - 1) },
                                    enabled = currentPage > 1
                                ) {
                                    Text("‹ Prev")
                                }

                                Text(
                                    text = "Page $currentPage of ${state.totalPages} (${state.totalUsers} Total)",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold
                                )

                                OutlinedButton(
                                    onClick = { viewModel.loadUsers(currentPage + 1) },
                                    enabled = currentPage < state.totalPages
                                ) {
                                    Text("Next ›")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                        }

                        LazyColumn(
                            state = listState,
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 80.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            itemsIndexed(
                                items = state.filteredUsers,
                                key = { _, user -> user.id }
                            ) { index, user ->
                                // Trigger Infinite Scroll when reaching end of list
                                if (isInfiniteScroll && index == state.filteredUsers.size - 1 && currentPage < state.totalPages && !isLoadingNextPage) {
                                    SideEffect {
                                        viewModel.loadNextPage()
                                    }
                                }

                                UserCard(
                                    user = user,
                                    onClick = { viewModel.selectUser(user) }
                                )
                            }

                            if (isInfiniteScroll && isLoadingNextPage) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(16.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text("Loading page ${currentPage + 1}...")
                                        }
                                    }
                                }
                            } else if (isInfiniteScroll && currentPage >= state.totalPages) {
                                item {
                                    Text(
                                        text = "✓ Reached end of ReqRes users (${state.totalUsers} loaded)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Dialogs
    UserDetailDialog(
        user = selectedUser,
        onDismiss = { viewModel.selectUser(null) },
        onEdit = { viewModel.showEditDialog(true) },
        onDelete = { user -> viewModel.deleteUser(user.id) }
    )

    CreateUserDialog(
        isOpen = isCreateDialogOpen,
        isSubmitting = isSubmitting,
        onDismiss = { viewModel.showCreateDialog(false) },
        onSubmit = { name, job -> viewModel.createUser(name, job) }
    )

    EditUserDialog(
        isOpen = isEditDialogOpen,
        user = selectedUser,
        isSubmitting = isSubmitting,
        onDismiss = { viewModel.showEditDialog(false) },
        onSubmit = { id, name, job -> viewModel.updateUser(id, name, job) }
    )

    FeedbackDialog(
        feedback = feedback,
        onDismiss = { viewModel.clearFeedback() }
    )
}
