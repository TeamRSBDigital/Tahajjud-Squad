package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel

enum class AmalNavDestination(
    val route: String,
    val titleBn: String,
    val titleEn: String,
    val icon: ImageVector
) {
    HOME("home", "হোম", "Home", Icons.Default.Home),
    PRAYER("prayer", "সালাত", "Prayer", Icons.Default.AccessTimeFilled),
    TAHAJJUD("tahajjud", "তাহাজ্জুদ", "Tahajjud", Icons.Default.NightsStay),
    AMAL("amal", "আমল", "Amal", Icons.Default.MenuBook),
    SQUAD("squad", "স্কোয়াড", "Squad", Icons.Default.Group),
    DHIKR("dhikr", "যিকির", "Dhikr", Icons.Default.Fingerprint),
    RECORDS("records", "রেকর্ড", "Records", Icons.Default.Insights),
    PROFILE("profile", "প্রোফাইল", "Profile", Icons.Default.Person),
    SUPER_ADMIN("super_admin", "অ্যাডমিন", "Admin", Icons.Default.AdminPanelSettings),
    SMART_ALARM_SETTINGS("alarm_settings", "মিশন সেটিংস", "Mission Settings", Icons.Default.Tune),
    ALARM_MISSION("alarm_mission", "মিশন", "Mission", Icons.Default.AlarmOn),
    QR_MISSION_SCANNER("qr_mission_scanner", "কিউআর স্ক্যানার", "QR Scanner", Icons.Default.QrCodeScanner)
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialDestination = intent?.getStringExtra("NAVIGATE_TO") ?: "home"

        setContent {
            AmalTrackerTheme {
                AmalAppRoot(initialRoute = initialDestination)
            }
        }
    }
}

@Composable
fun AmalAppRoot(initialRoute: String) {
    val viewModel: AmalViewModel = viewModel()
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val userMessage by viewModel.userMessage.collectAsState()
    val isAlarmRinging by viewModel.isAlarmRinging.collectAsState()

    var currentScreen by remember {
        mutableStateOf(
            when (initialRoute) {
                "alarm_mission" -> AmalNavDestination.ALARM_MISSION
                "qr_mission_scanner" -> AmalNavDestination.QR_MISSION_SCANNER
                "alarm_settings" -> AmalNavDestination.SMART_ALARM_SETTINGS
                "tahajjud" -> AmalNavDestination.TAHAJJUD
                "squad" -> AmalNavDestination.SQUAD
                "amal" -> AmalNavDestination.AMAL
                else -> AmalNavDestination.HOME
            }
        )
    }

    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ ->
        // Gracefully handled; alarm scheduler handles either state
    }

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

    LaunchedEffect(userMessage) {
        userMessage?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearUserMessage()
        }
    }

    // BackHandler for secondary screens (prevent accidental back press during alarm ringing)
    if (currentScreen != AmalNavDestination.HOME && currentScreen != AmalNavDestination.ALARM_MISSION) {
        BackHandler {
            if (currentScreen == AmalNavDestination.SMART_ALARM_SETTINGS) {
                currentScreen = AmalNavDestination.TAHAJJUD
            } else {
                currentScreen = AmalNavDestination.HOME
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (currentScreen != AmalNavDestination.SUPER_ADMIN &&
                currentScreen != AmalNavDestination.ALARM_MISSION &&
                currentScreen != AmalNavDestination.SMART_ALARM_SETTINGS
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(32.dp),
                        color = MinimalWhiteSurface,
                        border = BorderStroke(1.dp, MinimalWhiteCardBorder),
                        shadowElevation = 6.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 4.dp, vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val mainTabs = listOf(
                                AmalNavDestination.HOME,
                                AmalNavDestination.PRAYER,
                                AmalNavDestination.TAHAJJUD,
                                AmalNavDestination.AMAL,
                                AmalNavDestination.SQUAD,
                                AmalNavDestination.DHIKR,
                                AmalNavDestination.RECORDS,
                                AmalNavDestination.PROFILE
                            )

                            mainTabs.forEach { destination ->
                                val isSelected = currentScreen == destination
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(16.dp))
                                        .clickable { currentScreen = destination }
                                        .padding(horizontal = 3.dp, vertical = 3.dp)
                                        .testTag("nav_${destination.route}")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier
                                            .size(34.dp)
                                            .clip(CircleShape)
                                            .background(if (isSelected) EmeraldDark else Color.Transparent)
                                    ) {
                                        Icon(
                                            imageVector = destination.icon,
                                            contentDescription = destination.titleEn,
                                            tint = if (isSelected) Color.White else Color(0xFF64748B),
                                            modifier = Modifier.size(19.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (isBengali) destination.titleBn else destination.titleEn,
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 9.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) EmeraldDark else Color(0xFF64748B)
                                        ),
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentScreen) {
                AmalNavDestination.HOME -> HomeScreen(
                    viewModel = viewModel,
                    onNavigateToPrayer = { currentScreen = AmalNavDestination.PRAYER },
                    onNavigateToTahajjud = { currentScreen = AmalNavDestination.TAHAJJUD },
                    onNavigateToDhikr = { currentScreen = AmalNavDestination.DHIKR },
                    onNavigateToAmal = { currentScreen = AmalNavDestination.AMAL },
                    onNavigateToSquad = { currentScreen = AmalNavDestination.SQUAD },
                    onNavigateToRecords = { currentScreen = AmalNavDestination.RECORDS }
                )
                AmalNavDestination.PRAYER -> PrayerScreen(viewModel = viewModel)
                AmalNavDestination.TAHAJJUD -> TahajjudScreen(
                    viewModel = viewModel,
                    onNavigateToMissionSettings = { currentScreen = AmalNavDestination.SMART_ALARM_SETTINGS },
                    onNavigateToTestMission = { currentScreen = AmalNavDestination.ALARM_MISSION }
                )
                AmalNavDestination.SMART_ALARM_SETTINGS -> SmartAlarmSettingsScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AmalNavDestination.TAHAJJUD },
                    onTestMission = { currentScreen = AmalNavDestination.ALARM_MISSION }
                )
                AmalNavDestination.ALARM_MISSION -> AlarmMissionScreen(
                    viewModel = viewModel,
                    isTestMode = !isAlarmRinging,
                    onMissionCompleted = { currentScreen = AmalNavDestination.HOME },
                    onNavigateToTahajjudEntry = { currentScreen = AmalNavDestination.TAHAJJUD }
                )
                AmalNavDestination.AMAL -> AmalTrackerScreen(viewModel = viewModel)
                AmalNavDestination.SQUAD -> SquadScreen(viewModel = viewModel)
                AmalNavDestination.DHIKR -> DhikrScreen(viewModel = viewModel)
                AmalNavDestination.RECORDS -> RecordsScreen(
                    viewModel = viewModel,
                    onNavigateToSuperAdmin = { currentScreen = AmalNavDestination.SUPER_ADMIN }
                )
                AmalNavDestination.PROFILE -> ProfileScreen(
                    viewModel = viewModel,
                    onNavigateToSuperAdmin = { currentScreen = AmalNavDestination.SUPER_ADMIN }
                )
                AmalNavDestination.SUPER_ADMIN -> SuperAdminScreen(
                    viewModel = viewModel,
                    onNavigateBack = { currentScreen = AmalNavDestination.HOME }
                )
            }
        }
    }
}
