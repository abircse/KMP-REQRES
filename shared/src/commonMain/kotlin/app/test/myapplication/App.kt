package app.test.myapplication

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

import app.test.myapplication.ui.screens.AuthScreen
import app.test.myapplication.ui.screens.ConsoleScreen
import app.test.myapplication.ui.screens.ResourcesScreen
import app.test.myapplication.ui.screens.UsersScreen
import app.test.myapplication.ui.theme.ReqResTheme

enum class NavigationTab(val title: String, val iconText: String) {
    USERS("Users", "👥"),
    RESOURCES("Colors", "🎨"),
    AUTH("Auth", "🔑"),
    CONSOLE("Console", "⚡")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    val systemDark = isSystemInDarkTheme()
    var isDarkMode by remember { mutableStateOf(systemDark) }
    var currentTab by remember { mutableStateOf(NavigationTab.USERS) }

    ReqResTheme(darkTheme = isDarkMode) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                text = "ReqRes Explorer",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "https://reqres.in/",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { isDarkMode = !isDarkMode }) {
                            Text(
                                text = if (isDarkMode) "☀️" else "🌙",
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 8.dp
                ) {
                    NavigationTab.entries.forEach { tab ->
                        NavigationBarItem(
                            selected = currentTab == tab,
                            onClick = { currentTab = tab },
                            icon = {
                                Text(
                                    text = tab.iconText,
                                    style = MaterialTheme.typography.titleMedium
                                )
                            },
                            label = { Text(tab.title) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    NavigationTab.USERS -> UsersScreen()
                    NavigationTab.RESOURCES -> ResourcesScreen()
                    NavigationTab.AUTH -> AuthScreen()
                    NavigationTab.CONSOLE -> ConsoleScreen()
                }
            }
        }
    }
}
