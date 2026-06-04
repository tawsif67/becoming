package com.example.becoming

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
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

        // 1. Onboarding Route (Collects Name, Class, and Goal)
        composable("onboarding") {
            OnboardingScreen(
                onComplete = { name: String, path: String, goal: String ->
                    // Trigger the Gemini AI Call!
                    viewModel.initializeUserAndGenerateQuests(name, path, goal)
                    navController.navigate("dashboard") {
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            )
        }

        // 2. Dashboard Route
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onQuestSelected = { questId: String ->
                    navController.navigate("quest/$questId")
                }
            )
        }

        // 3. Quest Loop Route
        composable("quest/{questId}") { backStackEntry ->
            val questId = backStackEntry.arguments?.getString("questId")
            val quest = viewModel.activeQuests.value.find { it.id == questId }

            if (quest != null) {
                QuestLoopScreen(
                    quest = quest,
                    onComplete = { xpReward: Int ->
                        // Grant XP and check if user leveled up
                        val leveledUp = viewModel.grantBounty(xpReward)
                        navController.navigate("reward/${quest.xp}/$leveledUp") {
                            popUpTo("dashboard")
                        }
                    }
                )
            }
        }

        // 4. Reward / Level Up Route
        composable("reward/{xp}/{leveled}") { backStackEntry ->
            val xp = backStackEntry.arguments?.getString("xp")?.toInt() ?: 0
            val leveled = backStackEntry.arguments?.getString("leveled")?.toBoolean() ?: false

            RewardScreen(xp = xp, isLevelUp = leveled) {
                navController.popBackStack("dashboard", inclusive = false)
            }
        }
    }
}