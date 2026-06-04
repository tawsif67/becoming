package com.example.becoming

import android.graphics.Bitmap
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.becoming.ui.theme.*
import kotlinx.coroutines.delay

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
fun OnboardingScreen(onComplete: (String, String) -> Unit) {
    var step by remember { mutableStateOf(0) }
    var name by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize().background(ParchmentCream), contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = step, label = "onboarding") { targetStep ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                when (targetStep) {
                    0 -> {
                        Text("Hello, Traveller!", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack)
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = { haptic.performHapticFeedback(HapticFeedbackType.LongPress); step++ },
                            colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)
                        ) { Text("Continue", color = DeepInkBlack) }
                    }
                    1 -> {
                        Text("What do they call you?", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack)
                        Spacer(Modifier.height(16.dp))
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AntiqueGold),
                            textStyle = MaterialTheme.typography.bodyLarge
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(
                            onClick = { 
                                if (name.isNotBlank()) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onComplete(name, "Knight") 
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)
                        ) { Text("Seal Destiny", color = DeepInkBlack) }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    viewModel: BecomingViewModel, 
    onQuestSelected: (String) -> Unit,
    onOpenInventory: () -> Unit
) {
    val state by viewModel.charState.collectAsState()
    val isProcessing by viewModel.isProcessing.collectAsState()

    Box {
        Column(modifier = Modifier.fillMaxSize().background(ParchmentCream).padding(16.dp)) {
            // Hero Header
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("LVL ${state.level}", style = MaterialTheme.typography.displayLarge, color = DeepInkBlack, fontSize = 32.sp)
                    Text(state.name.uppercase(), style = MaterialTheme.typography.titleLarge, color = DeepInkBlack)
                }
                IconButton(onClick = onOpenInventory) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = "Alchemist's Inventory", tint = AntiqueGold)
                }
            }
            
            Spacer(Modifier.height(24.dp))
            
            // Flame of Resolve (Streak Proxy)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = DeepSlate,
                shape = RoundedCornerShape(8.dp)
            ) {
                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("🔥", fontSize = 24.sp)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("FLAME OF RESOLVE", style = MaterialTheme.typography.labelSmall, color = AntiqueGold)
                        Text("Your light burns steady, Traveller.", color = ParchmentCream)
                    }
                }
            }

            Spacer(Modifier.height(32.dp))

            Text("NOTICE BOARD", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack)
            Spacer(Modifier.height(16.dp))

            if (viewModel.activeQuests.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "The realm is quiet today.\nRest, or seek out your own trials.",
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyLarge,
                        color = DeepSlate.copy(alpha = 0.6f)
                    )
                }
            } else {
                LazyColumn {
                    items(viewModel.activeQuests) { quest ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp)
                                .clickable { onQuestSelected(quest.id) },
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = BorderStroke(1.dp, Color.LightGray)
                        ) {
                            Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(quest.title, style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, fontSize = 20.sp)
                                    Text("+${quest.xp} XP", color = AntiqueGold, fontWeight = FontWeight.Bold)
                                }
                                if (quest.requiresProof) {
                                    Icon(Icons.Default.AddAPhoto, contentDescription = "Proof Required", tint = DeepSlate)
                                }
                            }
                        }
                    }
                }
            }
        }
        
        if (isProcessing) LoadingOverlay()
    }
}

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
                        .border(2.dp, AntiqueGold, RoundedCornerShape(12.dp))
                        .clickable { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            // Create a dummy bitmap to trigger the Gemini logic for the demo
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
                    colors = CardDefaults.cardColors(containerColor = DeepSlate)
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

@Composable
fun QuestLoopScreen(quest: Quest, viewModel: BecomingViewModel, onComplete: (Int) -> Unit) {
    var phase by remember { mutableStateOf(0) }
    val isProcessing by viewModel.isProcessing.collectAsState()
    val haptic = LocalHapticFeedback.current

    Box {
        Box(modifier = Modifier.fillMaxSize().background(DeepSlate), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
                if (phase == 0) {
                    Text("THE BRIEFING", color = AntiqueGold, style = MaterialTheme.typography.labelSmall)
                    Text(quest.title, style = MaterialTheme.typography.displayLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                    Text(quest.desc, color = ParchmentCream, textAlign = TextAlign.Center, modifier = Modifier.padding(top = 8.dp))
                    
                    Spacer(Modifier.height(48.dp))
                    
                    Button(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            phase = 1 
                        }, 
                        colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("SWIPE TO SEAL", color = ParchmentCream) // Simplified for button
                    }
                } else if (phase == 1) {
                    if (quest.requiresProof) {
                        Text("THE SCRYING GLASS", color = AntiqueGold)
                        Text("Proof of your trial is required.", color = ParchmentCream)
                        Spacer(Modifier.height(32.dp))
                        IconButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                // In a real app: Capture photo -> viewModel.verifyProof -> phase = 2
                                phase = 2 
                            },
                            modifier = Modifier.size(80.dp).background(AntiqueGold, RoundedCornerShape(40.dp))
                        ) {
                            Icon(Icons.Default.AddAPhoto, contentDescription = "Capture Proof", tint = DeepInkBlack)
                        }
                    } else {
                        phase = 2
                    }
                } else {
                    Text("THE AFTERMATH", color = AntiqueGold)
                    Text("The trial is concluded.", color = ParchmentCream)
                    Spacer(Modifier.height(48.dp))
                    Button(
                        onClick = { 
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onComplete(quest.xp) 
                        }, 
                        colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                        modifier = Modifier.fillMaxWidth().height(56.dp)
                    ) {
                        Text("CLAIM BOUNTY")
                    }
                }
            }
        }
        if (isProcessing) LoadingOverlay()
    }
}

@Composable
fun RewardScreen(xp: Int, isLevelUp: Boolean, onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    LaunchedEffect(Unit) {
        delay(500)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(
        modifier = Modifier.fillMaxSize().background(if(isLevelUp) AntiqueGold else DeepSlate),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                if(isLevelUp) "LEVEL UP!" else "BOUNTY CLAIMED", 
                style = MaterialTheme.typography.displayLarge, 
                color = if(isLevelUp) DeepInkBlack else AntiqueGold,
                textAlign = TextAlign.Center
            )
            Text("+$xp XP", style = MaterialTheme.typography.displayLarge, color = ParchmentCream)
            Spacer(Modifier.height(48.dp))
            Button(
                onClick = onBack, 
                colors = ButtonDefaults.buttonColors(containerColor = if(isLevelUp) DeepInkBlack else AntiqueGold),
                modifier = Modifier.padding(horizontal = 32.dp)
            ) {
                Text("Return to Realm", color = if(isLevelUp) AntiqueGold else DeepInkBlack)
            }
        }
    }
}
