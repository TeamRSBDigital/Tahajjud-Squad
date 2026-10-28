package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UserProfile
import com.example.ui.components.ArabicGeometricPatternDecoration
import com.example.ui.components.IslamicCard
import com.example.ui.components.IslamicTopHeader
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SuperAdminScreen(
    viewModel: AmalViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val isSuperAdmin = userProfile.isSuperAdmin

    val isSyncing by viewModel.isSyncing.collectAsState()
    val lastSync by viewModel.lastSyncTimestamp.collectAsState()
    val syncLogs by viewModel.syncLogs.collectAsState()
    val auditLogs by viewModel.auditLogs.collectAsState()
    val appsScriptUrl by viewModel.appsScriptUrl.collectAsState()
    val syncToken by viewModel.syncSecretToken.collectAsState()

    var showConfigDialog by remember { mutableStateOf(false) }
    var configUrlInput by remember(appsScriptUrl) { mutableStateOf(appsScriptUrl) }
    var configTokenInput by remember(syncToken) { mutableStateOf(syncToken) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedUserDetail by remember { mutableStateOf<UserProfile?>(null) }

    // Mock authorized consented users directory for the administrative dashboard
    val consentedUsers = remember {
        listOf(
            UserProfile(
                uid = "usr_dhaka_01",
                displayName = "মুহাম্মদ আব্দুল্লাহ / M. Abdullah",
                email = "m.abdullah@example.com",
                timezone = "Asia/Dhaka",
                consentGiven = true
            ),
            UserProfile(
                uid = "usr_chittagong_02",
                displayName = "তানভীর আহমেদ / Tanvir Ahmed",
                email = "tanvir@example.com",
                timezone = "Asia/Dhaka",
                consentGiven = true
            ),
            UserProfile(
                uid = "usr_sylhet_03",
                displayName = "ফারহান ইসলাম / Farhan Islam",
                email = "farhan@example.com",
                timezone = "Asia/Dhaka",
                consentGiven = true
            )
        )
    }

    val filteredUsers = consentedUsers.filter {
        it.displayName.contains(searchQuery, ignoreCase = true) ||
                it.uid.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true)
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        // Admin Top Bar
        item {
            Surface(color = EmeraldDeep) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.testTag("admin_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = GoldAccent)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Security, contentDescription = null, tint = GoldAccent, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isBengali) "সুপার অ্যাডমিন প্ল্যাটফর্ম" else "Super Admin Platform",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    color = GoldAccent,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                        Text(
                            text = if (isBengali) "সুরক্ষিত মনিটরিং ও গুগল শিট সেন্ট্রাল রিপোর্টিং" else "Secure Multi-User Monitoring & Google Sheets",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextLightSecondary, fontSize = 11.sp)
                        )
                    }
                }
            }
        }

        // Access Gate Card (if not super admin)
        if (!isSuperAdmin) {
            item {
                Spacer(modifier = Modifier.height(20.dp))
                IslamicCard(
                    backgroundColor = StatusMissedLight,
                    borderColor = StatusMissed,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = StatusMissed, modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (isBengali) "ভূমিকা যাচাই প্রয়োজন (Role Restricted)" else "Role Restricted Access",
                        style = MaterialTheme.typography.titleMedium.copy(color = StatusMissed, fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = if (isBengali)
                            "এই ড্যাশবোর্ড কেবল অনুমোদিত সুপার অ্যাডমিনদের জন্য সংরক্ষিত। অ্যাডমিন অধিকার নিশ্চিত করতে নিচে টগল করুন:"
                        else
                            "This dashboard is strictly restricted to verified Super Admins. Enable admin access below:",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkPrimary)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = { viewModel.setSuperAdmin(true) },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier.fillMaxWidth().testTag("authorize_super_admin_btn")
                    ) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isBengali) "সুপার অ্যাডমিন অনুমতি সক্রিয় করুন" else "Authorize Super Admin Session")
                    }
                }
            }
        } else {
            // Authorized Admin Content
            item {
                Spacer(modifier = Modifier.height(14.dp))
                // Google Sheets Sync Hub Card
                IslamicCard(
                    backgroundColor = EmeraldDeep,
                    borderColor = GoldAccent.copy(alpha = 0.5f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        ArabicGeometricPatternDecoration(modifier = Modifier.matchParentSize())

                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CloudSync, contentDescription = null, tint = GoldAccent)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBengali) "গুগল শিট রিপোর্টিং হাব" else "Google Sheets Reporting Hub",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = GoldAccent,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                }

                                IconButton(onClick = { showConfigDialog = true }) {
                                    Icon(Icons.Default.Settings, contentDescription = "Config", tint = GoldSoft)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (lastSync > 0) {
                                    val sdf = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.getDefault())
                                    "সর্বশেষ সফল সিঙ্ক: ${sdf.format(Date(lastSync))}"
                                } else {
                                    if (isBengali) "এখনো গুগল শিটে সিঙ্ক করা হয়নি" else "No sync executed yet"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = TextLightSecondary, fontSize = 11.sp)
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            Button(
                                onClick = { viewModel.triggerGoogleSheetsSync() },
                                enabled = !isSyncing,
                                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                                modifier = Modifier.fillMaxWidth().height(48.dp).testTag("trigger_sheets_sync_btn")
                            ) {
                                if (isSyncing) {
                                    CircularProgressIndicator(color = EmeraldDeep, modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(if (isBengali) "গুগল শিটে সিঙ্ক হচ্ছে..." else "Syncing to Sheets...", color = EmeraldDeep)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, tint = EmeraldDeep)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isBengali) "এখনই গুগল শিটে সিঙ্ক করুন" else "Sync Now to Google Sheets",
                                        style = MaterialTheme.typography.labelLarge.copy(color = EmeraldDeep, fontWeight = FontWeight.Bold)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Aggregate Metrics Row
            item {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AdminMetricBox(title = "অনুমোদিত ইউজার", value = "${consentedUsers.size}", icon = Icons.Default.People, modifier = Modifier.weight(1f))
                    AdminMetricBox(title = "সালাত রেকর্ড", value = "1,420+", icon = Icons.Default.FactCheck, modifier = Modifier.weight(1f))
                    AdminMetricBox(title = "তাহাজ্জুদ রেট", value = "74%", icon = Icons.Default.NightsStay, modifier = Modifier.weight(1f))
                    AdminMetricBox(title = "সিঙ্ক স্ট্যাটাস", value = "OK", icon = Icons.Default.CheckCircle, modifier = Modifier.weight(1f))
                }
            }

            // Consented User Directory & Search
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = if (isBengali) "সম্মতিপ্রাপ্ত ব্যবহারকারী তালিকা (Consented Users)" else "Consented Users Directory",
                    style = MaterialTheme.typography.titleMedium.copy(color = EmeraldDark, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text(if (isBengali) "নাম বা UID দিয়ে খুঁজুন..." else "Search by name or UID...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).testTag("admin_user_search_input"),
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(filteredUsers) { user ->
                IslamicCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .clickable { selectedUserDetail = user }
                        .testTag("admin_user_item_${user.uid}")
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(user.displayName, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldDark))
                            Text(user.email, style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary))
                            Text("UID: ${user.uid} • ${user.timezone}", style = MaterialTheme.typography.labelSmall.copy(color = TextDarkMuted, fontSize = 10.sp))
                        }

                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusPerformedLight
                        ) {
                            Text(
                                text = "সম্মতিপ্রাপ্ত / Consented",
                                style = MaterialTheme.typography.labelSmall.copy(color = StatusPerformed, fontWeight = FontWeight.Bold),
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // Squad Oversight Section
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (isBengali) "কমিউনিটি স্কোয়াড পর্যবেক্ষণ (Squad Oversight)" else "Community Squad Oversight",
                    style = MaterialTheme.typography.titleMedium.copy(color = EmeraldDark, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            item {
                IslamicCard(
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Text(
                        text = "তাহাজ্জুদ স্কোয়াড আলফা (FAJR2026)",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                    )
                    Text(
                        text = "সদস্য: ১৮ জন • লিডার: admin_lead_01 • প্রাইভেসি: এনফোর্সড (নোটস গোপন)",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(shape = RoundedCornerShape(6.dp), color = StatusPerformedLight) {
                            Text("গড় উপস্থিতি: ৮৬%", style = MaterialTheme.typography.labelSmall.copy(color = StatusPerformed, fontWeight = FontWeight.Bold), modifier = Modifier.padding(4.dp))
                        }
                        Surface(shape = RoundedCornerShape(6.dp), color = GoldSoft) {
                            Text("রোলস: ১ লিডার, ২ মডারেটর", style = MaterialTheme.typography.labelSmall.copy(color = EmeraldDeep), modifier = Modifier.padding(4.dp))
                        }
                    }
                }
            }

            // Durable Sync Jobs Outbox
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isBengali) "ডিউরেবল সিঙ্ক আউটবক্স (Durable Sync Outbox)" else "Durable Sync Outbox",
                        style = MaterialTheme.typography.titleMedium.copy(color = EmeraldDark, fontWeight = FontWeight.Bold)
                    )
                    TextButton(onClick = { viewModel.triggerGoogleSheetsSync() }) {
                        Text("পুনরায় চেষ্টা (Retry)", color = EmeraldPrimary)
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            items(syncLogs.take(4)) { item ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.status == "SUCCESS") Icons.Default.CheckCircle else Icons.Default.Error,
                            contentDescription = null,
                            tint = if (item.status == "SUCCESS") StatusPerformed else StatusMissed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(item.message, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium))
                            Text("টাইপ: ${item.recordType} • রেকর্ড: ${item.recordCount}", style = MaterialTheme.typography.labelSmall.copy(color = TextDarkMuted, fontSize = 10.sp))
                        }
                    }
                }
            }

            // Administrative Audit Log Table
            item {
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = if (isBengali) "অ্যাডমিন অডিট লগ (Audit Trail)" else "Administrative Audit Trail",
                    style = MaterialTheme.typography.titleMedium.copy(color = EmeraldDark, fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(auditLogs.take(5)) { log ->
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.History, contentDescription = null, tint = GoldMuted, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(log.action, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldPrimary))
                                val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
                                Text(sdf.format(Date(log.timestamp)), style = MaterialTheme.typography.labelSmall.copy(color = TextDarkMuted))
                            }
                            Text(log.details, style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.sp))
                        }
                    }
                }
            }
        }
    }

    // Google Sheets Config Dialog
    if (showConfigDialog) {
        AlertDialog(
            onDismissRequest = { showConfigDialog = false },
            title = { Text(if (isBengali) "গুগল শিট স্ক্রিপ্ট কনফিগারেশন" else "Google Sheets Script Config") },
            text = {
                Column {
                    Text(
                        text = if (isBengali)
                            "আপনার ডিপ্লয় করা Google Apps Script Web App URL এবং গোপন সিক্রেট টোকেন দিন:"
                        else
                            "Enter your deployed Google Apps Script Web App URL and authentication token:",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = configUrlInput,
                        onValueChange = { configUrlInput = it },
                        label = { Text("Web App URL") },
                        modifier = Modifier.fillMaxWidth().testTag("config_script_url_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = configTokenInput,
                        onValueChange = { configTokenInput = it },
                        label = { Text("Secret Token") },
                        modifier = Modifier.fillMaxWidth().testTag("config_token_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Apps Script কোডটি প্রজেক্ট রুটের AmalTracker_AppsScript.js ফাইলে সংরক্ষিত আছে।",
                        style = MaterialTheme.typography.bodySmall.copy(color = GoldPrimary, fontSize = 11.sp)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateAppsScriptConfig(configUrlInput, configTokenInput)
                        showConfigDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBengali) "সংরক্ষণ" else "Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfigDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // User Detail Inspection Dialog
    if (selectedUserDetail != null) {
        val user = selectedUserDetail!!
        AlertDialog(
            onDismissRequest = { selectedUserDetail = null },
            title = { Text(user.displayName) },
            text = {
                Column {
                    Text("ইমেইল: ${user.email}")
                    Text("UID: ${user.uid}")
                    Text("টাইমজোন: ${user.timezone}")
                    Text("সম্মতি স্থিতি: ${if (user.consentGiven) "সম্মতিপ্রাপ্ত (Consented)" else "বাতিল"}")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "ইউজারের ৫ ওয়াক্ত সালাত ও তাহাজ্জুদ রেকর্ড গুগল শিট সেন্ট্রাল ডাটাবেজে নিয়মিত সিঙ্ক হচ্ছে।",
                        style = MaterialTheme.typography.bodySmall.copy(color = EmeraldDark)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { selectedUserDetail = null },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBengali) "ঠিক আছে" else "Close")
                }
            }
        )
    }
}

@Composable
fun AdminMetricBox(
    title: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark))
            Text(title, style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, color = TextDarkSecondary), maxLines = 1)
        }
    }
}
