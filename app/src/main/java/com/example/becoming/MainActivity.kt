package com.example.becoming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import com.example.becoming.ui.theme.BecomingTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BecomingTheme {
                BecomingApp()
            }
        }
    }
}

@Composable
fun BecomingApp() {
    val navController = rememberNavController()
    val viewModel: BecomingViewModel = viewModel()

    NavHost(navController = navController, startDestination = "onboarding") {

        // 1. Onboarding Route: Collects Name, DOB, Traits, Goal, and Timeline
        composable("onboarding") {
            OnboardingScreen(
                onComplete = { name, dob, traits, goal, timeline, path ->
                    // Trigger Gemini to generate 3 personalized storylines!
                    viewModel.initializeUserAndGenerateStories(name, dob, traits, goal, timeline, path)
                    navController.navigate("story_selection") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // 2. Story Selection Route: User picks or customizes their Grand Campaign
        composable("story_selection") {
            StorySelectionScreen(
                viewModel = viewModel,
                onStorySealed = {
                    navController.navigate("dashboard") {
                        popUpTo("story_selection") { inclusive = true }
                    }
                }
            )
        }

        // 3. Dashboard Route: The Realm Map and Notice Board
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onQuestSelected = { questId: String ->
                    navController.navigate("quest/$questId")
                },
                onOpenInventory = {
                    navController.navigate("inventory")
                }
            )
        }

        // Alchemist Inventory Route
        composable("inventory") {
            AlchemistInventoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        // 4. Quest Loop Route: The 3-Phase Immersive Trial
        composable("quest/{questId}") { backStackEntry ->
            val questId = backStackEntry.arguments?.getString("questId")
            val activeQuests by viewModel.activeQuests.collectAsState()
            val quest = activeQuests.find { it.id == questId }

            if (quest != null) {
                QuestLoopScreen(
                    quest = quest,
                    onComplete = { xpReward: Int ->
                        val leveledUp = viewModel.grantBounty(xpReward)
                        navController.navigate("reward/${xpReward}/$leveledUp") {
                            popUpTo("dashboard")
                        }
                    }
                )
            }
        }

        // 5. Reward Route: The Dopamine Hit
        composable("reward/{xp}/{leveled}") { backStackEntry ->
            val xp = backStackEntry.arguments?.getString("xp")?.toInt() ?: 0
            val leveled = backStackEntry.arguments?.getString("leveled")?.toBoolean() ?: false

            RewardScreen(viewModel = viewModel, xp = xp, isLevelUp = leveled) {
                navController.popBackStack("dashboard", inclusive = false)
            }
        }
    }
}
