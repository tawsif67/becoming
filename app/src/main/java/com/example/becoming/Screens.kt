package com.example.becoming

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
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
fun LoadingOverlay(text: String = "Consulting the Grimoires...") {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.7f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = AntiqueGold)
            Spacer(Modifier.height(16.dp))
            Text(text, style = MaterialTheme.typography.bodyLarge, color = ParchmentCream)
        }
    }
}

@Composable
fun TypewriterText(text: String, style: androidx.compose.ui.text.TextStyle, color: Color) {
    var displayedText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        displayedText = ""
        for (i in text.indices) {
            displayedText += text[i]
            delay(35)
        }
    }
    Text(displayedText, style = style, color = color, textAlign = TextAlign.Center)
}

// --- ONBOARDING FLOW ---
@Composable
fun OnboardingScreen(onComplete: (String, String, List<String>, String, Int, String) -> Unit) {
    var step by remember { mutableIntStateOf(0) }
    var name by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var goal by remember { mutableStateOf("") }
    var timeline by remember { mutableIntStateOf(3) }
    var selectedTraits by remember { mutableStateOf(listOf<String>()) }
    val haptic = LocalHapticFeedback.current

    val allTraits = listOf("Vitality", "Intellect", "Focus", "Endurance", "Charisma", "Discipline", "Creativity", "Empathy", "Courage", "Wisdom")

    Box(modifier = Modifier.fillMaxSize().background(DeepInkBlack).padding(24.dp), contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = step, label = "onboarding") { targetStep ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                when (targetStep) {
                    0 -> {
                        TypewriterText("Welcome to this world of endless possibilities. You can be here anything and everything you ever wanted!", MaterialTheme.typography.titleLarge, ParchmentCream)
                        Spacer(Modifier.height(32.dp))
                        Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); step++ }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) { Text("Continue", color = DeepInkBlack) }
                    }
                    1 -> {
                        TypewriterText("I am your guide here. Imagine me as a wrinkly old wizard with a funny hat. I'll be here whenever you need me, for anything!", MaterialTheme.typography.bodyLarge, Color.LightGray)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); step++ }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) { Text("Understood, Percey.", color = DeepInkBlack) }
                    }
                    2 -> {
                        TypewriterText("Now tell me, what do you want to call me?", MaterialTheme.typography.titleLarge, ParchmentCream)
                        Spacer(Modifier.height(24.dp))
                        var tempName by remember { mutableStateOf("") }
                        OutlinedTextField(value = tempName, onValueChange = { tempName = it }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ParchmentCream, unfocusedTextColor = ParchmentCream), placeholder = { Text("Enter a name...") })
                        Spacer(Modifier.height(16.dp))
                        if (tempName.isNotBlank()) Button(onClick = { step++ }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) { Text("Submit", color = DeepInkBlack) }
                    }
                    3 -> {
                        TypewriterText("Hmm... That doesn't sound too good. Perhaps I expected too much too fast from you. You know what? Call me Percey.", MaterialTheme.typography.titleLarge, ParchmentCream)
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); step++ }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) { Text("Understood, Percey.", color = DeepInkBlack) }
                    }
                    4 -> {
                        TypewriterText("Now, what do they call you, my friend?", MaterialTheme.typography.titleLarge, ParchmentCream)
                        Spacer(Modifier.height(24.dp))
                        OutlinedTextField(value = name, onValueChange = { name = it }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ParchmentCream, unfocusedTextColor = ParchmentCream), placeholder = { Text("Your Name") })
                        Spacer(Modifier.height(16.dp))
                        if (name.isNotBlank()) Button(onClick = { step++ }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) { Text("Submit", color = DeepInkBlack) }
                    }
                    5 -> {
                        TypewriterText("Ah, $name, that sounds good. Now, tell me, when did your story begin?", MaterialTheme.typography.titleLarge, ParchmentCream)
                        Spacer(Modifier.height(24.dp))
                        OutlinedTextField(value = dob, onValueChange = { dob = it }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ParchmentCream, unfocusedTextColor = ParchmentCream), placeholder = { Text("YYYY-MM-DD") })
                        Spacer(Modifier.height(16.dp))
                        if (dob.isNotBlank()) Button(onClick = { step++ }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) { Text("Next", color = DeepInkBlack) }
                    }
                    6 -> {
                        Text("Choose your 5 core traits to master.", style = MaterialTheme.typography.titleLarge, color = ParchmentCream)
                        Spacer(Modifier.height(16.dp))
                        Column {
                            allTraits.chunked(2).forEach { rowTraits ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                    rowTraits.forEach { trait ->
                                        val isSelected = selectedTraits.contains(trait)
                                        Button(
                                            onClick = {
                                                if (isSelected) selectedTraits = selectedTraits - trait
                                                else if (selectedTraits.size < 5) selectedTraits = selectedTraits + trait
                                            },
                                            colors = ButtonDefaults.buttonColors(containerColor = if (isSelected) AntiqueGold else DeepSlate),
                                            modifier = Modifier.weight(1f)
                                        ) { Text(trait, color = if(isSelected) DeepInkBlack else Color.Gray) }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        if (selectedTraits.size == 5) Button(onClick = { step++ }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) { Text("Confirm Traits", color = DeepInkBlack) }
                    }
                    7 -> {
                        Text("What great feat do you seek to accomplish in the waking world, and how many moons do you give yourself?", style = MaterialTheme.typography.titleLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        OutlinedTextField(value = goal, onValueChange = { goal = it }, modifier = Modifier.fillMaxWidth().height(100.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ParchmentCream, unfocusedTextColor = ParchmentCream), placeholder = { Text("E.g., Write a novel, run a marathon...") })
                        Spacer(Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(3, 6, 12).forEach { m ->
                                Button(onClick = { timeline = m }, colors = ButtonDefaults.buttonColors(containerColor = if(timeline == m) AntiqueGold else DeepSlate)) { Text("$m Moons", color = if(timeline == m) DeepInkBlack else Color.Gray) }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        if (goal.isNotBlank()) {
                            Button(onClick = { step++ }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold), modifier = Modifier.fillMaxWidth()) { Text("Consult the Stars", color = DeepInkBlack) }
                        }
                    }
                    8 -> {
                        val suggestedPath = if(goal.lowercase().contains("write") || goal.lowercase().contains("art")) "Artisan"
                        else if(goal.lowercase().contains("run") || goal.lowercase().contains("explore")) "Ranger"
                        else if(goal.lowercase().contains("read") || goal.lowercase().contains("code")) "Mage"
                        else "Knight"

                        Text("The stars speak clearly. You are destined to walk the path of the $suggestedPath.", style = MaterialTheme.typography.titleLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(32.dp))

                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onComplete(name, dob, selectedTraits, goal, timeline, suggestedPath)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold), modifier = Modifier.fillMaxWidth()
                        ) { Text("Accept Destiny", color = DeepInkBlack) }

                        Spacer(Modifier.height(16.dp))
                        Text("Or, forge your own path...", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                        Spacer(Modifier.height(8.dp))

                        listOf("Knight", "Mage", "Artisan", "Ranger").filter { it != suggestedPath }.forEach { path ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onComplete(name, dob, selectedTraits, goal, timeline, path)
                                },
                                colors = CardDefaults.cardColors(containerColor = DeepSlate),
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, Color.DarkGray)
                            ) {
                                Text(path, style = MaterialTheme.typography.titleLarge, color = Color.LightGray, modifier = Modifier.padding(16.dp))
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- STORY SELECTION SCREEN ---
@Composable
fun StorySelectionScreen(viewModel: BecomingViewModel, onStorySealed: () -> Unit) {
    val state by viewModel.charState.collectAsState()
    val stories by viewModel.generatedStories.collectAsState()
    val haptic = LocalHapticFeedback.current

    var editingStory by remember { mutableStateOf<CampaignStory?>(null) }
    var editTitle by remember { mutableStateOf("") }
    var editLore by remember { mutableStateOf("") }

    Box(modifier = Modifier.fillMaxSize().background(DeepSlate).padding(16.dp)) {
        if (state.isLoadingAI) {
            Column(modifier = Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = AntiqueGold)
                Spacer(Modifier.height(16.dp))
                Text("Percey is scrying the timelines...", color = AntiqueGold, style = MaterialTheme.typography.titleLarge)
            }
        } else if (editingStory != null) {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text("Customize Your Legend", style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = editTitle, onValueChange = { editTitle = it }, modifier = Modifier.fillMaxWidth(), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ParchmentCream, unfocusedTextColor = ParchmentCream))
                Spacer(Modifier.height(16.dp))
                OutlinedTextField(value = editLore, onValueChange = { editLore = it }, modifier = Modifier.fillMaxWidth().height(200.dp), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = ParchmentCream, unfocusedTextColor = ParchmentCream))
                Spacer(Modifier.height(24.dp))
                Button(
                    onClick = {
                        viewModel.customizeStory(editingStory!!.id, editTitle, editLore)
                        editingStory = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ForestGreen), modifier = Modifier.fillMaxWidth()
                ) { Text("Save Changes", color = ParchmentCream) }
            }
        } else {
            Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Text("Choose Your Grand Campaign", style = MaterialTheme.typography.displayLarge, color = AntiqueGold, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth().padding(top = 24.dp))
                Spacer(Modifier.height(24.dp))
                stories.forEach { story ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = DeepInkBlack),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, AntiqueGold),
                        elevation = CardDefaults.cardElevation(12.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text(story.title, style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
                            Spacer(Modifier.height(8.dp))
                            Text(story.loreSummary, style = MaterialTheme.typography.bodyLarge, color = ParchmentCream)
                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                TextButton(onClick = {
                                    editTitle = story.title
                                    editLore = story.loreSummary
                                    editingStory = story
                                }) { Text("Customize", color = Color.Gray) }

                                Button(onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    viewModel.sealCampaign(story)
                                    onStorySealed()
                                }, colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed)) {
                                    Text("Seal Destiny", color = ParchmentCream)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- DASHBOARD (THE REALM) ---
@Composable
fun DashboardScreen(
    viewModel: BecomingViewModel, 
    onQuestSelected: (String) -> Unit,
    onOpenInventory: () -> Unit
) {
    val state by viewModel.charState.collectAsState()
    val activeQuests by viewModel.activeQuests.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val haptic = LocalHapticFeedback.current

    Box {
        Column(modifier = Modifier.fillMaxSize().background(ParchmentCream).verticalScroll(rememberScrollState())) {

            // 1. Grand Campaign Tracker (Hero Element)
            Box(modifier = Modifier.fillMaxWidth().background(DeepSlate).padding(24.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text("GRAND CAMPAIGN", style = MaterialTheme.typography.bodyLarge, color = AntiqueGold, letterSpacing = 2.sp)
                            Text(state.activeCampaign?.title ?: "Unknown Fate", style = MaterialTheme.typography.titleLarge, color = ParchmentCream)
                        }
                        IconButton(onClick = onOpenInventory) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = "Alchemist's Inventory", tint = AntiqueGold)
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                    LinearProgressIndicator(
                        progress = { state.campaignXp.toFloat() / (state.activeCampaign?.totalRequiredXp?.toFloat() ?: 30000f) },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = ForestGreen, trackColor = DeepInkBlack
                    )
                    Text("${state.campaignXp} / ${state.activeCampaign?.totalRequiredXp ?: 0} XP", color = Color.Gray, modifier = Modifier.align(Alignment.End), fontSize = 12.sp, style = MaterialTheme.typography.bodyLarge)
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. Character Header & Traits
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).background(AntiqueGold, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    Text("LVL\n${state.level}", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.width(16.dp))
                Column {
                    Text(state.name, style = MaterialTheme.typography.displayLarge, color = DeepInkBlack)
                    Text("The ${state.heroClass}", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                }
            }

            Spacer(Modifier.height(24.dp))
            Text("CORE TRAITS", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))
            state.traits.forEach { trait ->
                Row(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(trait, style = MaterialTheme.typography.bodyLarge, color = DeepInkBlack, modifier = Modifier.width(100.dp))
                    LinearProgressIndicator(progress = { (state.level * 5) / 100f }, modifier = Modifier.weight(1f).height(8.dp).clip(RoundedCornerShape(4.dp)), color = RoyalBlue, trackColor = Color.LightGray)
                }
            }

            Spacer(Modifier.height(32.dp))

            // 3. Notice Board (Flagship Quests)
            Text("NOTICE BOARD", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            activeQuests.forEach { quest ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp).clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onQuestSelected(quest.id)
                    },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, AntiqueGold.copy(alpha=0.5f))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        val icon = when(quest.iconType) { "gps" -> Icons.Default.Map; "tap" -> Icons.Default.PanTool; "camera" -> Icons.Default.CameraAlt; "timer" -> Icons.Default.HourglassEmpty; else -> Icons.Default.Bedtime }
                        Box(modifier = Modifier.size(48.dp).background(DeepSlate, RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) { Icon(icon, contentDescription = null, tint = AntiqueGold) }
                        Spacer(Modifier.width(16.dp))
                        Column(Modifier.weight(1f)) {
                            Text(quest.title, style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, fontSize = 20.sp)
                            Text(quest.desc, style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray, fontSize = 14.sp, maxLines = 2)
                        }
                        Text("+${quest.xp}", style = MaterialTheme.typography.titleLarge, color = CrimsonRed)
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
        if (isProcessing) LoadingOverlay()
    }
}

// --- ALCHEMIST INVENTORY ---
@Composable
fun AlchemistInventoryScreen(viewModel: BecomingViewModel, onBack: () -> Unit) {
    val result by viewModel.alchemistResult.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()
    val state by viewModel.charState.collectAsState()
    val haptic = LocalHapticFeedback.current

    Box {
        Column(modifier = Modifier.fillMaxSize().background(ParchmentCream).padding(16.dp)) {
            Text("THE ALCHEMIST'S LEDGER", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack)
            Spacer(Modifier.height(8.dp))
            Text("Daily Consumables: ${state.dailyProtein}g Protein | ${state.dailyCarbs}g Carbs", color = DeepSlate)
            
            Spacer(Modifier.height(24.dp))

            if (result == null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .border(BorderStroke(2.dp, AntiqueGold), RoundedCornerShape(12.dp))
                        .clickable { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            val dummyBitmap = android.graphics.Bitmap.createBitmap(100, 100, android.graphics.Bitmap.Config.ARGB_8888)
                            viewModel.analyzeMeal(dummyBitmap)
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, modifier = Modifier.size(48.dp), tint = AntiqueGold)
                        Text("Scry Your Meal", color = AntiqueGold)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DeepSlate),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text(result!!.energyType.uppercase(), style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
                        Text(result!!.foodName, style = MaterialTheme.typography.bodyLarge, color = ParchmentCream)
                        Spacer(Modifier.height(12.dp))
                        Text(result!!.comment, style = MaterialTheme.typography.bodyLarge, color = ParchmentCream, fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.clearAlchemistResult() },
                            colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)
                        ) {
                            Text("Acknowledge", color = DeepInkBlack)
                        }
                    }
                }
            }
            
            Spacer(Modifier.weight(1f))
            
            TextButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                Text("Return to the Armory", color = DeepInkBlack)
            }
        }
        
        if (isProcessing) LoadingOverlay()
    }
}

// --- QUEST LOOP (3 PHASES) ---
@Composable
fun QuestLoopScreen(quest: Quest, onComplete: (Int) -> Unit) {
    var phase by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize().background(DeepInkBlack), contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = phase, label = "quest_phase") { targetPhase ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                when (targetPhase) {
                    0 -> { // Briefing
                        Text("THE BRIEFING", color = AntiqueGold, letterSpacing = 2.sp)
                        Spacer(Modifier.height(16.dp))
                        Text(quest.title, style = MaterialTheme.typography.displayLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        Text(quest.desc, style = MaterialTheme.typography.bodyLarge, color = Color.LightGray, fontSize = 18.sp, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(48.dp))
                        Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); phase = 1 }, colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Accept Quest", style = MaterialTheme.typography.titleLarge, color = ParchmentCream) }
                    }
                    1 -> { // Trial UI Placeholder
                        Text("THE TRIAL COMMENCES", color = AntiqueGold)
                        Spacer(Modifier.height(48.dp))
                        Box(modifier = Modifier.size(240.dp).border(BorderStroke(4.dp, if(quest.iconType == "timer") CrimsonRed else AntiqueGold), RoundedCornerShape(120.dp)).clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            phase = 2
                        }, contentAlignment = Alignment.Center) {
                            Text("TAP WHEN\nCOMPLETED", color = ParchmentCream, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                        }
                    }
                    2 -> { // Aftermath Journal
                        var journal1 by remember { mutableStateOf("") }
                        Text("THE AFTERMATH", color = AntiqueGold)
                        Spacer(Modifier.height(24.dp))
                        OutlinedTextField(value = journal1, onValueChange = { journal1 = it }, modifier = Modifier.fillMaxWidth().height(150.dp), colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AntiqueGold, focusedTextColor = ParchmentCream, unfocusedTextColor = ParchmentCream), placeholder = { Text("Log your reflections in the ledger...", color = Color.Gray) })
                        Spacer(Modifier.height(24.dp))
                        Button(onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); onComplete(quest.xp) }, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen), modifier = Modifier.fillMaxWidth().height(56.dp), enabled = journal1.isNotBlank()) { Text("Seal Record & Claim Bounty", style = MaterialTheme.typography.titleLarge, color = ParchmentCream) }
                    }
                }
            }
        }
    }
}

// --- DOPAMINE REWARD SCREEN ---
@Composable
fun RewardScreen(viewModel: BecomingViewModel, xp: Int, isLevelUp: Boolean, onBack: () -> Unit) {
    val state by viewModel.charState.collectAsState()
    val haptic = LocalHapticFeedback.current
    var displayXp by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        // Slot machine effect
        while(displayXp < xp) {
            displayXp += 10
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            delay(20)
        }
        displayXp = xp
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        if (isLevelUp) {
            delay(300)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val bgColor = if(isLevelUp) AntiqueGold else DeepSlate
    val textColor = if(isLevelUp) DeepInkBlack else ParchmentCream

    Box(modifier = Modifier.fillMaxSize().background(bgColor).padding(24.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(if(isLevelUp) "LEVEL UP!" else "BOUNTY CLAIMED", style = MaterialTheme.typography.displayLarge, color = textColor)
            Spacer(Modifier.height(16.dp))
            Text("+$displayXp XP", style = MaterialTheme.typography.displayLarge, color = if(isLevelUp) DeepInkBlack else AntiqueGold, fontSize = 64.sp)
            Spacer(Modifier.height(24.dp))

            // Dynamic Story Tie-In
            Text("Your efforts push you ever closer to:", style = MaterialTheme.typography.bodyLarge, color = if(isLevelUp) DeepSlate else Color.Gray)
            Text(state.activeCampaign?.title ?: "", style = MaterialTheme.typography.titleLarge, color = textColor, textAlign = TextAlign.Center)

            Spacer(Modifier.height(48.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = if(isLevelUp) DeepInkBlack else AntiqueGold), modifier = Modifier.fillMaxWidth().height(56.dp)) { Text("Return to Realm", style = MaterialTheme.typography.titleLarge, color = if(isLevelUp) AntiqueGold else DeepInkBlack) }
        }
    }
}
