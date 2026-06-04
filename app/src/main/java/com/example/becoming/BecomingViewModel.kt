package com.example.becoming

import android.content.Context
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

// --- CORE MODELS ---
enum class QuestType { TIMER, REPS, CHECK, INPUT, GPS }

@Serializable
data class TraitOption(val name: String, val kPts: Int, val mPts: Int, val aPts: Int, val rPts: Int)

@Serializable
data class QuizOption(val text: String, val knightPts: Int, val magePts: Int, val artisanPts: Int, val rangerPts: Int, val timelineMoons: Int = 0)

@Serializable
data class QuizQuestion(val text: String, val options: List<QuizOption>)

@Serializable
data class CampaignStory(val id: String, var title: String, var loreSummary: String, val totalRequiredXp: Int)

@Serializable
data class Quest(
    val id: String, 
    val path: String, 
    val baseTask: String, 
    val xp: Int, 
    val iconType: String,
    val type: QuestType,
    val targetValue: Int, 
    val unit: String = "",
    val timeScale: String = "DAILY"
)

@Serializable
data class QuestFlavor(
    val title: String,
    val narrativeDesc: String,
    val aftermathLore: String
)

@Serializable
data class CharacterState(
    val name: String = "",
    val heroClass: String = "Unknown",
    val level: Int = 1,
    val currentXp: Int = 0,
    val totalCampaignXp: Int = 0,
    val activeCampaign: CampaignStory? = null,
    val traits: List<String> = emptyList(),
    val showPathIntro: Boolean = true,
    val streakCount: Int = 0,
    val lastLoginMillis: Long = 0,
    val onboardingComplete: Boolean = false
)

// --- LEXICON MODELS ---
data class ClassLexicon(
    val title: String,
    val dailyHooks: List<String>,
    val weeklyHooks: List<String>,
    val monthlyHooks: List<String>,
    val actions: List<String>,
    val enemies: List<String>,
    val victories: List<String>
)

class BecomingViewModel : ViewModel() {
    private val _charState = MutableStateFlow(CharacterState())
    val charState = _charState.asStateFlow()

    private val _suggestedClass = MutableStateFlow("Unknown")
    val suggestedClass = _suggestedClass.asStateFlow()

    private val _chosenTimelineMoons = MutableStateFlow(3)
    val chosenTimelineMoons = _chosenTimelineMoons.asStateFlow()

    private val _generatedStories = MutableStateFlow<List<CampaignStory>>(emptyList())
    val generatedStories = _generatedStories.asStateFlow()

    private val _activeQuests = MutableStateFlow<List<Quest>>(emptyList())
    val activeQuests = _activeQuests.asStateFlow()

    private val jsonParser = Json { ignoreUnknownKeys = true; isLenient = true }

    // --- LEXICONS ---
    private val mageLexicon = ClassLexicon(
        title = "Mage",
        dailyHooks = listOf("The tower is perfectly quiet.", "The latent features of the realm are shifting.", "A faint trace of corrupted logic echoes in the halls."),
        weeklyHooks = listOf("A hidden backdoor in the network has been triggered!", "The minor council demands your findings.", "The architecture of the spell is destabilizing!"),
        monthlyHooks = listOf("The Grand Tribunal has convened.", "The Abyssal Network has launched an assault on reality.", "The fabric of the realm requires intervention."),
        actions = listOf("unlearn the corruption", "apply a general regularizer effect", "isolate the complex variables", "decipher the mechanistic runes"),
        enemies = listOf("corrupted algorithms", "noise in the system", "the shadow network"),
        victories = listOf("The logic is pure once more.", "You have mapped the unknown.", "Your intellect acts as a shield.")
    )

    private val knightLexicon = ClassLexicon(
        title = "Knight",
        dailyHooks = listOf("The perimeter is quiet, but the air is cold.", "The armory smells of oil and iron.", "The dawn breaks, demanding your sweat."),
        weeklyHooks = listOf("A scouting party has breached the valley!", "The Vanguard is moving out.", "A storm approaches; the walls must be tested."),
        monthlyHooks = listOf("The Obsidian Beast has finally descended.", "The Great Siege begins at dawn.", "The warlord has challenged you to combat."),
        actions = listOf("strike with relentless force", "fortify the line", "outlast the onslaught", "march until your boots wear thin"),
        enemies = listOf("the encroaching horde", "the beast's fire", "your own breaking point"),
        victories = listOf("The line held because of you.", "You outran the shadows.", "Your physical mastery is undeniable.")
    )

    private val artisanLexicon = ClassLexicon(
        title = "Artisan",
        dailyHooks = listOf("The workshop is silent, waiting for your touch.", "Dust motes dance in the morning light.", "Your tools are laid out in perfect order."),
        weeklyHooks = listOf("A patron has requested a masterwork.", "The market demands innovation.", "Your vision is beginning to take shape."),
        monthlyHooks = listOf("The Exhibition of Ages is approaching.", "Your masterpiece is halfway finished.", "The creative well is deep and full."),
        actions = listOf("carve your vision into reality", "forge beauty from raw chaos", "stitch the blueprint together", "polish every detail until it shines"),
        enemies = listOf("the demon of resistance", "the fog of procrastination", "the internal critic"),
        victories = listOf("You brought beauty into the world.", "The craft has been elevated.", "Your work speaks for itself.")
    )

    private val rangerLexicon = ClassLexicon(
        title = "Ranger",
        dailyHooks = listOf("The forest floor is damp and fragrant.", "The wind whispers secrets through the trees.", "Your spirit is grounded in the earth."),
        weeklyHooks = listOf("The boundaries of the wild are shifting.", "A new trail has revealed itself.", "The shadows in the woods are deepening."),
        monthlyHooks = listOf("The Great Migration has begun.", "You must guide the lost back to the light.", "The mountain peak calls to you."),
        actions = listOf("track the subtle signs", "center your soul in the silence", "breathe with the rhythm of nature", "navigate the uncharted terrain"),
        enemies = listOf("the city's toxic hum", "the weight of urban stress", "the loss of focus"),
        victories = listOf("You found peace in the chaos.", "The wild has accepted you.", "Your spirit is free and clear.")
    )

    private val lexicons = mapOf("Mage" to mageLexicon, "Knight" to knightLexicon, "Artisan" to artisanLexicon, "Ranger" to rangerLexicon)

    // --- 10 PREMIUM ATTRIBUTES ---
    val availableTraits = listOf(
        TraitOption("Physical Fitness", kPts = 3, mPts = 0, aPts = 0, rPts = 1),
        TraitOption("Mental Focus", kPts = 0, mPts = 3, aPts = 1, rPts = 0),
        TraitOption("Financial Wealth", kPts = 0, mPts = 2, aPts = 2, rPts = 0),
        TraitOption("Creative Output", kPts = 0, mPts = 0, aPts = 3, rPts = 0),
        TraitOption("Social Charisma", kPts = 1, mPts = 1, aPts = 1, rPts = 0),
        TraitOption("Emotional Resilience", kPts = 1, mPts = 0, aPts = 0, rPts = 3),
        TraitOption("Deep Knowledge", kPts = 0, mPts = 3, aPts = 0, rPts = 0),
        TraitOption("Career Growth", kPts = 0, mPts = 2, aPts = 1, rPts = 0),
        TraitOption("Mindfulness", kPts = 0, mPts = 0, aPts = 0, rPts = 3),
        TraitOption("Physical Endurance", kPts = 2, mPts = 0, aPts = 0, rPts = 2)
    )

    // --- 20 PREMIUM FLAGSHIP QUESTS ---
    private val allFlagshipQuests = listOf(
        // KNIGHT
        Quest("k1", "Knight", "Endurance Run", 250, "gps", QuestType.TIMER, 30, "min"),
        Quest("k2", "Knight", "Strength Training", 300, "tap", QuestType.REPS, 10, "sets"),
        Quest("k3", "Knight", "Macro Scanner", 150, "camera", QuestType.CHECK, 0),
        Quest("k4", "Knight", "HIIT Cardio", 350, "timer", QuestType.TIMER, 20, "min"),
        Quest("k5", "Knight", "Deep Sleep", 100, "rest", QuestType.CHECK, 8, "hours"),
        // MAGE
        Quest("m1", "Mage", "Deep Work Session", 300, "timer", QuestType.TIMER, 45, "min"),
        Quest("m2", "Mage", "Active Reading", 250, "rest", QuestType.INPUT, 30, "pages"),
        Quest("m3", "Mage", "Skill Practice", 150, "tap", QuestType.REPS, 10, "reps"),
        Quest("m4", "Mage", "Daily Journal", 200, "rest", QuestType.CHECK, 0),
        Quest("m5", "Mage", "Digital Detox", 100, "rest", QuestType.TIMER, 60, "min"),
        // ARTISAN
        Quest("a1", "Artisan", "Creative Flow", 300, "timer", QuestType.TIMER, 60, "min"),
        Quest("a2", "Artisan", "Micro-Habit", 150, "tap", QuestType.TIMER, 10, "min"),
        Quest("a3", "Artisan", "Inspiration Capture", 200, "camera", QuestType.CHECK, 3, "notes"),
        Quest("a4", "Artisan", "Project Blueprint", 250, "rest", QuestType.CHECK, 0),
        Quest("a5", "Artisan", "Gratitude Log", 100, "rest", QuestType.CHECK, 3, "items"),
        // RANGER
        Quest("r1", "Ranger", "10k Steps", 250, "gps", QuestType.GPS, 10000, "steps"),
        Quest("r2", "Ranger", "Guided Breathwork", 200, "timer", QuestType.TIMER, 10, "min"),
        Quest("r3", "Ranger", "Outdoor Immersion", 300, "gps", QuestType.TIMER, 30, "min"),
        Quest("r4", "Ranger", "Evening Reflection", 150, "rest", QuestType.CHECK, 0),
        Quest("r5", "Ranger", "Clean Fuel Scanner", 100, "camera", QuestType.CHECK, 0)
    )

    val quizQuestions = listOf(
        QuizQuestion("What is your primary goal for this journey?", listOf(
            QuizOption("Build physical strength and unmatched fitness.", 3, 0, 0, 0),
            QuizOption("Expand my knowledge and accelerate my career.", 0, 3, 0, 0),
            QuizOption("Create a masterpiece or build a business.", 0, 0, 3, 0),
            QuizOption("Improve mental health and explore the world.", 0, 0, 0, 3),
            QuizOption("I want a balance of everything.", 1, 1, 1, 1)
        )),
        QuizQuestion("How do you prefer to tackle a difficult challenge?", listOf(
            QuizOption("Head-on with raw force and discipline.", 3, 0, 0, 0),
            QuizOption("Analyze the problem and research a solution.", 0, 3, 0, 0),
            QuizOption("Design a creative workaround.", 0, 0, 3, 0),
            QuizOption("Adapt to the situation and maneuver past it.", 0, 0, 0, 3)
        )),
        QuizQuestion("In your free time, where would you rather be?", listOf(
            QuizOption("In the gym, arena, or on the field.", 3, 0, 0, 0),
            QuizOption("In a quiet library or a cozy study.", 0, 3, 0, 0),
            QuizOption("In a workshop, studio, or at a canvas.", 0, 0, 3, 0),
            QuizOption("Outdoors, hiking, or traveling.", 0, 0, 0, 3)
        )),
        QuizQuestion("What is your greatest weakness?", listOf(
            QuizOption("Impatience and acting without thinking.", 3, 0, 0, 0),
            QuizOption("Overthinking and analysis paralysis.", 0, 3, 0, 0),
            QuizOption("Perfectionism and never finishing things.", 0, 0, 3, 0),
            QuizOption("Restlessness and lack of routine.", 0, 0, 0, 3)
        )),
        QuizQuestion("Choose your ideal weapon.", listOf(
            QuizOption("A heavy Greatsword.", 3, 0, 0, 0),
            QuizOption("An ancient Spellbook.", 0, 3, 0, 0),
            QuizOption("A Chisel and Hammer.", 0, 0, 3, 0),
            QuizOption("A Longbow and Compass.", 0, 0, 0, 3)
        )),
        QuizQuestion("How do you handle failure?", listOf(
            QuizOption("Get angry and try again harder.", 3, 0, 0, 0),
            QuizOption("Study what went wrong to never repeat it.", 0, 3, 0, 0),
            QuizOption("Use the failure as inspiration for the next attempt.", 0, 0, 3, 0),
            QuizOption("Brush it off and walk a different path.", 0, 0, 0, 3)
        )),
        QuizQuestion("What legacy do you want to leave behind?", listOf(
            QuizOption("To be remembered as the strongest and most reliable.", 3, 0, 0, 0),
            QuizOption("To discover a truth that changes the world.", 0, 3, 0, 0),
            QuizOption("To leave behind a beautiful creation that outlasts me.", 0, 0, 3, 0),
            QuizOption("To map the uncharted and live truly free.", 0, 0, 0, 3)
        )),
        QuizQuestion("Pick an element.", listOf(
            QuizOption("Fire (Power & Destruction)", 3, 0, 0, 0),
            QuizOption("Ice (Focus & Control)", 0, 3, 0, 0),
            QuizOption("Lightning (Energy & Spark)", 0, 0, 3, 0),
            QuizOption("Earth (Growth & Stability)", 0, 0, 0, 3)
        )),
        QuizQuestion("How structured is your ideal day?", listOf(
            QuizOption("Strict military routine. No deviations.", 3, 1, 0, 0),
            QuizOption("Scheduled blocks of deep work and rest.", 1, 3, 0, 1),
            QuizOption("Bursts of intense inspiration, then rest.", 0, 0, 3, 0),
            QuizOption("Completely fluid. I go where the wind takes me.", 0, 0, 1, 3)
        )),
        QuizQuestion("Finally, how many moons will you commit to this Grand Campaign?", listOf(
            QuizOption("3 Moons (A rapid, intense sprint)", 0, 0, 0, 0, 3),
            QuizOption("6 Moons (A dedicated, steady journey)", 0, 0, 0, 0, 6),
            QuizOption("9 Moons (A profound transformation)", 0, 0, 0, 0, 9),
            QuizOption("12 Moons (A complete rebirth)", 0, 0, 0, 0, 12)
        ))
    )

    fun calculateSuggestion(selectedTraits: List<TraitOption>, answers: List<QuizOption>) {
        var k = 0; var m = 0; var a = 0; var r = 0; var timeline = 3
        selectedTraits.forEach { k += it.kPts; m += it.mPts; a += it.aPts; r += it.rPts }
        answers.forEach {
            k += it.knightPts; m += it.magePts; a += it.artisanPts; r += it.rangerPts
            if (it.timelineMoons > 0) timeline = it.timelineMoons
        }
        _chosenTimelineMoons.value = timeline
        _suggestedClass.value = when {
            k >= m && k >= a && k >= r -> "Knight"
            m >= k && m >= a && m >= r -> "Mage"
            a >= k && a >= m && a >= r -> "Artisan"
            else -> "Ranger"
        }
    }

    fun selectClassAndGenerateStories(name: String, path: String, traits: List<String>, context: Context) {
        val totalXp = _chosenTimelineMoons.value * 10000
        val stories = when (path) {
            "Knight" -> listOf(
                CampaignStory("k1", "The Iron Vanguard", "Transform your body into an unbreakable fortress. Forge discipline through sweat, heavy iron, and relentless physical conditioning.", totalXp),
                CampaignStory("k2", "The Dragon's Ascent", "Push your cardiovascular limits. Build the superhuman endurance required to conquer marathons, outrun exhaustion, and slay the beasts of lethargy.", totalXp),
                CampaignStory("k3", "Shield of the Realm", "Achieve peak physical mastery to become the ultimate protector of your domain and family. Health is the highest form of defense.", totalXp)
            )
            "Mage" -> listOf(
                CampaignStory("m1", "The Archmage's Trial", "Absorb the knowledge of the waking world. Master your career or degree through intense study, complex problem solving, and unbroken mental focus.", totalXp),
                CampaignStory("m2", "The Scholar's Path", "Banish modern distractions. Build an impenetrable mind palace capable of passing any certification or mastering any new language.", totalXp),
                CampaignStory("m3", "The Chronomancer", "Master the flow of time. Optimize your productivity systems and daily routines to achieve financial wealth and deep, lasting wisdom.", totalXp)
            )
            "Artisan" -> listOf(
                CampaignStory("a1", "The Master's Opus", "Dedicate yourself fully to your primary craft. Turn daily micro-habits into a legendary, finished creation (a novel, a portfolio, an album).", totalXp),
                CampaignStory("a2", "The Golden Forge", "Build your financial and creative independence. Launch your startup, side-hustle, or project stroke by stroke, day by day.", totalXp),
                CampaignStory("a3", "The Architect's Dream", "Design the life you want to live. Sketch the blueprint of your ideal lifestyle and execute the milestones with beautiful precision.", totalXp)
            )
            else -> listOf(
                CampaignStory("r1", "The Pathfinder's Journey", "Explore the wild world. Build a daily habit of movement, hiking, and deep physical presence in nature.", totalXp),
                CampaignStory("r2", "The Wild Hunt", "Track down your anxieties. Use breathwork, meditation, and mindfulness to build an unbreakable, calm spirit.", totalXp),
                CampaignStory("r3", "The Apex Explorer", "Step completely out of your comfort zone. Log miles and quiet moments to discover your true self in the unknown.", totalXp)
            )
        }
        _charState.update { it.copy(name = name, heroClass = path, traits = traits, onboardingComplete = true) }
        _generatedStories.value = stories
        _activeQuests.value = allFlagshipQuests.filter { it.path == path }
        saveData(context)
    }

    fun sealCampaign(story: CampaignStory, context: Context) {
        _charState.update { it.copy(activeCampaign = story) }
        saveData(context)
    }

    fun addCustomQuest(baseTask: String, timeScale: String) {
        val baseXp = 150
        val calculatedXp = when(timeScale) {
            "WEEKLY" -> baseXp * 5
            "MONTHLY" -> baseXp * 20
            else -> baseXp
        }
        val newQuest = Quest(
            id = "custom_${System.currentTimeMillis()}",
            path = _charState.value.heroClass,
            baseTask = baseTask,
            xp = calculatedXp,
            iconType = "scroll",
            type = QuestType.CHECK,
            targetValue = 0,
            timeScale = timeScale
        )
        _activeQuests.update { it + newQuest }
    }

    fun generateQuestNarrative(heroClass: String, realWorldTask: String, timeScale: String): QuestFlavor {
        val lexicon = lexicons[heroClass] ?: knightLexicon
        val hook = when(timeScale) {
            "WEEKLY" -> lexicon.weeklyHooks.random()
            "MONTHLY" -> lexicon.monthlyHooks.random()
            else -> lexicon.dailyHooks.random()
        }
        val action = lexicon.actions.random()
        val enemy = lexicon.enemies.random()
        val victory = lexicon.victories.random()

        val briefing = when(timeScale) {
            "DAILY" -> "$hook To prepare for what is to come, you must $action. Today's discipline: $realWorldTask. Do not let $enemy catch you off guard."
            "WEEKLY" -> "$hook This is no longer practice. Use the strength you've gathered this week to $action! Focus on this trial: $realWorldTask."
            "MONTHLY" -> "$hook This is the climax of your campaign. Everything you have done has led to this. Complete your ultimate objective: $realWorldTask. Defeat $enemy!"
            else -> "Complete the task: $realWorldTask."
        }
        val aftermath = when(timeScale) {
            "DAILY" -> "You completed: $realWorldTask. $victory The daily grind hardens you."
            "WEEKLY" -> "You conquered the weekly trial: $realWorldTask. You pushed back $enemy and proved your growth."
            "MONTHLY" -> "You survived the monthly milestone: $realWorldTask! $victory The realm will sing songs of your triumph!"
            else -> "Task sealed."
        }
        return QuestFlavor(
            title = if (timeScale == "MONTHLY") "THE GRAND TRIAL" else if (timeScale == "WEEKLY") "THE ESCALATION" else "Daily Discipline",
            narrativeDesc = briefing,
            aftermathLore = aftermath
        )
    }

    fun grantBounty(xp: Int, context: Context): Boolean {
        var leveledUp = false
        _charState.update { state ->
            var newXp = state.currentXp + xp
            var newLevel = state.level
            val xpNeeded = state.level * 1000
            if (newXp >= xpNeeded) { newXp -= xpNeeded; newLevel++; leveledUp = true }
            state.copy(level = newLevel, currentXp = newXp, totalCampaignXp = state.totalCampaignXp + xp)
        }
        saveData(context)
        return leveledUp
    }

    fun grantPartialBounty(quest: Quest, completedValue: Int, context: Context): Boolean {
        val fraction = if (quest.targetValue > 0) completedValue.toFloat() / quest.targetValue.toFloat() else 1f
        val xpToGrant = (quest.xp * fraction).roundToInt()
        return grantBounty(xpToGrant, context)
    }

    fun saveData(context: Context) {
        val prefs = context.getSharedPreferences("BecomingPrefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putString("state", jsonParser.encodeToString(_charState.value))
            apply()
        }
    }

    fun loadData(context: Context): Boolean {
        val prefs = context.getSharedPreferences("BecomingPrefs", Context.MODE_PRIVATE)
        val stateJson = prefs.getString("state", null)
        if (stateJson != null) {
            try {
                var loadedState = jsonParser.decodeFromString<CharacterState>(stateJson)
                val now = System.currentTimeMillis()
                val oneDayMillis = 24 * 60 * 60 * 1000L
                val diff = now - loadedState.lastLoginMillis
                loadedState = when {
                    loadedState.lastLoginMillis == 0L -> loadedState.copy(lastLoginMillis = now, streakCount = 1)
                    diff > oneDayMillis * 2 -> loadedState.copy(lastLoginMillis = now, streakCount = 0)
                    diff > oneDayMillis -> loadedState.copy(lastLoginMillis = now, streakCount = loadedState.streakCount + 1)
                    else -> loadedState
                }
                _charState.value = loadedState
                saveData(context)
                _activeQuests.value = allFlagshipQuests.filter { it.path == loadedState.heroClass }
                return loadedState.onboardingComplete
            } catch (e: Exception) { return false }
        }
        return false
    }
}
