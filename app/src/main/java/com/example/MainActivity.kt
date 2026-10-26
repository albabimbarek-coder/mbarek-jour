package com.example

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.WorkTrackerViewModel
import com.example.ui.components.DayEditDialog
import com.example.ui.components.QuickReturnDialog
import com.example.ui.screens.FinancesScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.screens.StatsScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.util.AppLanguage
import com.example.util.LocalAppStrings

class MainActivity : ComponentActivity() {

    private val viewModel: WorkTrackerViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Handle notification deep link
        handleIntent(intent)

        setContent {
            val appLanguage by viewModel.appLanguage.collectAsState()
            val appTheme by viewModel.appTheme.collectAsState()

            MyApplicationTheme(
                themeMode = appTheme,
                language = appLanguage
            ) {
                val context = LocalContext.current
                val strings = LocalAppStrings.current
                val activeTab by viewModel.activeTab.collectAsState()
                val isEditorOpen by viewModel.isEditorOpen.collectAsState()
                val editingEntry by viewModel.editingEntry.collectAsState()
                val isQuickReturnOpen by viewModel.isQuickReturnOpen.collectAsState()
                val settings by viewModel.settings.collectAsState()

                // Request notification permission on Android 13+
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                    onResult = { /* Handled */ }
                )

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        val hasPermission = ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) == PackageManager.PERMISSION_GRANTED
                        if (!hasPermission) {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        }
                    }
                }

                // BackHandler: Navigate back to Home tab first
                BackHandler(enabled = activeTab != 0) {
                    viewModel.setActiveTab(0)
                }

                var languageMenuOpen by remember { mutableStateOf(false) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        CenterAlignedTopAppBar(
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Schedule,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = strings.appName,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            },
                            actions = {
                                // Theme Toggle Button
                                IconButton(
                                    onClick = {
                                        val nextTheme = when (appTheme) {
                                            "LIGHT" -> "DARK"
                                            "DARK" -> "LIGHT"
                                            else -> "DARK"
                                        }
                                        viewModel.setAppTheme(nextTheme)
                                    },
                                    modifier = Modifier.testTag("theme_toggle_button")
                                ) {
                                    Icon(
                                        imageVector = if (appTheme == "DARK") Icons.Default.LightMode else Icons.Default.DarkMode,
                                        contentDescription = "تبديل المظهر"
                                    )
                                }

                                // Language Switcher Dropdown
                                Box {
                                    IconButton(
                                        onClick = { languageMenuOpen = true },
                                        modifier = Modifier.testTag("language_menu_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Language,
                                            contentDescription = "اللغة"
                                        )
                                    }

                                    DropdownMenu(
                                        expanded = languageMenuOpen,
                                        onDismissRequest = { languageMenuOpen = false }
                                    ) {
                                        DropdownMenuItem(
                                            text = { Text("العربية (Arabic)") },
                                            onClick = {
                                                viewModel.setAppLanguage(AppLanguage.AR)
                                                languageMenuOpen = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("English") },
                                            onClick = {
                                                viewModel.setAppLanguage(AppLanguage.EN)
                                                languageMenuOpen = false
                                            }
                                        )
                                        DropdownMenuItem(
                                            text = { Text("Français") },
                                            onClick = {
                                                viewModel.setAppLanguage(AppLanguage.FR)
                                                languageMenuOpen = false
                                            }
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
                        WorkTrackerBottomBar(
                            activeTab = activeTab,
                            onTabSelected = { viewModel.setActiveTab(it) }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = activeTab,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "tab_animation"
                        ) { tab ->
                            when (tab) {
                                0 -> HomeScreen(viewModel = viewModel)
                                1 -> StatsScreen(viewModel = viewModel)
                                2 -> FinancesScreen(viewModel = viewModel)
                                3 -> ReportsScreen(viewModel = viewModel)
                            }
                        }

                        // Day Edit Dialog Modal
                        if (isEditorOpen && editingEntry != null) {
                            DayEditDialog(
                                initialEntry = editingEntry!!,
                                settings = settings,
                                onDismiss = { viewModel.closeEditor() },
                                onSave = { date, isWork, dayType, regHours, otHours1, otHours2, notes ->
                                    viewModel.saveEntry(date, isWork, dayType, regHours, otHours1, otHours2, notes)
                                },
                                onDelete = { entry ->
                                    viewModel.deleteEntry(entry)
                                }
                            )
                        }

                        // Quick Return From Work Dialog Modal
                        if (isQuickReturnOpen) {
                            QuickReturnDialog(
                                settings = settings,
                                onDismiss = { viewModel.closeQuickReturnDialog() },
                                onConfirm = { regHours, otHours1, otHours2, notes ->
                                    viewModel.quickLogTodayReturn(regHours, otHours1, otHours2, notes)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent?.getBooleanExtra("EXTRA_QUICK_LOG", false) == true) {
            viewModel.openQuickReturnDialog()
        }
    }
}

@Composable
fun WorkTrackerBottomBar(
    activeTab: Int,
    onTabSelected: (Int) -> Unit
) {
    val strings = LocalAppStrings.current

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = activeTab == 0,
            onClick = { onTabSelected(0) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 0) Icons.Default.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = strings.navHome
                )
            },
            label = {
                Text(
                    text = strings.navHome,
                    fontSize = 11.sp,
                    fontWeight = if (activeTab == 0) FontWeight.Bold else FontWeight.Normal
                )
            },
            modifier = Modifier.testTag("nav_home"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = activeTab == 1,
            onClick = { onTabSelected(1) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 1) Icons.Default.BarChart else Icons.Outlined.BarChart,
                    contentDescription = strings.navStats
                )
            },
            label = {
                Text(
                    text = strings.navStats,
                    fontSize = 11.sp,
                    fontWeight = if (activeTab == 1) FontWeight.Bold else FontWeight.Normal
                )
            },
            modifier = Modifier.testTag("nav_stats"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = activeTab == 2,
            onClick = { onTabSelected(2) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 2) Icons.Default.AccountBalanceWallet else Icons.Outlined.AccountBalanceWallet,
                    contentDescription = strings.navFinances
                )
            },
            label = {
                Text(
                    text = strings.navFinances,
                    fontSize = 11.sp,
                    fontWeight = if (activeTab == 2) FontWeight.Bold else FontWeight.Normal
                )
            },
            modifier = Modifier.testTag("nav_finances"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )

        NavigationBarItem(
            selected = activeTab == 3,
            onClick = { onTabSelected(3) },
            icon = {
                Icon(
                    imageVector = if (activeTab == 3) Icons.Default.Assessment else Icons.Outlined.Assessment,
                    contentDescription = strings.navReports
                )
            },
            label = {
                Text(
                    text = strings.navReports,
                    fontSize = 11.sp,
                    fontWeight = if (activeTab == 3) FontWeight.Bold else FontWeight.Normal
                )
            },
            modifier = Modifier.testTag("nav_reports"),
            colors = NavigationBarItemDefaults.colors(
                indicatorColor = MaterialTheme.colorScheme.primaryContainer
            )
        )
    }
}
