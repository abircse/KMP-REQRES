package app.test.myapplication.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.test.myapplication.ui.viewmodel.ConsoleViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsoleScreen(
    viewModel: ConsoleViewModel = remember { ConsoleViewModel() }
) {
    val method by viewModel.method.collectAsState()
    val endpoint by viewModel.endpoint.collectAsState()
    val requestBody by viewModel.requestBody.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val response by viewModel.response.collectAsState()

    var showHeaders by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "ReqRes Interactive API Console",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = "Test direct REST API requests to https://reqres.in/api/",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Preset Templates Chips
        Text(
            text = "Preset Templates:",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterChip(
                selected = false,
                onClick = { viewModel.applyTemplate("GET", "users?page=2") },
                label = { Text("GET Users") }
            )
            FilterChip(
                selected = false,
                onClick = { viewModel.applyTemplate("GET", "users/2") },
                label = { Text("GET User #2") }
            )
            FilterChip(
                selected = false,
                onClick = {
                    viewModel.applyTemplate(
                        "POST",
                        "users",
                        "{\n  \"name\": \"morpheus\",\n  \"job\": \"leader\"\n}"
                    )
                },
                label = { Text("POST User") }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // HTTP Method Chips
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf("GET", "POST", "PUT", "DELETE", "PATCH").forEach { m ->
                FilterChip(
                    selected = method == m,
                    onClick = { viewModel.onMethodChanged(m) },
                    label = { Text(m, fontWeight = FontWeight.Bold) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = when (m) {
                            "GET" -> Color(0xFF10B981)
                            "POST" -> Color(0xFF3B82F6)
                            "PUT" -> Color(0xFFF59E0B)
                            "DELETE" -> Color(0xFFEF4444)
                            else -> Color(0xFF8B5CF6)
                        },
                        selectedLabelColor = Color.White
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Endpoint Input
        OutlinedTextField(
            value = endpoint,
            onValueChange = { viewModel.onEndpointChanged(it) },
            label = { Text("Endpoint Path / URL") },
            prefix = { Text("https://reqres.in/api/") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Request Body (if POST/PUT/PATCH)
        if (method in listOf("POST", "PUT", "PATCH")) {
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedTextField(
                value = requestBody,
                onValueChange = { viewModel.onRequestBodyChanged(it) },
                label = { Text("JSON Request Body") },
                minLines = 3,
                maxLines = 6,
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { viewModel.execute() },
            enabled = !isLoading && endpoint.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
            }
            Text("Send $method Request")
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Response View
        AnimatedVisibility(visible = response != null) {
            val res = response ?: return@AnimatedVisibility

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = when {
                                res.statusCode in 200..299 -> Color(0xFF10B981)
                                res.statusCode in 400..499 -> Color(0xFFF59E0B)
                                else -> Color(0xFFEF4444)
                            }
                        ) {
                            Text(
                                text = "Status: ${res.statusCode} ${res.statusMessage}",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }

                        Text(
                            text = "${res.durationMs} ms",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    TextButton(onClick = { showHeaders = !showHeaders }) {
                        Text(if (showHeaders) "Hide Response Headers" else "Show Response Headers")
                    }

                    if (showHeaders) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    MaterialTheme.colorScheme.surfaceVariant,
                                    RoundedCornerShape(8.dp)
                                )
                                .padding(12.dp)
                        ) {
                            res.headers.forEach { (key, values) ->
                                Text(
                                    text = "$key: ${values.joinToString()}",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    Text(
                        text = "Response Body:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    SelectionContainer {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = res.body.ifBlank { "(Empty Response Body)" },
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 13.sp,
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
