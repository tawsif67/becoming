package com.example.becoming

import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.becoming.ui.theme.BecomingTheme

class MainActivity : ComponentActivity() {
    private var soundManager: SoundManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundManager = SoundManager(this)

        setContent {
            BecomingTheme {
                val viewModel: BecomingViewModel = viewModel()
                val context = LocalContext.current
                
                var isDataLoaded by remember { mutableStateOf(value = false) }
                var startDestination by remember { mutableStateOf("onboarding") }
                var showVideo by remember { mutableStateOf(true) }

                LaunchedEffect(Unit) {
                    val onboardingComplete = viewModel.loadData(context)
                    startDestination = if (onboardingComplete) "title_screen" else "onboarding"
                    isDataLoaded = true
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    if (showVideo) {
                        IntroVideoScreen(onFinished = {
                            showVideo = false
                            soundManager?.startBgm()
                        })
                    } else if (isDataLoaded) {
                        // --- GLOBAL PARCHMENT BACKGROUND ---
                        Image(
                            painter = painterResource(id = R.drawable.bgi),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.FillBounds
                        )

                        val navController = rememberNavController()
                        val charState by viewModel.charState.collectAsState()

                        NavHost(navController = navController, startDestination = startDestination) {
                            composable("onboarding") {
                                OnboardingScreen(
                                    viewModel = viewModel,
                                    soundManager = soundManager,
                                    onComplete = {
                                        viewModel.saveData(context)
                                        navController.navigate("dashboard") { 
                                            popUpTo("onboarding") { inclusive = true } 
                                        }
                                    }
                                )
                            }

                            composable("title_screen") {
                                TitleScreen(
                                    state = charState,
                                    soundManager = soundManager,
                                    onContinue = {
                                        navController.navigate("dashboard") {
                                            popUpTo("title_screen") { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("dashboard") {
                                DashboardScreen(
                                    viewModel = viewModel,
                                    soundManager = soundManager,
                                    onQuestSelected = { questId -> navController.navigate("quest/$questId") },
                                    onCreateQuest = { navController.navigate("create_quest") }
                                )
                            }

                            composable("create_quest") {
                                CustomQuestScreen(
                                    viewModel = viewModel,
                                    soundManager = soundManager,
                                    onBack = { navController.popBackStack() }
                                )
                            }

                            composable("quest/{questId}") { backStackEntry ->
                                val questId = backStackEntry.arguments?.getString("questId")
                                val activeQuests by viewModel.activeQuests.collectAsState()
                                val quest = activeQuests.find { it.id == questId }

                                if (quest != null) {
                                    QuestLoopScreen(
                                        quest = quest,
                                        viewModel = viewModel,
                                        soundManager = soundManager,
                                        onComplete = {
                                            navController.popBackStack("dashboard", inclusive = false)
                                        }
                                    )
                                }
                            }

                            composable("reward/{xp}/{leveled}") { backStackEntry ->
                                val xp = backStackEntry.arguments?.getString("xp")?.toInt() ?: 0
                                val leveled = backStackEntry.arguments?.getString("leveled")?.toBoolean() ?: false

                                RewardScreen(xp = xp, isLevelUp = leveled) {
                                    navController.popBackStack("dashboard", inclusive = false)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        soundManager?.pauseBgm()
    }

    override fun onResume() {
        super.onResume()
        soundManager?.startBgm()
    }

    override fun onDestroy() {
        super.onDestroy()
        soundManager?.release()
    }
}

@Composable
fun IntroVideoScreen(onFinished: () -> Unit) {
    val context = LocalContext.current
    val videoUri = Uri.parse("android.resource://${context.packageName}/${R.raw.intro}")

    AndroidView(
        modifier = Modifier.fillMaxSize().background(Color.Black),
        factory = { ctx ->
            VideoView(ctx).apply {
                setVideoURI(videoUri)
                setOnCompletionListener { onFinished() }
                start()
            }
        }
    )
}
