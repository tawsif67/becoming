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
        composable("onboarding") {
            OnboardingScreen(
                onComplete = { name, path ->
                    viewModel.initializeUser(name, path)
                    navController.navigate("dashboard") { popUpTo("onboarding") { inclusive = true } }
                }
            )
        }
        composable("dashboard") {
            DashboardScreen(
                viewModel = viewModel,
                onQuestSelected = { questId -> navController.navigate("quest/$questId") },
                onOpenInventory = { navController.navigate("inventory") }
            )
        }
        composable("inventory") {
            AlchemistInventoryScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("quest/{questId}") { backStackEntry ->
            val questId = backStackEntry.arguments?.getString("questId")
            val quest = viewModel.activeQuests.find { it.id == questId }
            if (quest != null) {
                QuestLoopScreen(
                    quest = quest,
                    viewModel = viewModel,
                    onComplete = { xpReward ->
                        val leveledUp = viewModel.grantBounty(xpReward)
                        navController.navigate("reward/${xpReward}/$leveledUp") {
                            popUpTo("dashboard")
                        }
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
