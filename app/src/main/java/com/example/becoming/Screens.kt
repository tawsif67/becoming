package com.example.becoming

import android.net.Uri
import android.widget.VideoView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.becoming.data.*
import com.example.becoming.domain.*
import com.example.becoming.ui.components.*
import com.example.becoming.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- UTILITY COMPOSABLES ---

@Composable
fun OutlinedText(
    text: String,
    style: TextStyle,
    color: Color,
    modifier: Modifier = Modifier,
    textAlign: TextAlign? = null
) {
    Text(
        text = text,
        style = style.copy(
            shadow = Shadow(
                color = Color.Black.copy(alpha = 0.9f),
                blurRadius = 6f
            )
        ),
        color = color,
        modifier = modifier,
        textAlign = textAlign
    )
}

@Composable
fun TypewriterText(text: String, style: TextStyle, color: Color) {
    var displayedText by remember { mutableStateOf("") }
    LaunchedEffect(text) {
        displayedText = ""
        for (i in text.indices) {
            displayedText += text[i]
            delay(35)
        }
    }
    OutlinedText(displayedText, style = style, color = color, textAlign = TextAlign.Center)
}

// --- HELPERS ---

private fun String.toClassType(): ClassType = when (this.uppercase()) {
    "MAGE" -> ClassType.MAGE
    "ARTISAN" -> ClassType.ARTISAN
    "RANGER" -> ClassType.RANGER
    else -> ClassType.KNIGHT
}

// --- TITLE SCREEN ---
@Composable
fun TitleScreen(state: CharacterState, soundManager: SoundManager?, onContinue: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedText("BECOMING", style = MaterialTheme.typography.displayLarge, color = AntiqueGold, modifier = Modifier.graphicsLayer { scaleX = 1.2f; scaleY = 1.2f })
            OutlinedText("THE LEDGER OF LEGENDS", style = MaterialTheme.typography.titleLarge, color = ParchmentCream)
            
            Spacer(Modifier.height(80.dp))
            
            RuneSlate(
                modifier = Modifier.width(240.dp),
                streakStatus = if (state.campfireStreak == 0) List(7) { false } else List(7) { i -> i < (state.campfireStreak - 1) % 7 + 1 },
                classTheme = state.heroClass.toClassType()
            )
            Spacer(Modifier.height(32.dp))
            
            OutlinedText("Welcome back, ${state.heroName}", color = Color.LightGray, style = MaterialTheme.typography.bodyLarge)
            OutlinedText("The ${state.heroClass}", color = AntiqueGold, style = MaterialTheme.typography.titleLarge)
            
            Spacer(Modifier.height(48.dp))
            
            Button(
                onClick = { 
                    soundManager?.playClick()
                    onContinue() 
                }, 
                colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("CONTINUE YOUR CHRONICLE", color = DeepInkBlack)
                    Spacer(Modifier.width(8.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = DeepInkBlack)
                }
            }
        }
    }
}

// --- ONBOARDING FLOW ---
enum class OnboardingPhase { TITLE, POETIC1, POETIC2, POETIC3, POETIC4, POETIC5, POETIC6, NAME, QUIZ, ATTRIBUTES, ACTIVITIES, CLASS_GATE, WAR_ROOM, CRUCIBLE, DESTINY }

@Composable
fun OnboardingScreen(viewModel: BecomingViewModel, soundManager: SoundManager?, onComplete: () -> Unit) {
    val context = LocalContext.current
    var phase by remember { mutableStateOf(OnboardingPhase.TITLE) }
    var name by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    
    val suggestedClass by viewModel.suggestedClass.collectAsState()
    val questions = viewModel.quizQuestions
    
    // Selection States
    val selectedAttributes = remember { mutableStateListOf<AttributeOption>() }
    val selectedDisciplines = remember { mutableStateListOf<DisciplineOption>() }
    
    var highlightedAttribute by remember { mutableStateOf<AttributeOption?>(null) }
    
    val baselines = remember { mutableStateMapOf<String, Float>() }
    val goals = remember { mutableStateMapOf<String, Float>() }
    
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val collectedAnswers = remember { mutableStateListOf<QuizOption>() }
    
    var selectedGateClass by remember { mutableStateOf("KNIGHT") }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
        AnimatedContent(targetState = phase, label = "onboarding") { targetPhase ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(24.dp).verticalScroll(rememberScrollState())) {
                when (targetPhase) {
                    OnboardingPhase.TITLE -> {
                        OutlinedText("BECOMING", style = MaterialTheme.typography.displayLarge, color = AntiqueGold)
                        OutlinedText("THE AWAKENING", style = MaterialTheme.typography.titleLarge, color = ParchmentCream)
                        Spacer(Modifier.height(48.dp))
                        Button(onClick = { 
                            soundManager?.playClick()
                            phase = OnboardingPhase.POETIC1 
                        }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) {
                            Text("Begin the Journey", color = DeepInkBlack)
                        }
                    }
                    OnboardingPhase.POETIC1 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            TypewriterText("Hail, Traveler of the Waking World.", MaterialTheme.typography.titleLarge, ParchmentCream)
                            Spacer(Modifier.height(48.dp))
                            Button(onClick = { soundManager?.playClick(); phase = OnboardingPhase.POETIC2 }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) {
                                Text("Step Forward", color = DeepInkBlack)
                            }
                        }
                    }
                    OnboardingPhase.POETIC2 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            TypewriterText("Step across the threshold into a realm of untamed majesty. Here, the air hums with ancient promise, and the horizon stretches without end.", MaterialTheme.typography.bodyLarge, ParchmentCream)
                            Spacer(Modifier.height(48.dp))
                            Button(onClick = { soundManager?.playClick(); phase = OnboardingPhase.POETIC3 }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) {
                                Text("Venture Deeper", color = DeepInkBlack)
                            }
                        }
                    }
                    OnboardingPhase.POETIC3 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            TypewriterText("Within these borders, the tapestry of fate remains unspun. The clay of destiny yields only to your hands—your potential is as limitless as the stars above.", MaterialTheme.typography.bodyLarge, ParchmentCream)
                            Spacer(Modifier.height(48.dp))
                            Button(onClick = { soundManager?.playClick(); phase = OnboardingPhase.POETIC4 }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) {
                                Text("Acknowledge Fate", color = DeepInkBlack)
                            }
                        }
                    }
                    OnboardingPhase.POETIC4 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            TypewriterText("Do you wish to outrun the wind, striking the earth until tomorrow surrenders? We shall race the sun together. Do you seek to devour the ancient tomes, delving into deep focus until the knowledge burns behind your eyes? It shall be so.", MaterialTheme.typography.bodyLarge, ParchmentCream)
                            Spacer(Modifier.height(48.dp))
                            Button(onClick = { soundManager?.playClick(); phase = OnboardingPhase.POETIC5 }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) {
                                Text("Declare Intent", color = DeepInkBlack)
                            }
                        }
                    }
                    OnboardingPhase.POETIC5 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            TypewriterText("Hear me well, my friend: you are the master of your destiny, never the victim of your fate.", MaterialTheme.typography.titleLarge, ParchmentCream)
                            Spacer(Modifier.height(48.dp))
                            Button(onClick = { soundManager?.playClick(); phase = OnboardingPhase.POETIC6 }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) {
                                Text("Acknowledge Truth", color = DeepInkBlack)
                            }
                        }
                    }
                    OnboardingPhase.POETIC6 -> {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                            TypewriterText("Welcome to the crucible of your Becoming. In this realm, the impossible is nothing but a legend waiting to be written.", MaterialTheme.typography.titleLarge, ParchmentCream)
                            Spacer(Modifier.height(48.dp))
                            Button(onClick = { soundManager?.playClick(); phase = OnboardingPhase.NAME }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) {
                                Text("Enter the Crucible", color = DeepInkBlack)
                            }
                        }
                    }
                    OnboardingPhase.NAME -> {
                        TypewriterText("What do they call you, Traveller?", MaterialTheme.typography.titleLarge, ParchmentCream)
                        Spacer(Modifier.height(24.dp))
                        OutlinedTextField(
                            value = name, 
                            onValueChange = { 
                                if (it.length > name.length) soundManager?.playClick()
                                name = it 
                            }, 
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = ParchmentCream, 
                                unfocusedTextColor = ParchmentCream,
                                focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                                unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                            ),
                            placeholder = { Text("Enter your name...", color = Color.Gray) }
                        )
                        Spacer(Modifier.height(24.dp))
                        if (name.isNotBlank()) {
                            Button(onClick = { 
                                soundManager?.playClick()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                phase = OnboardingPhase.QUIZ 
                            }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) { Text("Seal Name", color = DeepInkBlack) }
                        }
                    }
                    OnboardingPhase.QUIZ -> {
                        val question = questions[currentQuestionIndex]
                        OutlinedText("THE RITE OF PASSAGE", style = MaterialTheme.typography.labelSmall, color = AntiqueGold)
                        Spacer(Modifier.height(16.dp))
                        OutlinedText(question.text, style = MaterialTheme.typography.titleLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(32.dp))
                        
                        question.options.forEach { opt ->
                            Button(
                                onClick = {
                                    soundManager?.playClick()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    collectedAnswers.add(opt)
                                    if (currentQuestionIndex < questions.size - 1) {
                                        currentQuestionIndex++
                                    } else {
                                        viewModel.calculateSuggestion(selectedAttributes.toList(), collectedAnswers.toList())
                                        selectedGateClass = viewModel.suggestedClass.value
                                        phase = OnboardingPhase.ATTRIBUTES
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) { Text(opt.text, color = ParchmentCream, textAlign = TextAlign.Center) }
                        }
                    }
                    OnboardingPhase.ATTRIBUTES -> {
                        OutlinedText("THE SELECTION OF ATTRIBUTES", style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
                        Spacer(Modifier.height(24.dp))
                        
                        Column {
                            viewModel.premiumAttributes.chunked(2).forEach { row ->
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    row.forEach { attr ->
                                        val isSelected = selectedAttributes.contains(attr)
                                        val isHighlighted = highlightedAttribute == attr
                                        Card(
                                            modifier = Modifier.weight(1f).aspectRatio(1f).clickable {
                                                soundManager?.playClick()
                                                highlightedAttribute = attr
                                            }.border(2.dp, if(isSelected) AntiqueGold else if(isHighlighted) Color.White else Color.Transparent, RoundedCornerShape(8.dp)),
                                            colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))
                                        ) {
                                            Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                                                Icon(attr.icon, contentDescription = null, tint = if(isSelected) AntiqueGold else Color.Gray, modifier = Modifier.size(40.dp))
                                                Text(attr.name, color = if(isSelected) AntiqueGold else Color.White, fontSize = 10.sp, textAlign = TextAlign.Center)
                                            }
                                        }
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        
                        Spacer(Modifier.height(16.dp))
                        highlightedAttribute?.let { attr ->
                            val isSelected = selectedAttributes.contains(attr)
                            Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f)).padding(16.dp)) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(attr.description, color = ParchmentCream, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.height(8.dp))
                                    Button(
                                        onClick = {
                                            soundManager?.playClick()
                                            if (isSelected) selectedAttributes.remove(attr) else selectedAttributes.add(attr)
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = if(isSelected) CrimsonRed else AntiqueGold)
                                    ) { Text(if(isSelected) "Remove" else "Add", color = if(isSelected) Color.White else DeepInkBlack) }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(24.dp))
                        if (selectedAttributes.isNotEmpty()) {
                            Button(onClick = { 
                                soundManager?.playClick()
                                phase = OnboardingPhase.ACTIVITIES 
                            }, colors = ButtonDefaults.buttonColors(containerColor = ForestGreen), modifier = Modifier.fillMaxWidth()) { 
                                Text("Venture to the Forge (${selectedAttributes.size} Chosen)", color = Color.White) 
                            }
                        }
                    }
                    OnboardingPhase.ACTIVITIES -> {
                        Box(modifier = Modifier.fillMaxWidth().background(AntiqueGold).padding(8.dp)) {
                            Text("Select your Disciplines (${selectedDisciplines.size}/10)", color = DeepInkBlack, fontWeight = FontWeight.Black, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
                        }
                        Spacer(Modifier.height(16.dp))
                        
                        Column {
                            selectedAttributes.forEach { attr ->
                                OutlinedText(attr.name.uppercase(), style = MaterialTheme.typography.labelSmall, color = AntiqueGold)
                                Spacer(Modifier.height(8.dp))
                                viewModel.disciplineRegistry.filter { it.attributeCategory == attr.name }.forEach { disc ->
                                    val isSelected = selectedDisciplines.contains(disc)
                                    val isMaxed = selectedDisciplines.size >= 10 && !isSelected
                                    
                                    Card(
                                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(enabled = !isMaxed) {
                                            soundManager?.playClick()
                                            if (isSelected) selectedDisciplines.remove(disc) else selectedDisciplines.add(disc)
                                        }.graphicsLayer { alpha = if(isMaxed) 0.4f else 1.0f },
                                        colors = CardDefaults.cardColors(containerColor = if(isSelected) ForestGreen else Color.Black.copy(alpha = 0.4f)),
                                        border = BorderStroke(1.dp, if(isSelected) Color.White else Color.Gray)
                                    ) {
                                        Text(disc.name, color = Color.White, modifier = Modifier.padding(12.dp), fontSize = 14.sp)
                                    }
                                }
                                Spacer(Modifier.height(24.dp))
                            }
                        }
                        
                        if (selectedDisciplines.isNotEmpty()) {
                            Button(onClick = { 
                                soundManager?.playClick()
                                phase = OnboardingPhase.CLASS_GATE 
                            }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold), modifier = Modifier.fillMaxWidth()) { 
                                Text("Seal the Disciplines", color = DeepInkBlack) 
                            }
                        }
                    }
                    OnboardingPhase.CLASS_GATE -> {
                        val descriptor = ClassLexicons.get(selectedGateClass)
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(descriptor.icon, contentDescription = null, tint = AntiqueGold, modifier = Modifier.size(120.dp))
                                Spacer(Modifier.height(32.dp))
                                TypewriterText(descriptor.title, MaterialTheme.typography.displayMedium, AntiqueGold)
                                Spacer(Modifier.height(16.dp))
                                Text(
                                    descriptor.description,
                                    color = ParchmentCream,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(Modifier.height(48.dp))
                                Button(
                                    onClick = { 
                                        soundManager?.playClick()
                                        phase = OnboardingPhase.WAR_ROOM 
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)
                                ) { Text(descriptor.commitmentText, color = DeepInkBlack) }
                                
                                Spacer(Modifier.height(16.dp))
                                TextButton(onClick = { 
                                    selectedGateClass = when(selectedGateClass) {
                                        "KNIGHT" -> "MAGE"
                                        "MAGE" -> "ARTISAN"
                                        "ARTISAN" -> "RANGER"
                                        else -> "KNIGHT"
                                    }
                                }) { Text("Change Path", color = Color.Gray) }
                            }
                        }
                    }
                    OnboardingPhase.WAR_ROOM -> {
                        val descriptor = ClassLexicons.get(selectedGateClass)
                        OutlinedText(descriptor.magnitudeTitle, style = MaterialTheme.typography.displaySmall, color = AntiqueGold, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(32.dp))
                        
                        Column {
                            selectedDisciplines.forEach { disc ->
                                val config = getTacticalConfig(disc.name)
                                baselines.putIfAbsent(disc.name, 5f)
                                goals.putIfAbsent(disc.name, 10f)
                                
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
                                    colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.5f)),
                                    border = BorderStroke(1.dp, AntiqueGold.copy(alpha = 0.3f))
                                ) {
                                    Column(Modifier.padding(16.dp)) {
                                        Text(disc.name, color = AntiqueGold, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                                        Spacer(Modifier.height(16.dp))
                                        
                                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Column(Modifier.weight(1f)) {
                                                Text("BASELINE", color = Color.Gray, style = MaterialTheme.typography.labelSmall)
                                                when (config.tool) {
                                                    TacticalTool.LD -> LogisticsDial("Start", config.unit, baselines[disc.name] ?: 5f) { baselines[disc.name] = it }
                                                    TacticalTool.AM -> ArchiveMeter("Start", baselines[disc.name] ?: 0f) { baselines[disc.name] = it }
                                                    TacticalTool.HG -> HourglassGauge("Mins", baselines[disc.name] ?: 15f) { baselines[disc.name] = it }
                                                    TacticalTool.LT -> LedgerToggle("Done?", (baselines[disc.name] ?: 0f) > 0f) { baselines[disc.name] = if(it) 1f else 0f }
                                                    TacticalTool.TG -> ThresholdGauge("Start", baselines[disc.name] ?: 20f) { baselines[disc.name] = it }
                                                }
                                            }
                                            Spacer(Modifier.width(32.dp))
                                            Column(Modifier.weight(1f)) {
                                                Text("ULTIMATE", color = AntiqueGold, style = MaterialTheme.typography.labelSmall)
                                                when (config.tool) {
                                                    TacticalTool.LD -> LogisticsDial("Goal", config.unit, goals[disc.name] ?: 20f) { goals[disc.name] = it }
                                                    TacticalTool.AM -> ArchiveMeter("Goal", goals[disc.name] ?: 30f) { goals[disc.name] = it }
                                                    TacticalTool.HG -> HourglassGauge("Mins", goals[disc.name] ?: 60f) { goals[disc.name] = it }
                                                    TacticalTool.LT -> Text("Goal Set", color = Color.Gray)
                                                    TacticalTool.TG -> ThresholdGauge("Goal", goals[disc.name] ?: 80f) { goals[disc.name] = it }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(48.dp))
                        Button(
                            onClick = { 
                                soundManager?.playClick()
                                phase = OnboardingPhase.CRUCIBLE 
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
                        ) { Text("FORGE YOUR DESTINY", color = Color.White) }
                    }
                    OnboardingPhase.CRUCIBLE -> {
                        val descriptor = ClassLexicons.get(selectedGateClass)
                        var crucibleIndex by remember { mutableIntStateOf(0) }
                        
                        LaunchedEffect(Unit) {
                            while(crucibleIndex < descriptor.crucibleLines.size - 1) {
                                delay(2000)
                                crucibleIndex++
                            }
                            delay(2000)
                            phase = OnboardingPhase.DESTINY
                        }
                        
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                val tint = when(selectedGateClass) {
                                    "KNIGHT" -> CrimsonRed
                                    "MAGE" -> RoyalBlue
                                    "ARTISAN" -> AntiqueGold
                                    else -> ForestGreen
                                }
                                Icon(Icons.Default.Whatshot, contentDescription = null, tint = tint, modifier = Modifier.size(100.dp))
                                Spacer(Modifier.height(32.dp))
                                TypewriterText(descriptor.crucibleLines[crucibleIndex], MaterialTheme.typography.titleLarge, ParchmentCream)
                            }
                        }
                    }
                    OnboardingPhase.DESTINY -> {
                        val descriptor = ClassLexicons.get(selectedGateClass)
                        OutlinedText("THE STARS HAVE SPOKEN", style = MaterialTheme.typography.labelSmall, color = AntiqueGold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedText("Your Grand Campaign: ${descriptor.campaignTitle}", style = MaterialTheme.typography.displayLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(32.dp))
                        
                        // CLASS SELECTION TILES
                        val classes = listOf("KNIGHT", "MAGE", "ARTISAN", "RANGER")
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            classes.forEach { cls ->
                                val clsDesc = ClassLexicons.get(cls)
                                val isRecommended = cls == viewModel.suggestedClass.value
                                Card(
                                    modifier = Modifier.fillMaxWidth().clickable { 
                                        soundManager?.playClick()
                                        selectedGateClass = cls 
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if(selectedGateClass == cls) AntiqueGold else Color.Black.copy(alpha = 0.6f)
                                    ),
                                    border = BorderStroke(2.dp, if(isRecommended) RoyalBlue else Color.Gray)
                                ) {
                                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                        Icon(clsDesc.icon, contentDescription = null, tint = if(selectedGateClass == cls) DeepInkBlack else AntiqueGold, modifier = Modifier.size(32.dp))
                                        Spacer(Modifier.width(16.dp))
                                        Column(Modifier.weight(1f)) {
                                            Text(cls, color = if(selectedGateClass == cls) DeepInkBlack else ParchmentCream, style = MaterialTheme.typography.titleLarge)
                                            if (isRecommended) Text("Stars Recommend", color = if(selectedGateClass == cls) DeepInkBlack.copy(alpha=0.7f) else AntiqueGold, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(48.dp))
                        val scope = rememberCoroutineScope()
                        Button(
                            onClick = { 
                                soundManager?.playClick()
                                scope.launch {
                                    viewModel.finalizeCharacterAndCampaign(
                                        name, selectedGateClass, 
                                        selectedAttributes.map { it.name }, 
                                        baselines.toMap(), goals.toMap(), 
                                        context
                                    )
                                    onComplete()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) { Text("Seal My Path", color = ParchmentCream) }
                    }
                }
            }
        }
    }
}

// --- STORY SELECTION SCREEN ---
@Composable
fun StorySelectionScreen(viewModel: BecomingViewModel, soundManager: SoundManager?, context: android.content.Context, onStorySealed: () -> Unit) {
    val stories by viewModel.generatedStories.collectAsState()
    val charState by viewModel.charState.collectAsState()
    val haptic = LocalHapticFeedback.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp).verticalScroll(rememberScrollState())) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.1f)))
        OutlinedText("DESTINY SEALED", style = MaterialTheme.typography.labelSmall, color = AntiqueGold, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        OutlinedText("Path of the ${charState.heroClass}", style = MaterialTheme.typography.displayLarge, color = ParchmentCream, modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
        
        Spacer(Modifier.height(32.dp))
        OutlinedText("CHOOSE YOUR GRAND CAMPAIGN", style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
        Spacer(Modifier.height(16.dp))

        stories.forEach { story ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).clickable {
                    soundManager?.playClick()
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.sealCampaign(story, context)
                    onStorySealed()
                },
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.8f)),
                border = BorderStroke(1.dp, AntiqueGold)
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(story.title, style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
                    Spacer(Modifier.height(8.dp))
                    Text(story.loreSummary, style = MaterialTheme.typography.bodyLarge, color = ParchmentCream)
                    Spacer(Modifier.height(16.dp))
                    Text("BOUNTY: ${story.totalRequiredXp} XP", color = Color.LightGray, style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// --- CUSTOM QUEST SCREEN ---
@Composable
fun CustomQuestScreen(viewModel: BecomingViewModel, soundManager: SoundManager?, onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    val state by viewModel.charState.collectAsState()
    
    var baseTask by remember { mutableStateOf("") }
    var timeScale by remember { mutableStateOf("DAILY") }
    var selectedAttr by remember { mutableStateOf(state.selectedAttributeNames.firstOrNull() ?: "Mindfulness") }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)))
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            OutlinedText("FORGE A NEW BOUNTY", style = MaterialTheme.typography.displayLarge, color = AntiqueGold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(8.dp))
            OutlinedText("The realm bends to your will, ${state.heroClass}.", color = ParchmentCream, style = MaterialTheme.typography.bodyLarge, textAlign = TextAlign.Center)
            Spacer(Modifier.height(48.dp))

            OutlinedText("The Real-World Task", color = AntiqueGold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = baseTask,
                onValueChange = { 
                    if (it.length > baseTask.length) soundManager?.playClick()
                    baseTask = it 
                },
                modifier = Modifier.fillMaxWidth().height(120.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AntiqueGold, 
                    focusedTextColor = ParchmentCream, 
                    unfocusedTextColor = ParchmentCream,
                    focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                ),
                placeholder = { Text("e.g., Draft the methodology chapter, or run 5km.", color = Color.Gray) }
            )

            Spacer(Modifier.height(24.dp))

            OutlinedText("Target Domain", color = AntiqueGold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(8.dp))
            Column(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.4f)).padding(8.dp)) {
                state.selectedAttributeNames.forEach { attr ->
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().clickable { selectedAttr = attr; soundManager?.playClick() }) {
                        RadioButton(selected = selectedAttr == attr, onClick = { selectedAttr = attr; soundManager?.playClick() }, colors = RadioButtonDefaults.colors(selectedColor = AntiqueGold))
                        Text(attr, color = if(selectedAttr == attr) AntiqueGold else Color.Gray)
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            OutlinedText("Magnitude of the Trial", color = AntiqueGold, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                listOf("DAILY", "WEEKLY", "MONTHLY").forEach { scale ->
                    val isSelected = timeScale == scale
                    Button(
                        onClick = { 
                            soundManager?.playClick()
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            timeScale = scale 
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) CrimsonRed else Color.Black.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(scale, color = ParchmentCream, fontSize = 10.sp)
                    }
                }
            }

            Spacer(Modifier.height(48.dp))

            Button(
                onClick = {
                    if (baseTask.isNotBlank()) {
                        soundManager?.playClick()
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.addCustomQuest(baseTask, timeScale, selectedAttr)
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text("SEAL THE VOW", color = DeepInkBlack, style = MaterialTheme.typography.titleLarge)
            }
            
            Spacer(Modifier.height(16.dp))
            TextButton(onClick = onBack) { OutlinedText("Cancel", style = MaterialTheme.typography.bodyLarge, color = Color.LightGray) }
        }
    }
}

// --- DASHBOARD ---
@Composable
fun DashboardScreen(
    viewModel: BecomingViewModel, 
    soundManager: SoundManager?, 
    onQuestSelected: (String) -> Unit, 
    onCreateQuest: () -> Unit,
    onOpenSettings: () -> Unit
) {
    val state by viewModel.charState.collectAsState()
    val activeQuests by viewModel.activeQuests.collectAsState()
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // 1. Level Progress
            Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.85f)).padding(24.dp)) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedText("LEVEL PROGRESS", style = MaterialTheme.typography.bodyLarge, color = AntiqueGold, modifier = Modifier.weight(1f))
                        IconButton(onClick = onOpenSettings) {
                            Icon(Icons.Default.Settings, contentDescription = "The Armory", tint = AntiqueGold)
                        }
                    }
                    OutlinedText("TO LVL ${state.globalLevel + 1}", style = MaterialTheme.typography.titleLarge, color = ParchmentCream)
                    Spacer(Modifier.height(16.dp))
                    
                    val xpNeeded = state.globalLevel * 1000
                    val targetProgress = state.currentXp.toFloat() / xpNeeded.toFloat()
                    val animatedProgress by animateFloatAsState(
                        targetValue = targetProgress,
                        animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing),
                        label = "xp_whoosh"
                    )
                    
                    LinearProgressIndicator(
                        progress = { animatedProgress },
                        modifier = Modifier.fillMaxWidth().height(12.dp).clip(RoundedCornerShape(6.dp)),
                        color = ForestGreen, trackColor = DeepInkBlack
                    )
                    OutlinedText("${state.currentXp} / $xpNeeded XP", color = Color.Gray, modifier = Modifier.align(Alignment.End), style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(Modifier.height(16.dp))

            // 2. Character Header
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).background(AntiqueGold, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    OutlinedText("LVL\n${state.globalLevel}", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    OutlinedText(state.heroName, style = MaterialTheme.typography.displayLarge, color = DeepInkBlack)
                    OutlinedText("The ${state.heroClass}", color = Color.DarkGray, style = MaterialTheme.typography.bodyLarge)
                }
                
                RuneSlate(
                    modifier = Modifier.width(180.dp),
                    streakStatus = if (state.campfireStreak == 0) List(7) { false } else List(7) { i -> i < (state.campfireStreak - 1) % 7 + 1 },
                    classTheme = state.heroClass.toClassType()
                )
            }
            if (state.campfireStreak > 0) {
                OutlinedText("${state.campfireStreak} DAY STREAK", style = MaterialTheme.typography.labelSmall, color = CrimsonRed, modifier = Modifier.padding(start = 16.dp))
            }

            Spacer(Modifier.height(32.dp))

            // 3. Attributes (Domains of Mastery)
            if (state.selectedAttributeNames.isNotEmpty()) {
                OutlinedText("DOMAINS OF MASTERY", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(Modifier.height(8.dp))
                
                Column(Modifier.padding(horizontal = 16.dp)) {
                    state.selectedAttributeNames.forEach { attrName ->
                        val xpValue = when(attrName) {
                            "Physical Fitness" -> state.attrPhysicalFitness
                            "Mental Focus" -> state.attrMentalFocus
                            "Financial Wealth" -> state.attrFinancialWealth
                            "Creative Output" -> state.attrCreativeOutput
                            "Social Charisma" -> state.attrSocialCharisma
                            "Emotional Resilience" -> state.attrEmotionalResilience
                            "Deep Knowledge" -> state.attrDeepKnowledge
                            "Career Growth" -> state.attrCareerGrowth
                            "Mindfulness" -> state.attrMindfulness
                            "Physical Endurance" -> state.attrPhysicalEndurance
                            else -> 0f
                        }
                        val level = (xpValue / 1000).toInt() + 1
                        val progress = (xpValue % 1000) / 1000f

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
                            Column(Modifier.weight(1f)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(attrName, color = DeepInkBlack, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
                                    Text("LVL $level", color = DeepInkBlack, style = MaterialTheme.typography.bodySmall)
                                }
                                LinearProgressIndicator(
                                    progress = { progress },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(CircleShape),
                                    color = AntiqueGold,
                                    trackColor = Color.LightGray.copy(alpha = 0.3f)
                                )
                            }
                        }
                    }
                }
                Spacer(Modifier.height(32.dp))
            }

            // 4. The Ledger of Tasks
            OutlinedText("THE LEDGER OF TASKS", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            activeQuests.forEachIndexed { index, quest ->
                val tilt = remember(quest.id) { listOf(-1.5f, 1f, -0.5f, 1.5f, 0.5f).random() }
                val questRarityColor = when {
                    quest.xp >= 300 -> AntiqueGold
                    quest.xp >= 200 -> RoyalBlue
                    else -> Color.DarkGray
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .graphicsLayer { rotationZ = tilt }
                        .shadow(elevation = 8.dp, shape = RoundedCornerShape(4.dp))
                        .clickable {
                            soundManager?.playClick()
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            onQuestSelected(quest.id)
                        },
                    colors = CardDefaults.cardColors(containerColor = ParchmentCream),
                    shape = RoundedCornerShape(4.dp),
                    border = BorderStroke(2.dp, questRarityColor)
                ) {
                    Box {
                        // --- PIN (RED DOT) ---
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (-6).dp)
                                .background(CrimsonRed, CircleShape)
                                .border(1.dp, AntiqueGold.copy(alpha = 0.5f), CircleShape)
                        )

                        Row(Modifier.padding(16.dp).padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            val icon = when(quest.iconType) { "gps" -> Icons.Default.Map; "tap" -> Icons.Default.PanTool; "camera" -> Icons.Default.CameraAlt; "timer" -> Icons.Default.HourglassEmpty; else -> Icons.Default.Bedtime }
                            Icon(icon, contentDescription = null, tint = DeepInkBlack)
                            Spacer(Modifier.width(16.dp))
                            Column(Modifier.weight(1f)) {
                                val questFlavor = viewModel.generateQuestNarrative(quest)
                                Text(questFlavor.title, style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, fontSize = 18.sp)
                                Text(questFlavor.narrativeDesc, color = Color.DarkGray, fontSize = 13.sp)
                                Spacer(Modifier.height(4.dp))
                                Text("BOUNTY: ${quest.targetValue} ${quest.unit}", color = DeepInkBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                            }
                            Text("+${quest.xp}", color = questRarityColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(100.dp))
        }

        FloatingActionButton(
            onClick = { 
                soundManager?.playClick()
                onCreateQuest() 
            },
            containerColor = CrimsonRed,
            contentColor = ParchmentCream,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Forge Bounty")
        }
    }
}

// --- SETTINGS SCREEN ---
@Composable
fun SettingsScreen(viewModel: BecomingViewModel, soundManager: SoundManager?, onBack: () -> Unit) {
    val charState by viewModel.charState.collectAsState()
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.5f)))
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(32.dp))
            OutlinedText("THE ARMORY", style = MaterialTheme.typography.displayLarge, color = AntiqueGold)
            OutlinedText("Manage your ledger and gear.", style = MaterialTheme.typography.bodyLarge, color = ParchmentCream)
            
            Spacer(Modifier.height(48.dp))

            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                border = BorderStroke(1.dp, AntiqueGold)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("Mystic Echoes", style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
                        Text("Toggle background music and effects.", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = charState.isSoundEnabled,
                        onCheckedChange = { 
                            soundManager?.playClick()
                            viewModel.toggleSound(context) 
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = AntiqueGold,
                            checkedTrackColor = ForestGreen,
                            uncheckedThumbColor = Color.Gray,
                            uncheckedTrackColor = DeepSlate
                        )
                    )
                }
            }

            Spacer(Modifier.height(48.dp))

            OutlinedText("ABOUT THE LEDGER", style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
            Spacer(Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedText("Created by Md. Tawsif Mostafiz", style = MaterialTheme.typography.bodyLarge, color = ParchmentCream)
                OutlinedText("All Rights Reserved", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }

            Spacer(Modifier.height(64.dp))

            Button(
                onClick = { viewModel.resetProgress(context) },
                colors = ButtonDefaults.buttonColors(containerColor = CrimsonRed),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("RESET CHRONICLE (DANGER)", color = Color.White)
            }

            Spacer(Modifier.height(16.dp))
            
            Button(
                onClick = { 
                    soundManager?.playClick()
                    onBack() 
                },
                colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold),
                modifier = Modifier.fillMaxWidth().height(56.dp)
            ) {
                Text("Return to the Realm", color = DeepInkBlack)
            }
        }
    }
}

// --- QUEST LOOP ---
@Composable
fun QuestLoopScreen(quest: QuestEntity, viewModel: BecomingViewModel, soundManager: SoundManager?, onComplete: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }
    var actualProgress by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current
    val state by viewModel.charState.collectAsState()

    val flavor = remember(quest.id) { viewModel.generateQuestNarrative(quest) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.4f)))
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            when (phase) {
                0 -> { // Briefing
                    OutlinedText(flavor.title, style = MaterialTheme.typography.labelSmall, color = AntiqueGold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedText(flavor.title, style = MaterialTheme.typography.displayLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    OutlinedText(flavor.narrativeDesc, style = MaterialTheme.typography.bodyLarge, color = Color.LightGray, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(48.dp))
                    
                    HoldToCommitButton(
                        text = "HOLD TO COMMIT",
                        onCommitted = {
                            soundManager?.playClick()
                            phase = 1
                        }
                    )
                }
                1 -> { // Trial
                    // Interactive Phases
                    val qType = when(quest.associatedTrait) {
                        "Physical Endurance" -> QuestType.TIMER
                        "Physical Fitness" -> QuestType.REPS
                        "Mental Focus" -> QuestType.TIMER
                        "Deep Knowledge" -> QuestType.TIMER
                        else -> QuestType.CHECK
                    }

                    when (qType) {
                        QuestType.TIMER -> TimerPhase(if(quest.targetValue > 0) quest.targetValue else 30, soundManager) { progress -> 
                            actualProgress = progress
                            phase = 2 
                        }
                        QuestType.REPS -> RepsPhase(if(quest.targetValue > 0) quest.targetValue else 10, soundManager) { progress -> 
                            actualProgress = progress
                            phase = 2 
                        }
                        else -> CheckPhase(soundManager) { 
                            actualProgress = 1
                            phase = 2 
                        }
                    }
                }
                2 -> { // Aftermath
                    var journal by remember { mutableStateOf("") }
                    OutlinedText("THE AFTERMATH", style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
                    Spacer(Modifier.height(16.dp))
                    OutlinedText(flavor.aftermathLore, style = MaterialTheme.typography.bodyLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                    Spacer(Modifier.height(24.dp))
                    OutlinedTextField(
                        value = journal, 
                        onValueChange = { 
                            if (it.length > journal.length) soundManager?.playClick()
                            journal = it 
                        }, 
                        modifier = Modifier.fillMaxWidth().height(150.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AntiqueGold, 
                            focusedTextColor = ParchmentCream, 
                            unfocusedTextColor = ParchmentCream,
                            focusedContainerColor = Color.Black.copy(alpha = 0.6f)
                        ),
                        placeholder = { Text("Log your reflections in the ledger...", color = Color.Gray) }
                    )
                    Spacer(Modifier.height(24.dp))
                    if (journal.isNotBlank()) {
                        Button(
                            onClick = { 
                                soundManager?.playClick()
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.grantBounty(quest.id, quest.xp)
                                onComplete() 
                            }, 
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen), 
                            modifier = Modifier.fillMaxWidth().height(56.dp)
                        ) { Text("Seal Record", color = ParchmentCream) }
                    }
                }
            }
        }
    }
}

// --- SUB-PHASES ---

@Composable
fun TimerPhase(minutes: Int, soundManager: SoundManager?, onDone: (Int) -> Unit) {
    var timeLeft by remember { mutableIntStateOf(minutes * 60) }
    val totalTime = minutes * 60
    var running by remember { mutableStateOf(true) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(running) {
        while (running && timeLeft > 0) {
            delay(1000)
            timeLeft--
        }
        if (timeLeft == 0) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedText("THE RUNIC CIRCLE", style = MaterialTheme.typography.labelSmall, color = AntiqueGold)
        Spacer(Modifier.height(48.dp))
        Box(modifier = Modifier.size(240.dp).border(4.dp, AntiqueGold, RoundedCornerShape(120.dp)), contentAlignment = Alignment.Center) {
            OutlinedText("${timeLeft / 60}:${(timeLeft % 60).toString().padStart(2, '0')}", style = MaterialTheme.typography.displayLarge, color = ParchmentCream)
        }
        Spacer(Modifier.height(48.dp))
        OutlinedText("Do not break the runic circle by leaving the ledger.", style = MaterialTheme.typography.bodyLarge, color = Color.LightGray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { 
                soundManager?.playClick()
                val completedMins = (totalTime - timeLeft) / 60
                onDone(completedMins) 
            }, 
            colors = ButtonDefaults.buttonColors(containerColor = if (timeLeft == 0) ForestGreen else Color.Black.copy(alpha = 0.7f))
        ) { 
            Text(if (timeLeft == 0) "Trial Concluded" else "End Early", color = ParchmentCream) 
        }
    }
}

@Composable
fun RepsPhase(target: Int, soundManager: SoundManager?, onDone: (Int) -> Unit) {
    var current by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedText("THE IRON FORGE", style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
        Spacer(Modifier.height(48.dp))
        Box(
            modifier = Modifier.size(240.dp).border(4.dp, AntiqueGold, RoundedCornerShape(120.dp)).clickable {
                if (current < target) {
                    soundManager?.playClick()
                    current++
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                }
            }, 
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedText("$current", style = MaterialTheme.typography.displayLarge, color = ParchmentCream)
                OutlinedText("OF $target", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
        }
        Spacer(Modifier.height(48.dp))
        OutlinedText("Strike the anvil for every completed set.", style = MaterialTheme.typography.bodyLarge, color = Color.LightGray, textAlign = TextAlign.Center)
        Spacer(Modifier.height(32.dp))
        
        Button(
            onClick = { 
                soundManager?.playClick()
                onDone(current) 
            }, 
            colors = ButtonDefaults.buttonColors(containerColor = if (current >= target) ForestGreen else Color.Black.copy(alpha = 0.6f))
        ) { 
            Text(if (current >= target) "Trial Concluded" else "End Early", color = ParchmentCream) 
        }
    }
}

@Composable
fun CheckPhase(soundManager: SoundManager?, onDone: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedText("THE TRIAL", style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
        Spacer(Modifier.height(48.dp))
        Box(modifier = Modifier.size(200.dp).border(BorderStroke(4.dp, AntiqueGold), RoundedCornerShape(100.dp)).clickable {
            soundManager?.playClick()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            onDone()
        }, contentAlignment = Alignment.Center) {
            OutlinedText("TAP WHEN\nCOMPLETED", style = MaterialTheme.typography.titleLarge, color = ParchmentCream, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun RewardScreen(xp: Int, isLevelUp: Boolean, onBack: () -> Unit) {
    val haptic = LocalHapticFeedback.current
    var displayXp by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        while(displayXp < xp) {
            displayXp += 5
            delay(20)
        }
        displayXp = xp
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(if(isLevelUp) AntiqueGold.copy(alpha = 0.9f) else Color.Black.copy(alpha = 0.7f)))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            OutlinedText(if(isLevelUp) "LEVEL UP!" else "BOUNTY CLAIMED", style = MaterialTheme.typography.displayLarge, color = if(isLevelUp) DeepInkBlack else AntiqueGold)
            OutlinedText("+$displayXp XP", style = MaterialTheme.typography.displayLarge, color = if(isLevelUp) DeepInkBlack else ParchmentCream, modifier = Modifier.graphicsLayer { scaleX = 1.5f; scaleY = 1.5f })
            Spacer(Modifier.height(48.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = if(isLevelUp) DeepInkBlack else AntiqueGold)) { 
                Text("Return to Realm", color = if(isLevelUp) AntiqueGold else DeepInkBlack) 
            }
        }
    }
}

@Composable
fun HoldToCommitButton(text: String, onCommitted: () -> Unit) {
    var isPressed by remember { mutableStateOf(false) }
    var progress by remember { mutableFloatStateOf(0f) }
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(isPressed) {
        if (isPressed) {
            val startTime = System.currentTimeMillis()
            val duration = 2000L
            while (isPressed && progress < 1f) {
                progress = ((System.currentTimeMillis() - startTime).toFloat() / duration).coerceAtMost(1f)
                if (progress > 0.1f) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                delay(16)
            }
            if (progress >= 1f) {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onCommitted()
            }
        } else {
            progress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.5f))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        try {
                            awaitRelease()
                        } finally {
                            isPressed = false
                        }
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(progress)
                .fillMaxHeight()
                .background(CrimsonRed.copy(alpha = 0.9f))
                .align(Alignment.CenterStart)
        )
        Text(
            text = text, 
            style = MaterialTheme.typography.titleLarge, 
            color = if (progress > 0.5f) ParchmentCream else AntiqueGold
        )
    }
}
