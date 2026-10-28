package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.SharingConsentScope
import com.example.model.Squad
import com.example.model.SquadRole
import com.example.ui.components.ArabicGeometricPatternDecoration
import com.example.ui.components.IslamicCard
import com.example.ui.components.IslamicTopHeader
import com.example.ui.theme.*
import com.example.viewmodel.AmalViewModel

@Composable
fun SquadScreen(
    viewModel: AmalViewModel,
    modifier: Modifier = Modifier
) {
    val userProfile by viewModel.userProfile.collectAsState()
    val isBengali = userProfile.language == "bn"
    val squads by viewModel.allSquads.collectAsState()
    val currentSquadId = userProfile.activeSquadId

    var showCreateSquadDialog by remember { mutableStateOf(false) }
    var showJoinSquadDialog by remember { mutableStateOf(false) }
    var newSquadName by remember { mutableStateOf("") }
    var newSquadDesc by remember { mutableStateOf("") }
    var newSquadPrivate by remember { mutableStateOf(true) }
    var joinInviteCode by remember { mutableStateOf("") }

    val activeSquad = squads.find { it.id == currentSquadId }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentPadding = PaddingValues(bottom = 96.dp)
    ) {
        item {
            IslamicTopHeader(
                title = if (isBengali) "তাহাজ্জুদ ও আমল স্কোয়াড" else "Tahajjud & Amal Squad",
                subtitle = if (isBengali) "দ্বীনি সহমর্মিতা ও যৌথ অনুপ্রেরণা পরিষদ" else "Private community vigil & aggregate progress",
                isBengali = isBengali,
                onLanguageToggle = { viewModel.setLanguage(if (isBengali) "en" else "bn") }
            )
        }

        // Granular Sharing Privacy Mode Banner
        item {
            Spacer(modifier = Modifier.height(14.dp))
            IslamicCard(
                backgroundColor = MinimalWhiteSurface,
                borderColor = GoldPrimary.copy(alpha = 0.45f),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(GoldSoft)
                    ) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = GoldDark, modifier = Modifier.size(18.dp))
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isBengali) "আপনার স্কোয়াড শেয়ারিং মোড" else "Your Squad Sharing Mode",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextDarkPrimary)
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = userProfile.consentScope.descriptionBn,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                )
                Spacer(modifier = Modifier.height(10.dp))

                // 3 Options Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    SharingConsentScope.values().forEach { scope ->
                        val isSel = userProfile.consentScope == scope
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isSel) EmeraldPrimary else MinimalWhiteSurface,
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (isSel) EmeraldPrimary else MinimalWhiteCardBorder),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { viewModel.setSharingConsentScope(scope) }
                        ) {
                            Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
                                Text(
                                    text = if (isBengali) scope.labelBn.split(" ")[0] else scope.labelEn,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSel) Color.White else TextDarkPrimary,
                                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Active Squad Card
        if (activeSquad != null) {
            item {
                Spacer(modifier = Modifier.height(14.dp))
                IslamicCard(
                    backgroundColor = MinimalWhiteSurface,
                    borderColor = GoldPrimary.copy(alpha = 0.45f),
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(GoldSoft)
                                ) {
                                    Icon(Icons.Default.Group, contentDescription = null, tint = GoldDark, modifier = Modifier.size(20.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = activeSquad.name,
                                    style = MaterialTheme.typography.titleMedium.copy(color = TextDarkPrimary, fontWeight = FontWeight.Bold)
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = GoldSoft,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder)
                            ) {
                                Text(
                                    text = "কোড: ${activeSquad.inviteCode}",
                                    style = MaterialTheme.typography.labelSmall.copy(color = GoldDark, fontWeight = FontWeight.Bold),
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = activeSquad.description,
                            style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary)
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Squad Aggregate Summary
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MinimalWhiteSurfaceVariant,
                            border = androidx.compose.foundation.BorderStroke(1.dp, MinimalWhiteCardBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("${activeSquad.memberCount}", style = MaterialTheme.typography.titleMedium.copy(color = EmeraldDark, fontWeight = FontWeight.Bold))
                                    Text("সদস্য সংখ্যা", style = MaterialTheme.typography.labelSmall.copy(color = TextDarkSecondary, fontSize = 10.sp))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("86%", style = MaterialTheme.typography.titleMedium.copy(color = StatusPerformed, fontWeight = FontWeight.Bold))
                                    Text("ফজর জামাত হার", style = MaterialTheme.typography.labelSmall.copy(color = TextDarkSecondary, fontSize = 10.sp))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("24", style = MaterialTheme.typography.titleMedium.copy(color = GoldDark, fontWeight = FontWeight.Bold))
                                    Text("তাহাজ্জুদ রাত", style = MaterialTheme.typography.labelSmall.copy(color = TextDarkSecondary, fontSize = 10.sp))
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { viewModel.leaveSquad(activeSquad.id) },
                                colors = ButtonDefaults.textButtonColors(contentColor = StatusMissed)
                            ) {
                                Text("স্কোয়াড ত্যাগ করুন", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold))
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Join or Create Squad
        item {
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showCreateSquadDialog = true },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("create_squad_btn"),
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "নতুন স্কোয়াড" else "Create Squad")
                }

                OutlinedButton(
                    onClick = { showJoinSquadDialog = true },
                    modifier = Modifier.weight(1f).height(48.dp).testTag("join_squad_btn"),
                    border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldPrimary)
                ) {
                    Icon(Icons.Default.GroupAdd, contentDescription = null, tint = EmeraldPrimary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (isBengali) "কোড দিয়ে যোগ দিন" else "Join with Code", color = EmeraldPrimary)
                }
            }
        }

        // Available Community Squads List
        item {
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = if (isBengali) "জনপ্রিয় দ্বীনি স্কোয়াডসমূহ (Community Squads)" else "Community Squads",
                style = MaterialTheme.typography.titleMedium.copy(color = EmeraldDark, fontWeight = FontWeight.Bold),
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(squads) { squad ->
            IslamicCard(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(squad.name, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, color = EmeraldDark))
                        Text(squad.description, style = MaterialTheme.typography.bodySmall.copy(color = TextDarkSecondary), maxLines = 2)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text("সদস্য: ${squad.memberCount} জন • আমন্ত্রণ কোড: ${squad.inviteCode}", style = MaterialTheme.typography.labelSmall.copy(color = GoldPrimary, fontWeight = FontWeight.Bold))
                    }

                    if (squad.id != currentSquadId) {
                        Button(
                            onClick = { viewModel.joinSquad(squad.inviteCode) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldDark)
                        ) {
                            Text(if (isBengali) "যুক্ত হন" else "Join")
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = StatusPerformedLight
                        ) {
                            Text("যুক্ত আছেন", style = MaterialTheme.typography.labelSmall.copy(color = StatusPerformed, fontWeight = FontWeight.Bold), modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                        }
                    }
                }
            }
        }
    }

    // Create Squad Dialog
    if (showCreateSquadDialog) {
        AlertDialog(
            onDismissRequest = { showCreateSquadDialog = false },
            title = { Text(if (isBengali) "নতুন স্কোয়াড গঠন করুন" else "Create New Squad") },
            text = {
                Column {
                    OutlinedTextField(
                        value = newSquadName,
                        onValueChange = { newSquadName = it },
                        label = { Text("স্কোয়াডের নাম") },
                        placeholder = { Text("যেমন: ফজরের কাফেলা") },
                        modifier = Modifier.fillMaxWidth().testTag("new_squad_name_input"),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newSquadDesc,
                        onValueChange = { newSquadDesc = it },
                        label = { Text("বিবরণ ও উদ্দেশ্য") },
                        modifier = Modifier.fillMaxWidth().testTag("new_squad_desc_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newSquadName.isNotBlank()) {
                            viewModel.createSquad(newSquadName, newSquadDesc, newSquadPrivate)
                            newSquadName = ""
                            newSquadDesc = ""
                            showCreateSquadDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBengali) "তৈরি করুন" else "Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCreateSquadDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }

    // Join Squad Dialog
    if (showJoinSquadDialog) {
        AlertDialog(
            onDismissRequest = { showJoinSquadDialog = false },
            title = { Text(if (isBengali) "আমন্ত্রণ কোড দিয়ে যোগ দিন" else "Join Squad with Invite Code") },
            text = {
                OutlinedTextField(
                    value = joinInviteCode,
                    onValueChange = { joinInviteCode = it.uppercase() },
                    label = { Text("ইনভাইট কোড (যেমন: FAJR2026)") },
                    modifier = Modifier.fillMaxWidth().testTag("join_squad_code_input"),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (joinInviteCode.isNotBlank()) {
                            viewModel.joinSquad(joinInviteCode)
                            joinInviteCode = ""
                            showJoinSquadDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                ) {
                    Text(if (isBengali) "যোগ দিন" else "Join")
                }
            },
            dismissButton = {
                TextButton(onClick = { showJoinSquadDialog = false }) {
                    Text(if (isBengali) "বাতিল" else "Cancel")
                }
            }
        )
    }
}
