package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.PredictionViewModel
import com.example.ui.screens.LiveSearchScreen
import com.example.ui.screens.SitesDirectoryScreen
import com.example.ui.screens.TestingLedgerScreen
import com.example.ui.screens.ThinkingAndChatScreen
import com.example.ui.theme.MyApplicationTheme

enum class AppDestination(val title: String, val icon: ImageVector, val tag: String) {
    SITES("Sites Audit", Icons.Default.Shield, "nav_sites"),
    TESTING("Taste & Test", Icons.Default.Leaderboard, "nav_testing"),
    LIVE_SEARCH("Live Intel", Icons.Default.TravelExplore, "nav_search"),
    THINKING_CHAT("AI & Thinking", Icons.Default.Psychology, "nav_thinking")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainApp()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(viewModel: PredictionViewModel = viewModel()) {
    var currentDestination by remember { mutableStateOf(AppDestination.SITES) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("main_app_scaffold"),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = currentDestination.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.testTag("bottom_nav_bar")
            ) {
                AppDestination.values().forEach { dest ->
                    NavigationBarItem(
                        selected = currentDestination == dest,
                        onClick = { currentDestination = dest },
                        icon = { Icon(dest.icon, contentDescription = dest.title) },
                        label = { Text(dest.title, fontSize = 11.sp, fontWeight = if (currentDestination == dest) FontWeight.Bold else FontWeight.Normal) },
                        modifier = Modifier.testTag(dest.tag)
                    )
                }
            }
        }
    ) { innerPadding ->
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentDestination) {
                AppDestination.SITES -> {
                    SitesDirectoryScreen(
                        viewModel = viewModel,
                        onNavigateToTesting = { currentDestination = AppDestination.TESTING },
                        onNavigateToThinking = { currentDestination = AppDestination.THINKING_CHAT },
                        onNavigateToLiveSearch = { query ->
                            viewModel.runSearchGroundedQuery(query)
                            currentDestination = AppDestination.LIVE_SEARCH
                        }
                    )
                }
                AppDestination.TESTING -> {
                    TestingLedgerScreen(viewModel = viewModel)
                }
                AppDestination.LIVE_SEARCH -> {
                    LiveSearchScreen(
                        viewModel = viewModel,
                        onNavigateToTesting = { currentDestination = AppDestination.TESTING }
                    )
                }
                AppDestination.THINKING_CHAT -> {
                    ThinkingAndChatScreen(viewModel = viewModel)
                }
            }
        }
    }
}

