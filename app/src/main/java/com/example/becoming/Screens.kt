package com.example.becoming

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.becoming.ui.theme.*
import kotlinx.coroutines.delay

// --- UTILITY COMPOSABLES ---
@Composable
fun TypewriterText(text: String, style: androidx.compose.ui.text.TextStyle, color: Color) {
    var displayedText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        displayedText = ""
        for (i in text.indices) {
            displayedText += text[i]
            delay(35) // Typing speed
        }
    }
    Text(displayedText, style = style, color = color, textAlign = TextAlign.Center)
}

// --- SCREENS ---

@Composable
fun OnboardingScreen(onComplete: (String, String, String) -> Unit) {
    var step by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize().background(DeepInkBlack).padding(24.dp), contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = step, label = "onboarding") { targetStep ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                when (targetStep) {
                    0 -> {
                        TypewriterText("Ah... A new soul enters the Realm.", MedievalTypography.titleLarge, ParchmentCream)
                        Spacer(Modifier.height(16.dp))
                        TypewriterText("I am Percey, Keeper of the Ledgers. What do they call you in the waking world?", MedievalTypography.bodyLarge, Color.LightGray)
                        Spacer(Modifier.height(32.dp))
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AntiqueGold,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = ParchmentCream,
                                unfocusedTextColor = ParchmentCream
                            ),
                            placeholder = { Text("Enter your name...", color = Color.Gray) },
                            singleLine = true
                        )
                        Spacer(Modifier.height(24.dp))
                        if (name.isNotBlank()) {
                            Button(
                                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); step++ },
                                colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Speak Name", color = DeepInkBlack, style = MedievalTypography.titleLarge) }
                        }
                    }
                    1 -> {
                        TypewriterText("Well met, $name.", MedievalTypography.titleLarge, ParchmentCream)
                        Spacer(Modifier.height(16.dp))
                        TypewriterText("What grand feat do you seek to accomplish in the waking world?", MedievalTypography.bodyLarge, Color.LightGray)
                        Spacer(Modifier.height(32.dp))
                        OutlinedTextField(
                            value = goal,
                            onValueChange = { goal = it },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = AntiqueGold,
                                unfocusedBorderColor = Color.DarkGray,
                                focusedTextColor = ParchmentCream,
                                unfocusedTextColor = ParchmentCream
                            ),
                            placeholder = { Text("E.g., Write a novel, run a marathon...", color = Color.Gray) },
                            minLines = 3
                        )
                        Spacer(Modifier.height(24.dp))
                        if (goal.isNotBlank()) {
                            Button(
                                onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); step++ },
                                colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold),
                                shape = RoundedCornerShape(8.dp)
                            ) { Text("Set Destiny", color = DeepInkBlack, style = MedievalTypography.titleLarge) }
                        }
                    }
                    2 -> {
                        Text("Choose your Path, $name.", style = MedievalTypography.titleLarge, color = ParchmentCream)
                        Spacer(Modifier.height(32.dp))
                        val paths = listOf("The Knight" to Icons.Default.Shield, "The Mage" to Icons.Default.AutoAwesome, "The Artisan" to Icons.Default.Brush, "The Ranger" to Icons.Default.Map)

                        paths.forEach { (path, icon) ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp)
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        onComplete(name, path, goal)
                                    },
                                colors = CardDefaults.cardColors(containerColor = DeepSlate),
                                border = border(1.dp, AntiqueGold, RoundedCornerShape(8.dp))
                            ) {
                                Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Icon(icon, contentDescription = null, tint = AntiqueGold, modifier = Modifier.size(28.dp))
                                    Spacer(Modifier.width(16.dp))
                                    Text(path, style = MedievalTypography.titleLarge, color = ParchmentCream)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(viewModel: BecomingViewModel, onQuestSelected: (String) -> Unit) {
    val state by viewModel.charState.collectAsState()
    val activeQuests by viewModel.activeQuests.collectAsState()
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize().background(DeepInkBlack).verticalScroll(rememberScrollState())) {
        // Hero Header
        Box(modifier = Modifier.fillMaxWidth().background(DeepSlate).padding(24.dp)) {
            Column {
                Text(state.heroClass.uppercase(), style = MedievalTypography.bodyLarge, color = AntiqueGold, letterSpacing = 2.sp)
                Text(state.name, style = MedievalTypography.displayLarge, color = ParchmentCream)
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("LVL ${state.level}", style = MedievalTypography.titleLarge, color = AntiqueGold)
                    Spacer(Modifier.width(16.dp))
                    LinearProgressIndicator(
                        progress = { (state.currentXp % 1000) / 1000f },
                        modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = AntiqueGold,
                        trackColor = Color.DarkGray
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // Grand Campaign Card
        Card(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = DeepSlate),
            border = border(1.dp, Color.DarkGray, RoundedCornerShape(8.dp))
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("GRAND CAMPAIGN", style = MedievalTypography.bodyLarge, color = Color.Gray)
                Text(state.campaignTitle.ifBlank { "The Unknown Path" }, style = MedievalTypography.titleLarge, color = ParchmentCream)
                Spacer(Modifier.height(16.dp))
                LinearProgressIndicator(
                    progress = { state.campaignXp.toFloat() / state.totalCampaignXp.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                    color = ForestGreen,
                    trackColor = Color.Black
                )
                Text("${state.campaignXp} / ${state.totalCampaignXp} XP", color = Color.Gray, modifier = Modifier.align(Alignment.End), fontSize = 12.sp)
            }
        }

        // AI Lore Message
        Card(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Color.Transparent),
            border = border(1.dp, AntiqueGold.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
        ) {
            Text(
                text = "“${state.perceyMessage}”",
                style = MedievalTypography.bodyLarge,
                color = AntiqueGold,
                modifier = Modifier.padding(16.dp),
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
            )
        }

        // Notice Board
        Text("THE NOTICE BOARD", style = MedievalTypography.titleLarge, color = AntiqueGold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        if (state.isLoading) {
            Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = AntiqueGold)
                Spacer(Modifier.height(16.dp))
                Text("Scrying the tasks ahead...", color = Color.Gray, style = MedievalTypography.bodyLarge)
            }
        } else {
            activeQuests.forEach { quest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.Medium)
                            onQuestSelected(quest.id)
                        },
                    colors = CardDefaults.cardColors(containerColor = ParchmentCream),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp).background(DeepSlate, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                            val icon = when(quest.iconType) {
                                "strength" -> Icons.Default.FitnessCenter
                                "focus" -> Icons.Default.AutoAwesome
                                "agility" -> Icons.Default.DirectionsRun
                                else -> Icons.Default.Star
                            }
                            Icon(icon, contentDescription = null, tint = AntiqueGold)
                        }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(quest.title, style = MedievalTypography.titleLarge, color = DeepInkBlack, fontSize = 20.sp)
                            Text(quest.desc, style = MedievalTypography.bodyLarge, color = Color.DarkGray, fontSize = 14.sp)
                        }
                        Text("+${quest.xp}", style = MedievalTypography.titleLarge, color = ForestGreen)
                    }
                }
            }
        }
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
fun QuestLoopScreen(quest: Quest, onComplete: (Int) -> Unit) {
    var phase by remember { mutableStateOf(0) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize().background(DeepInkBlack), contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = phase, label = "quest_phase") { targetPhase ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                when (targetPhase) {
                    0 -> { // The Briefing
                        Text("THE BRIEFING", color = AntiqueGold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(quest.title, style = MedievalTypography.displayLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Text(quest.desc, style = MedievalTypography.bodyLarge, color = Color.LightGray, fontSize = 18.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(48.dp))
                        Button(
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); phase = 1 },
                            colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) { Text("Accept Quest", style = MedievalTypography.titleLarge, color = ParchmentCream) }
                    }
                    1 -> { // The Trial
                        Text("THE TRIAL COMMENCES", color = AntiqueGold)
                        Spacer(Modifier.height(48.dp))
                        Box(modifier = Modifier.size(240.dp).border(4.dp, AntiqueGold, RoundedCornerShape(120.dp)).clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            phase = 2
                        }, contentAlignment = Alignment.Center) {
                            Text("TAP WHEN\nCOMPLETED", color = ParchmentCream, style = MedievalTypography.titleLarge, textAlign = TextAlign.Center)
                        }
                    }
                    2 -> { // The Aftermath
                        var ledger by remember { mutableStateOf("") }
                        Text("THE AFTERMATH", color = AntiqueGold)
                        Spacer(Modifier.height(16.dp))
                        Text("Log your trial in the Ledger.", style = MedievalTypography.bodyLarge, color = ParchmentCream)
                        Spacer(Modifier.height(24.dp))
                        OutlinedTextField(
                            value = ledger, onValueChange = { ledger = it },
                            modifier = Modifier.fillMaxWidth().height(150.dp),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AntiqueGold, focusedTextColor = ParchmentCream, unfocusedTextColor = ParchmentCream),
                            placeholder = { Text("What did you learn? How did it feel?...", color = Color.Gray) }
                        )
                        Spacer(Modifier.height(24.dp))
                        Button(
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onComplete(quest.xp) },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            enabled = ledger.isNotBlank()
                        ) { Text("Claim Bounty (+${quest.xp} XP)", style = MedievalTypography.titleLarge, color = ParchmentCream) }
                    }
                }
            }
        }
    }
}

@Composable
fun RewardScreen(xp: Int, isLevelUp: Boolean, onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        delay(300)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (isLevelUp) {
            delay(200)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val bgColor = if(isLevelUp) AntiqueGold else DeepSlate
    val textColor = if(isLevelUp) DeepInkBlack else ParchmentCream

    Box(modifier = Modifier.fillMaxSize().background(bgColor).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if(isLevelUp) "LEVEL UP!" else "BOUNTY CLAIMED", style = MedievalTypography.displayLarge, color = textColor)
            Spacer(Modifier.height(16.dp))
            Text("+$xp XP", style = MedievalTypography.displayLarge, color = if(isLevelUp) DeepInkBlack else AntiqueGold, fontSize = 64.sp)
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = if(isLevelUp) DeepInkBlack else AntiqueGold),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) { Text("Return to Realm", style = MedievalTypography.titleLarge, color = if(isLevelUp) AntiqueGold else DeepInkBlack) }
        }
    }
}