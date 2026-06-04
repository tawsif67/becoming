package com.example.becoming

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.ai.client.generativeai.GenerativeModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

// 1. Data Models (Quest must be Serializable for AI JSON parsing)
@Serializable
data class Quest(val id: String, val title: String, val desc: String, val xp: Int, val iconType: String)

data class CharacterState(
    val name: String = "",
    val heroClass: String = "Unknown",
    val level: Int = 1,
    val currentXp: Int = 0,
    val traits: Map<String, Float> = mapOf("Vitality" to 0.1f, "Focus" to 0.1f, "Discipline" to 0.1f, "Endurance" to 0.1f),
    val campaignTitle: String = "",
    val campaignXp: Int = 0,
    val totalCampaignXp: Int = 30000,
    val perceyMessage: String = "The stars are aligning...",
    val isLoading: Boolean = false
)

class BecomingViewModel : ViewModel() {
    private val _charState = MutableStateFlow(CharacterState())
    val charState = _charState.asStateFlow()

    private val _activeQuests = MutableStateFlow<List<Quest>>(emptyList())
    val activeQuests = _activeQuests.asStateFlow()

    // 2. Initialize the real Gemini AI Model securely
    private val generativeModel = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY // Pulls securely from local.properties
    )

    // 3. The Core AI Engine
    fun initializeUserAndGenerateQuests(name: String, path: String, realLifeGoal: String) {
        // Set loading state and initial data
        _charState.update {
            it.copy(
                name = name,
                heroClass = path,
                campaignTitle = realLifeGoal,
                isLoading = true,
                perceyMessage = "Consulting the ancient grimoires to chart your destiny..."
            )
        }

        viewModelScope.launch {
            try {
                // Task A: Generate Percey's Lore Greeting
                val lorePrompt = "You are Percey, an ancient magical ledger keeper in a medieval RPG. Greet a new hero named $name who has chosen the path of the $path to achieve this real-life goal: '$realLifeGoal'. Keep it strictly to 2 short, epic, immersive sentences. Speak directly to the hero."
                val loreResponse = generativeModel.generateContent(lorePrompt)

                _charState.update { it.copy(perceyMessage = loreResponse.text?.trim() ?: "Welcome to the Realm, Hero.") }

                // Task B: Generate Dynamic Quests formatted as JSON
                val questPrompt = """
                    You are the engine of a real-life habit tracker disguised as an RPG. 
                    The user's real-life goal is: '$realLifeGoal'. They chose the class: '$path'.
                    Generate exactly 3 specific, highly actionable real-world tasks they should do TODAY to move closer to this goal. 
                    Return ONLY a raw JSON array of objects. Do not include markdown code blocks (like ```json).
                    Each object must strictly have these exact keys:
                    "id" (string: "1", "2", "3"),
                    "title" (string: Epic medieval name for the task),
                    "desc" (string: The actual real-world action they must take today),
                    "xp" (integer: between 150 and 500 based on difficulty),
                    "iconType" (string: either "strength", "focus", or "agility")
                """.trimIndent()

                val questResponse = generativeModel.generateContent(questPrompt)

                // Clean the AI output and parse it
                val rawJson = questResponse.text?.replace("