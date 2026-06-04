package com.example.becoming

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Data Models
data class Quest(val id: String, val title: String, val desc: String, val xp: Int, val requiresProof: Boolean = false)

data class CharacterState(
    val name: String = "",
    val heroClass: String = "Knight",
    val level: Int = 1,
    val currentXp: Int = 0,
    val traits: List<String> = emptyList(),
    val campaignTitle: String = "The Iron Gauntlet",
    val campaignXp: Int = 0,
    val totalCampaignXp: Int = 30000,
    val dailyProtein: Int = 0,
    val dailyCarbs: Int = 0
)

class BecomingViewModel : ViewModel() {
    private val geminiService = GeminiService()

    private val _charState = MutableStateFlow(CharacterState())
    val charState = _charState.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    private val _alchemistResult = MutableStateFlow<AlchemistResult?>(null)
    val alchemistResult = _alchemistResult.asStateFlow()

    // Quests with proof requirements
    val activeQuests = listOf(
        Quest("1", "The Rune of Warding", "Deep focus for 25 mins.", 150),
        Quest("2", "The Iron Forge", "Strength training at the anvil.", 200, requiresProof = true),
        Quest("3", "The Long Fast", "16 hours of Monk's Discipline.", 300),
        Quest("4", "The Morning Dew", "Drink a glass of water at sunrise.", 50, requiresProof = true)
    )

    fun initializeUser(name: String, path: String) {
        _charState.update { it.copy(name = name, heroClass = path) }
    }

    fun analyzeMeal(bitmap: Bitmap) {
        viewModelScope.launch {
            _isProcessing.value = true
            val result = geminiService.analyzeFood(bitmap)
            _isProcessing.value = false
            _alchemistResult.value = result
            
            result?.let {
                _charState.update { state ->
                    state.copy(
                        dailyProtein = state.dailyProtein + it.proteinGrams,
                        dailyCarbs = state.dailyCarbs + it.carbsGrams
                    )
                }
                // Grant some minor XP for logging
                grantBounty(25)
            }
        }
    }

    fun clearAlchemistResult() {
        _alchemistResult.value = null
    }

    suspend fun verifyProof(bitmap: Bitmap, questTitle: String): Boolean {
        _isProcessing.value = true
        val verified = geminiService.verifyQuest(bitmap, questTitle)
        _isProcessing.value = false
        return verified
    }

    // Leveling Engine
    fun grantBounty(xp: Int): Boolean {
        var leveledUp = false
        _charState.update { state ->
            var newXp = state.currentXp + xp
            var newLevel = state.level
            val xpNeeded = state.level * 1000

            if (newXp >= xpNeeded) {
                newXp -= xpNeeded
                newLevel++
                leveledUp = true
            }
            state.copy(
                level = newLevel,
                currentXp = newXp,
                campaignXp = state.campaignXp + xp
            )
        }
        return leveledUp
    }
}
