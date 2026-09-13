package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.service.FcmNotificationManager
import com.example.ui.PredictionViewModel
import com.example.ui.components.NotificationCenterSheet
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
    private var initialDestination = AppDestination.SITES

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        if (intent?.getStringExtra(FcmNotificationManager.EXTRA_NAVIGATE_TO) == FcmNotificationManager.DESTINATION_TESTING) {
            initialDestination = AppDestination.TESTING
        }

        setContent {
            MyApplicationTheme {
                MainApp(initialDestination = initialDestination)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp(
    viewModel: PredictionViewModel = viewModel(),
    initialDestination: AppDestination = AppDestination.SITES
) {
    val context = LocalContext.current
    var currentDestination by remember { mutableStateOf(initialDestination) }
    var showNotificationSheet by remember { mutableStateOf(false) }
    val unreadCount by viewModel.unreadNotificationCount.collectAsState()

    // Request POST_NOTIFICATIONS permission on Android 13+ (API 33+)
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Permission result handled */ }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

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
                actions = {
                    IconButton(
                        onClick = { showNotificationSheet = true },
                        modifier = Modifier.testTag("open_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadCount > 0) {
                                    Badge(
                                        containerColor = Color(0xFFFF5252),
                                        contentColor = Color.White
                                    ) {
                                        Text("$unreadCount")
                                    }
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.Notifications,
                                contentDescription = "Best Pick Alerts & Push Notifications",
                                tint = if (unreadCount > 0) Color(0xFF00E5FF) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
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
        Box(
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

        if (showNotificationSheet) {
            NotificationCenterSheet(
                viewModel = viewModel,
                onDismiss = { showNotificationSheet = false },
                onNavigateToBestPicks = {
                    currentDestination = AppDestination.TESTING
                }
            )
        }
    }
}

