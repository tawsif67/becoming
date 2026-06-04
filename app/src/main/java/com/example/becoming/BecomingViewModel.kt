package com.example.becoming

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// --- CORE MODELS ---
@Serializable
data class CampaignStory(val id: String, var title: String, var loreSummary: String, val totalRequiredXp: Int)

@Serializable
data class StoryGenerationResponse(val stories: List<CampaignStory>)

data class Quest(val id: String, val path: String, val title: String, val desc: String, val xp: Int, val iconType: String, val requiresProof: Boolean = false)

data class CharacterState(
    val name: String = "",
    val dob: String = "",
    val heroClass: String = "Unknown",
    val level: Int = 1,
    val currentXp: Int = 0,
    val traits: List<String> = emptyList(),
    val activeCampaign: CampaignStory? = null,
    val campaignXp: Int = 0,
    val isLoadingAI: Boolean = false,
    val showPathIntro: Boolean = true,
    val dailyProtein: Int = 0,
    val dailyCarbs: Int = 0
)

class BecomingViewModel : ViewModel() {
    private val geminiService = GeminiService()

    private val _charState = MutableStateFlow(CharacterState())
    val charState = _charState.asStateFlow()

    private val _generatedStories = MutableStateFlow<List<CampaignStory>>(emptyList())
    val generatedStories = _generatedStories.asStateFlow()

    private val _activeQuests = MutableStateFlow<List<Quest>>(emptyList())
    val activeQuests = _activeQuests.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing = _isProcessing.asStateFlow()

    private val _alchemistResult = MutableStateFlow<AlchemistResult?>(null)
    val alchemistResult = _alchemistResult.asStateFlow()

    // 20 Bespoke Flagship Quests (Modernized Details)
    private val allFlagshipQuests = listOf(
        Quest("k1", "Knight", "Run the Wild", "GPS Tracking enabled. Maintain a steady pace.", 250, "gps"),
        Quest("k2", "Knight", "The Iron Forge", "Log your heavy lifts. Track progressive overload.", 300, "tap", requiresProof = true),
        Quest("k3", "Knight", "Alchemist's Lens", "Scan provisions. Log your macronutrients.", 150, "camera"),
        Quest("k4", "Knight", "Trial by Combat", "HIIT timer. Push your cardiovascular limits.", 350, "timer"),
        Quest("k5", "Knight", "The Long Rest", "Sleep cycle tracking. Recover your physical form.", 100, "rest"),

        Quest("m1", "Mage", "Arcane Focus", "Deep work timer. Do not break the runic circle.", 300, "timer"),
        Quest("m2", "Mage", "Study Grimoires", "Reading session. Log pages read and insights gained.", 250, "rest"),
        Quest("m3", "Mage", "Mind Palace", "Skill review. Practice flashcards or coding exercises.", 150, "tap"),
        Quest("m4", "Mage", "Scribe's Scroll", "Journaling. Empty your thoughts onto parchment.", 200, "rest"),
        Quest("m5", "Mage", "The Long Rest", "Digital detox. Step away from the glowing screens.", 100, "rest"),

        Quest("a1", "Artisan", "Master's Flow", "Creative timer. Work on your primary craft.", 300, "timer"),
        Quest("a2", "Artisan", "Daily Stroke", "Micro-habit logging. Ensure daily creative touch.", 150, "tap"),
        Quest("a3", "Artisan", "Observe the Realm", "Capture visual inspiration for your mood board.", 200, "camera"),
        Quest("a4", "Artisan", "Scribe's Scroll", "Outline your next big project or design.", 250, "rest"),
        Quest("a5", "Artisan", "Campfire Reflection", "Log one creation you are proud of today.", 100, "rest"),

        Quest("r1", "Ranger", "Forage & Scout", "Pedometer tracking. Map new neighborhood routes.", 250, "gps"),
        Quest("r2", "Ranger", "Meditative Trance", "Breathwork timer. Lower your resting heart rate.", 200, "timer"),
        Quest("r3", "Ranger", "Run the Wild", "Trail running. Push your endurance boundaries.", 300, "gps"),
        Quest("r4", "Ranger", "Campfire Reflection", "Nature journaling. Log observations from the wild.", 150, "rest"),
        Quest("r5", "Ranger", "Alchemist's Lens", "Log healthy, whole-food provisions.", 100, "camera")
    )

    private val json = Json { ignoreUnknownKeys = true }

    fun initializeUserAndGenerateStories(name: String, dob: String, traits: List<String>, goal: String, timeline: Int, path: String) {
        _charState.update { it.copy(name = name, dob = dob, traits = traits, heroClass = path, isLoadingAI = true) }
        
        viewModelScope.launch {
            try {
                val prompt = """
                    Based on a user's life goal: "$goal" and their RPG class: "$path", 
                    generate exactly 3 unique, high-fantasy "Grand Campaigns" (long-term questlines).
                    Each campaign should have:
                    - id: a unique string
                    - title: a legendary sounding title
                    - loreSummary: 2-3 sentences explaining the epic stakes
                    - totalRequiredXp: a number between 20000 and 50000
                    
                    Return ONLY a raw JSON object with a "stories" key containing an array of these 3 campaigns.
                """.trimIndent()

                val result = geminiService.generateRawText(prompt)
                val response = json.decodeFromString<StoryGenerationResponse>(result ?: "{\"stories\":[]}")
                _generatedStories.value = response.stories
            } catch (e: Exception) {
                // Fallback stories
                _generatedStories.value = listOf(
                    CampaignStory("f1", "The Iron Gauntlet", "Master your physical vessel and crush all weakness.", 30000),
                    CampaignStory("f2", "The Archmage's Ascent", "Unlock the secrets of the universe through study.", 25000),
                    CampaignStory("f3", "The Ranger's Path", "Protect the wild from the shadows within.", 35000)
                )
            } finally {
                _charState.update { it.copy(isLoadingAI = false) }
            }
        }
    }

    fun sealCampaign(story: CampaignStory) {
        _charState.update { it.copy(activeCampaign = story, campaignXp = 0) }
        _activeQuests.value = allFlagshipQuests.filter { it.path == _charState.value.heroClass }
    }

    fun customizeStory(id: String, title: String, lore: String) {
        _generatedStories.update { list ->
            list.map { if (it.id == id) it.copy(title = title, loreSummary = lore) else it }
        }
    }

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

    // AI Features (Food/Proof)
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
}
