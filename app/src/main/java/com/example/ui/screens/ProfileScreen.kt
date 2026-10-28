package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.IslamicCard
import com.example.ui.components.IslamicTopHeader
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel

@Composable
fun ProfileScreen(
    viewModel: AmalViewModel,
    onNavigateToSuperAdmin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"

    var showWipeConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyNoticeDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            IslamicTopHeader(
                title = if (isBengali) "প্রোফাইল ও সেটিংস" else "Profile & Settings",
                subtitle = if (isBengali) "ব্যবহারকারী অ্যাকাউন্ট, সম্মতি ও গোপনীয়তা" else "Account preferences, consent & privacy",
                isBengali = isBengali,
                onLanguageToggle = {
                    viewModel.setLanguage(if (isBengali) "en" else "bn")
                }
            )
        }

        // Account Details Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            IslamicCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(EmeraldPrimary)
                    ) {
                        Text(
                            text = userProfile.displayName.take(1),
                            style = MaterialTheme.typography.titleLarge.copy(color = GoldAccent, fontWeight = FontWeight.Bold)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = userProfile.displayName,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                        )
                        Text(
                            text = userProfile.email.ifEmpty { "signed-in with Google" },
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                        )
                        Text(
                            text = "UID: ${userProfile.uid}",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextDarkMuted, fontSize = 10.sp)
                        )
                    }
                }
            }
        }

        // Language & Localization Settings
        item {
            Spacer(modifier = Modifier.height(14.dp))
            IslamicCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (isBengali) "ভাষা নির্বাচন (Language)" else "Language Selection",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    FilterChip(
                        selected = isBengali,
                        onClick = { viewModel.setLanguage("bn") },
                        label = { Text("বাংলা (ডিফল্ট)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f).testTag("select_bn_lang_chip")
                    )

                    FilterChip(
                        selected = !isBengali,
                        onClick = { viewModel.setLanguage("en") },
                        label = { Text("English (Optional)") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = EmeraldPrimary, selectedLabelColor = Color.White),
                        modifier = Modifier.weight(1f).testTag("select_en_lang_chip")
                    )
                }
            }
        }

        // Tahajjud Reminder Preferences
        item {
            Spacer(modifier = Modifier.height(14.dp))
            IslamicCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (isBengali) "তাহাজ্জুদ রিমাইন্ডার সেটিংস" else "Tahajjud Reminder Settings",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "ফজরের ৩০ মিনিট আগে অ্যালার্ম" else "Alarm 30m before Fajr",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold)
                        )
                        Text(
                            text = if (isBengali) "অ্যাস্ট্রোনমিক্যাল ফজরের শিডিউল অনুযায়ী অ্যালার্ম বাজবে" else "Scheduled via astronomical prayer times",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                        )
                    }

                    Switch(
                        checked = userProfile.tahajjudAlarmEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.repository.updateAlarmPreferences(enabled, 30)
                            viewModel.refreshPrayerSchedule()
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary, checkedTrackColor = EmeraldSoftBg)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedButton(
                    onClick = { viewModel.testTahajjudAlarm() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "টেস্ট অ্যালার্ম নোটিফিকেশন প্রেরণ করুন" else "Send Test Alarm Notification")
                }
            }
        }

        // Privacy & Google Sheets Consent Card
        item {
            Spacer(modifier = Modifier.height(14.dp))
            IslamicCard(
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.VerifiedUser, contentDescription = null, tint = EmeraldPrimary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isBengali) "আমল পর্যবেক্ষণ ও সম্মতি" else "Data Collection & Consent",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                        )
                    }

                    Switch(
                        checked = userProfile.consentGiven,
                        onCheckedChange = { viewModel.setConsent(it) },
                        colors = SwitchDefaults.colors(checkedThumbColor = EmeraldPrimary, checkedTrackColor = EmeraldSoftBg),
                        modifier = Modifier.testTag("consent_toggle_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = if (isBengali) "শেয়ারিং প্রাইভেসি স্তর নির্বাচন করুন:" else "Select Sharing Privacy Level:",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                )
                Spacer(modifier = Modifier.height(6.dp))

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    com.example.model.SharingConsentScope.values().forEach { scope ->
                        val isSel = userProfile.consentScope == scope
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) EmeraldSoftBg else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldPrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable { viewModel.setSharingConsentScope(scope) }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSel,
                                    onClick = { viewModel.setSharingConsentScope(scope) },
                                    colors = RadioButtonDefaults.colors(selectedColor = EmeraldPrimary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = if (isBengali) scope.labelBn else scope.labelEn,
                                        style = MaterialTheme.typography.titleSmall.copy(
                                            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                            color = EmeraldDark
                                        )
                                    )
                                    Text(
                                        text = scope.descriptionBn,
                                        style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary, fontSize = 11.sp)
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                TextButton(
                    onClick = { showPrivacyNoticeDialog = true },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (isBengali) "পূর্ণাঙ্গ গোপনীয়তা নীতি পড়ুন" else "Read Privacy Notice", color = EmeraldPrimary)
                }
            }
        }

        // Super Admin Access Entry
        item {
            Spacer(modifier = Modifier.height(14.dp))
            IslamicCard(
                backgroundColor = IvorySurfaceVariant,
                borderColor = GoldPrimary.copy(alpha = 0.5f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isBengali) "সুপার অ্যাডমিন প্ল্যাটফর্ম" else "Super Admin Platform",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = EmeraldDark)
                        )
                        Text(
                            text = if (isBengali) "সকল ইউজারের সেন্ট্রাল ড্যাশবোর্ড ও গুগল শিট" else "Central monitoring & Google Sheets",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                        )
                    }

                    Button(
                        onClick = onNavigateToSuperAdmin,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                    ) {
                        Text(if (isBengali) "প্রবেশ" else "Open")
                    }
                }
            }
        }

        // Account Data Wipe (Danger Zone)
        item {
            Spacer(modifier = Modifier.height(16.dp))
            IslamicCard(
                backgroundColor = StatusMissedLight.copy(alpha = 0.5f),
                borderColor = StatusMissed.copy(alpha = 0.4f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = if (isBengali) "অ্যাকাউন্ট ডেটা মুছে ফেলা (Data Wipe)" else "Account Data Deletion",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = StatusMissed)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (isBengali)
                        "আপনার সংরক্ষিত সমস্ত সালাত, তাহাজ্জুদ ও যিকির রেকর্ড স্থায়ীভাবে মুছে ফেলতে পারবেন।"
                    else
                        "Permanently delete all your prayer, tahajjud, and dhikr records from this device.",
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                )
                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = { showWipeConfirmDialog = true },
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusMissed),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusMissed),
                    modifier = Modifier.fillMaxWidth().testTag("wipe_all_data_btn")
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusMissed)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "সমস্ত আমল রেকর্ড মুছে ফেলুন" else "Delete All Worship Records")
                }
            }
        }
    }

    // Privacy Notice Dialog
    if (showPrivacyNoticeDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyNoticeDialog = false },
            title = { Text(if (isBengali) "গোপনীয়তা ও তথ্য নীতি" else "Privacy Notice & Data Disclosure") },
            text = {
                LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                    item {
                        Text(
                            text = if (isBengali) """
                            ১. কী তথ্য সংরক্ষিত হয়:
                            আপনার পাঁচ ওয়াক্ত সালাতের স্ট্যাটাস (আদায়কৃত, কাযা, ছুটে গেছে), তাহাজ্জুদ আদায়ের সংখ্যা ও রাকাত, এবং ডিজিটাল তাসবীহ গণনার পরিসংখ্যান।

                            ২. তথ্যের নিরাপত্তা ও প্রবেশাধিকার:
                            ব্যবহারকারীর ব্যক্তিগত আমলের তথ্য কখনোই জনসমক্ষে উন্মুক্ত করা হয় না। কেবল আপনার সম্মতি সাপেক্ষে অনুমোদিত সুপার অ্যাডমিনগণ সেন্ট্রাল গুগল শিটে সামগ্রিক রিপোর্ট নিরীক্ষণ করতে পারেন।

                            ৩. গুগল শিট সিঙ্ক ব্যবস্থা:
                            গুগল অ্যাপস স্ক্রিপ্ট ওয়েব অ্যাপ্লিকেশন এপিআই-এর মাধ্যমে সিক্রেট টোকেন ভিত্তিক নিরাপদ এন্ডপয়েন্টে ডেটা আদান-প্রদান করা হয়।

                            ৪. সম্মতি প্রত্যাহার ও ডেটা মুছনের অধিকার:
                            যেকোনো সময় আপনি উপরের টগল বন্ধ করে সম্মতি প্রত্যাহার করতে পারেন এবং নিচের ডিলিট বাটন চেপে সকল ডেটা নিশ্চিহ্ন করতে পারেন।
                        """.trimIndent()
                            else """
                            1. Data Collected:
                            Daily five prayer status (Performed, Qaza, Missed), Tahajjud sessions, rak'ahs, and tasbih counts.

                            2. Security & Access:
                            Worship records are strictly private and never public. Only with explicit consent, authorized Super Admins may review reporting replicas in private Google Sheets.

                            3. Central Reporting Replica:
                            Secure token-authenticated Apps Script endpoints process sync requests.

                            4. Consent Withdrawal & Deletion:
                            You may withdraw consent at any time or permanently delete all your records.
                        """.trimIndent(),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyNoticeDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBengali) "বুঝেছি" else "Understood")
                }
            }
        )
    }

    // Wipe Confirmation Dialog
    if (showWipeConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showWipeConfirmDialog = false },
            title = { Text(if (isBengali) "রেকর্ড মুছে ফেলা নিশ্চিত করুন" else "Confirm Data Deletion") },
            text = {
                Text(
                    text = if (isBengali)
                        "আপনি কি নিশ্চিত যে আপনার সমস্ত পাঁচ ওয়াক্ত সালাত, তাহাজ্জুদ এবং যিকির রেকর্ড স্থায়ীভাবে মুছে ফেলতে চান? এই পরিবর্তনটি পরবর্তীতে আর ফিরিয়ে আনা যাবে না।"
                    else
                        "Are you sure you want to permanently delete all prayer, tahajjud, and dhikr records? This action cannot be undone."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeAllData()
                        showWipeConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusMissed)
                ) {
                    Text(if (isBengali) "হ্যাঁ, মুছে ফেলুন" else "Yes, Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirmDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
