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

@Composable
fun CampfireVideo(streakCount: Int, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.campfire_video}")
    
    if (streakCount == 0) {
        OutlinedText("💨", style = MaterialTheme.typography.displayLarge, color = Color.Gray, modifier = modifier)
    } else {
        AndroidView(
            modifier = modifier
                .size(100.dp)
                .clip(CircleShape),
            factory = { ctx ->
                VideoView(ctx).apply {
                    setVideoURI(videoUri)
                    setOnPreparedListener { mp ->
                        mp.isLooping = true
                        val duration = mp.duration
                        val seekPos = ((streakCount.coerceIn(1, 30) - 1).toFloat() / 29f * duration).toInt()
                        mp.seekTo(seekPos)
                        start()
                    }
                }
            }
        )
    }
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
            
            CampfireVideo(streakCount = state.streakCount)
            Spacer(Modifier.height(16.dp))
            
            OutlinedText("Welcome back, ${state.name}", color = Color.LightGray, style = MaterialTheme.typography.bodyLarge)
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
enum class OnboardingPhase { TITLE, POETIC1, POETIC2, POETIC3, POETIC4, POETIC5, POETIC6, NAME, TRAITS, QUIZ, DESTINY }

@Composable
fun OnboardingScreen(viewModel: BecomingViewModel, soundManager: SoundManager?, onComplete: () -> Unit) {
    val context = LocalContext.current
    var phase by remember { mutableStateOf(OnboardingPhase.TITLE) }
    var name by remember { mutableStateOf("") }
    val haptic = LocalHapticFeedback.current
    
    val suggestedClass by viewModel.suggestedClass.collectAsState()
    val questions = viewModel.quizQuestions
    val selectedTraits = remember { mutableStateListOf<TraitOption>() }
    
    var currentQuestionIndex by remember { mutableIntStateOf(0) }
    val collectedAnswers = remember { mutableStateListOf<QuizOption>() }

    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.2f)))
        AnimatedContent(targetState = phase, label = "onboarding") { targetPhase ->
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
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
                                phase = OnboardingPhase.TRAITS 
                            }, colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)) { Text("Seal Name", color = DeepInkBlack) }
                        }
                    }
                    OnboardingPhase.TRAITS -> {
                        OutlinedText("CHOOSE YOUR ATTRIBUTES", style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
                        OutlinedText("Select 3 to 5 traits you wish to master.", style = MaterialTheme.typography.bodyLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(24.dp))
                        
                        Column {
                            viewModel.availableTraits.chunked(2).forEach { rowTraits ->
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                    rowTraits.forEach { trait ->
                                        val isSelected = selectedTraits.contains(trait)
                                        Button(
                                            onClick = {
                                                soundManager?.playClick()
                                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                                if (isSelected) selectedTraits.remove(trait)
                                                else if (selectedTraits.size < 5) selectedTraits.add(trait)
                                            },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = if (isSelected) AntiqueGold else Color.Black.copy(alpha = 0.6f)
                                            ),
                                            modifier = Modifier.weight(1f)
                                        ) { 
                                            Text(trait.name, color = if(isSelected) DeepInkBlack else Color.LightGray, fontSize = 12.sp, textAlign = TextAlign.Center) 
                                        }
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(24.dp))
                        if (selectedTraits.size in 3..5) {
                            Button(
                                onClick = { 
                                    soundManager?.playClick()
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    phase = OnboardingPhase.QUIZ 
                                }, 
                                colors = ButtonDefaults.buttonColors(containerColor = AntiqueGold)
                            ) { Text("Lock Attributes", color = DeepInkBlack) }
                        } else {
                            OutlinedText("${selectedTraits.size} / 5 Selected", style = MaterialTheme.typography.bodyLarge, color = Color.LightGray)
                        }
                    }
                    OnboardingPhase.QUIZ -> {
                        val question = questions[currentQuestionIndex]
                        OutlinedText("QUESTION ${currentQuestionIndex + 1} OF ${questions.size}", style = MaterialTheme.typography.labelSmall, color = AntiqueGold)
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
                                        viewModel.calculateSuggestion(selectedTraits.toList(), collectedAnswers.toList())
                                        phase = OnboardingPhase.DESTINY
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Black.copy(alpha = 0.7f)),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) { Text(opt.text, color = ParchmentCream, textAlign = TextAlign.Center) }
                        }
                    }
                    OnboardingPhase.DESTINY -> {
                        OutlinedText("THE STARS HAVE SPOKEN", style = MaterialTheme.typography.labelSmall, color = AntiqueGold)
                        Spacer(Modifier.height(8.dp))
                        OutlinedText("The Stars suggest you are a $suggestedClass", style = MaterialTheme.typography.displayLarge, color = ParchmentCream, textAlign = TextAlign.Center)
                        Spacer(Modifier.height(32.dp))
                        
                        var selectedClass by remember { mutableStateOf(suggestedClass) }
                        val classes = listOf("Knight", "Mage", "Artisan", "Ranger")
                        
                        classes.forEach { cls ->
                            Card(
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable { 
                                    soundManager?.playClick()
                                    selectedClass = cls
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                },
                                colors = CardDefaults.cardColors(containerColor = if(selectedClass == cls) AntiqueGold else Color.Black.copy(alpha = 0.6f)),
                                border = BorderStroke(1.dp, Color.Gray)
                            ) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text(cls, color = if(selectedClass == cls) DeepInkBlack else ParchmentCream, style = MaterialTheme.typography.titleLarge)
                                    if (cls == suggestedClass) {
                                        Spacer(Modifier.width(8.dp))
                                        Text("(Recommended)", color = if(selectedClass == cls) DeepInkBlack.copy(alpha=0.6f) else AntiqueGold, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                        }
                        
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = { 
                                soundManager?.playClick()
                                viewModel.selectClassAndGenerateStories(name, selectedClass, selectedTraits.map { it.name }, context)
                                onComplete()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen),
                            modifier = Modifier.fillMaxWidth()
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
                modifier = Modifier.fillMaxWidth().height(150.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AntiqueGold, 
                    focusedTextColor = ParchmentCream, 
                    unfocusedTextColor = ParchmentCream,
                    focusedContainerColor = Color.Black.copy(alpha = 0.5f),
                    unfocusedContainerColor = Color.Black.copy(alpha = 0.3f)
                ),
                placeholder = { Text("e.g., Draft the methodology chapter, or run 5km.", color = Color.Gray) }
            )

            Spacer(Modifier.height(32.dp))

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
                        viewModel.addCustomQuest(baseTask, timeScale)
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

// --- DASHBOARD (THE REALM) ---
@Composable
fun DashboardScreen(viewModel: BecomingViewModel, soundManager: SoundManager?, onQuestSelected: (String) -> Unit, onCreateQuest: () -> Unit) {
    val state by viewModel.charState.collectAsState()
    val activeQuests by viewModel.activeQuests.collectAsState()
    val haptic = LocalHapticFeedback.current

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {

            // 1. Level Progress (Animated XP Whoosh)
            Box(modifier = Modifier.fillMaxWidth().background(Color.Black.copy(alpha = 0.85f)).padding(24.dp)) {
                Column {
                    OutlinedText("LEVEL PROGRESS", style = MaterialTheme.typography.bodyLarge, color = AntiqueGold)
                    OutlinedText("TO LVL ${state.level + 1}", style = MaterialTheme.typography.titleLarge, color = ParchmentCream)
                    Spacer(Modifier.height(16.dp))
                    
                    val xpNeeded = state.level * 1000
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

            // 2. Character Header & Campfire Streak
            Row(Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).background(AntiqueGold, RoundedCornerShape(16.dp)), contentAlignment = Alignment.Center) {
                    OutlinedText("LVL\n${state.level}", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, textAlign = TextAlign.Center)
                }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    OutlinedText(state.name, style = MaterialTheme.typography.displayLarge, color = DeepInkBlack)
                    OutlinedText("The ${state.heroClass}", color = Color.DarkGray, style = MaterialTheme.typography.bodyLarge)
                }
                
                CampfireVideo(streakCount = state.streakCount)
            }
            if (state.streakCount > 0) {
                OutlinedText("${state.streakCount} DAY STREAK", style = MaterialTheme.typography.labelSmall, color = CrimsonRed, modifier = Modifier.padding(start = 16.dp))
            }

            Spacer(Modifier.height(32.dp))

            // 3. The Ledger of Tasks
            OutlinedText("THE LEDGER OF TASKS", style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, modifier = Modifier.padding(horizontal = 16.dp))
            Spacer(Modifier.height(8.dp))

            activeQuests.forEachIndexed { index, quest ->
                val tilt = remember(quest.id) { listOf(-2f, 1f, -1.5f, 2f, 0.5f).random() }
                val questRarityColor = when {
                    quest.xp >= 300 -> AntiqueGold
                    quest.xp >= 200 -> RoyalBlue
                    else -> Color.DarkGray
                }

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .graphicsLayer { rotationZ = tilt }
                        .shadow(elevation = 12.dp, shape = RoundedCornerShape(4.dp))
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
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (-8).dp)
                                .background(CrimsonRed, CircleShape)
                                .border(2.dp, AntiqueGold.copy(alpha = 0.5f), CircleShape)
                        )

                        Row(Modifier.padding(16.dp).padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            val icon = when(quest.iconType) { "gps" -> Icons.Default.Map; "tap" -> Icons.Default.PanTool; "camera" -> Icons.Default.CameraAlt; "timer" -> Icons.Default.HourglassEmpty; else -> Icons.Default.Bedtime }
                            Icon(icon, contentDescription = null, tint = DeepInkBlack)
                            Spacer(Modifier.width(16.dp))
                                Column(Modifier.weight(1f)) {
                                val questFlavor = viewModel.generateQuestNarrative(state.heroClass, quest.baseTask, quest.timeScale)
                                Text(questFlavor.title, style = MaterialTheme.typography.titleLarge, color = DeepInkBlack, fontSize = 20.sp)
                                Text(quest.baseTask, color = Color.DarkGray, fontSize = 14.sp)
                            }
                            Text("+${quest.xp}", color = questRarityColor, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
            
            Spacer(Modifier.height(48.dp))
            
            Column(modifier = Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                OutlinedText("Created by Md. Tawsif Mostafiz", style = MaterialTheme.typography.bodyLarge, color = Color.DarkGray)
                OutlinedText("All Rights Reserved", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            }
            
            Spacer(Modifier.height(100.dp)) // Space for FAB
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

// --- QUEST LOOP ---
@Composable
fun QuestLoopScreen(quest: Quest, viewModel: BecomingViewModel, soundManager: SoundManager?, onComplete: () -> Unit) {
    var phase by remember { mutableIntStateOf(0) }
    var actualProgress by remember { mutableIntStateOf(0) }
    val haptic = LocalHapticFeedback.current
    val context = LocalContext.current
    val state by viewModel.charState.collectAsState()

    val flavor = remember(quest.id) { viewModel.generateQuestNarrative(state.heroClass, quest.baseTask, quest.timeScale) }

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
                    when (quest.type) {
                        QuestType.TIMER -> TimerPhase(quest.targetValue, soundManager) { progress -> 
                            actualProgress = progress
                            phase = 2 
                        }
                        QuestType.REPS -> RepsPhase(quest.targetValue, soundManager) { progress -> 
                            actualProgress = progress
                            phase = 2 
                        }
                        QuestType.INPUT -> InputPhase(quest.targetValue, quest.unit, soundManager) { progress -> 
                            actualProgress = progress
                            phase = 2 
                        }
                        else -> CheckPhase(soundManager) { 
                            actualProgress = quest.targetValue
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
                                viewModel.grantPartialBounty(quest, actualProgress, context)
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
            colors = ButtonDefaults.buttonColors(containerColor = if (current >= target) ForestGreen else Color.Black.copy(alpha = 0.7f))
        ) { 
            Text(if (current >= target) "Trial Concluded" else "End Early", color = ParchmentCream) 
        }
    }
}

@Composable
fun InputPhase(target: Int, unit: String, soundManager: SoundManager?, onDone: (Int) -> Unit) {
    var value by remember { mutableStateOf("") }
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        OutlinedText("THE LEDGER ENTRY", style = MaterialTheme.typography.titleLarge, color = AntiqueGold)
        Spacer(Modifier.height(48.dp))
        OutlinedTextField(
            value = value, 
            onValueChange = { 
                if (it.all { char -> char.isDigit() }) {
                    if (it.length > value.length) soundManager?.playClick()
                    value = it 
                }
            }, 
            label = { Text("How many $unit completed?", color = AntiqueGold) },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = ParchmentCream, 
                unfocusedTextColor = ParchmentCream,
                focusedContainerColor = Color.Black.copy(alpha = 0.5f)
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
        )
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { 
                soundManager?.playClick()
                onDone(value.toIntOrNull() ?: 0) 
            },
            enabled = value.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = ForestGreen)
        ) { Text("Record Progress", color = ParchmentCream) }
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

// --- REWARD SCREEN ---
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
