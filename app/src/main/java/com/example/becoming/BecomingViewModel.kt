package com.example.becoming

import android.content.Context
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.becoming.data.*
import com.example.becoming.domain.*
import com.example.becoming.util.Prefs
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

// --- UI MODELS ---
data class AttributeOption(
    val name: String,
    val description: String,
    val icon: ImageVector,
    val kPts: Int,
    val mPts: Int,
    val aPts: Int,
    val rPts: Int
)

data class DisciplineOption(
    val name: String,
    val attributeCategory: String
)

data class QuizOption(val text: String, val knightPts: Int, val magePts: Int, val artisanPts: Int, val rangerPts: Int, val timelineMoons: Int = 0)
data class QuizQuestion(val text: String, val options: List<QuizOption>)

data class CharacterState(
    val heroName: String = "",
    val heroClass: String = "Unknown",
    val globalLevel: Int = 1,
    val currentXp: Int = 0,
    val totalCampaignXp: Int = 0,
    val campfireStreak: Int = 0,
    // Attributes Values
    val attrPhysicalFitness: Float = 0f,
    val attrMentalFocus: Float = 0f,
    val attrFinancialWealth: Float = 0f,
    val attrCreativeOutput: Float = 0f,
    val attrSocialCharisma: Float = 0f,
    val attrEmotionalResilience: Float = 0f,
    val attrDeepKnowledge: Float = 0f,
    val attrCareerGrowth: Float = 0f,
    val attrMindfulness: Float = 0f,
    val attrPhysicalEndurance: Float = 0f,
    val selectedAttributeNames: List<String> = emptyList(),
    val onboardingComplete: Boolean = false,
    val activeCampaign: CampaignStory? = null,
    val isSoundEnabled: Boolean = true
)

class BecomingViewModel : ViewModel() {
    private var database: BecomingDatabase? = null

    private val _charState = MutableStateFlow(CharacterState())
    val charState = _charState.asStateFlow()

    private val _suggestedClass = MutableStateFlow("Unknown")
    val suggestedClass = _suggestedClass.asStateFlow()

    private val _chosenTimelineMoons = MutableStateFlow(3)
    val chosenTimelineMoons = _chosenTimelineMoons.asStateFlow()

    private val _generatedStories = MutableStateFlow<List<CampaignStory>>(emptyList())
    val generatedStories = _generatedStories.asStateFlow()

    private val _activeQuests = MutableStateFlow<List<QuestEntity>>(emptyList())
    val activeQuests = _activeQuests.asStateFlow()

    // --- 10 PREMIUM ATTRIBUTES ---
    val premiumAttributes = listOf(
        AttributeOption("Physical Fitness", "Forge your body into an unbreakable vessel of power, strength, and vitality.", Icons.Default.FitnessCenter, 3, 0, 0, 1),
        AttributeOption("Mental Focus", "Sharpen your mind to pierce through distractions and sustain unbroken concentration.", Icons.Default.Psychology, 0, 3, 1, 0),
        AttributeOption("Financial Wealth", "Master the flow of resources to build an empire of stability and ultimate freedom.", Icons.Default.AccountBalanceWallet, 0, 2, 2, 0),
        AttributeOption("Creative Output", "Channel your inner spark into tangible artifacts, transforming thoughts into masterpieces.", Icons.Default.Brush, 0, 0, 3, 0),
        AttributeOption("Social Charisma", "Cultivate a magnetic presence to lead, connect deeply, and inspire those around you.", Icons.Default.Groups, 1, 1, 1, 0),
        AttributeOption("Emotional Resilience", "Build an inner fortress that remains unshaken by the chaotic storms of life.", Icons.Default.Shield, 1, 0, 0, 3),
        AttributeOption("Deep Knowledge", "Expand the archives of your mind through relentless curiosity and rigorous study.", Icons.Default.MenuBook, 0, 3, 0, 0),
        AttributeOption("Career Growth", "Command your professional domain, level up your skills, and ascend the ranks.", Icons.Default.TrendingUp, 0, 2, 1, 0),
        AttributeOption("Mindfulness", "Anchor your spirit in the present moment, dissolving the noise of the waking world.", Icons.Default.SelfImprovement, 0, 0, 0, 3),
        AttributeOption("Physical Endurance", "Expand the limits of your heart and lungs to outlast any challenge placed before you.", Icons.Default.DirectionsRun, 2, 0, 0, 2)
    )

    // --- 100 PREMIUM DISCIPLINES ---
    val disciplineRegistry = listOf(
        DisciplineOption("Weightlifting / Powerlifting", "Physical Fitness"),
        DisciplineOption("Calisthenics (Bodyweight training)", "Physical Fitness"),
        DisciplineOption("Bouldering / Rock Climbing", "Physical Fitness"),
        DisciplineOption("High-Intensity Interval Training (HIIT)", "Physical Fitness"),
        DisciplineOption("Power Yoga / Core training", "Physical Fitness"),
        DisciplineOption("Martial Arts (Striking)", "Physical Fitness"),
        DisciplineOption("Brazilian Jiu-Jitsu / Grappling", "Physical Fitness"),
        DisciplineOption("Gymnastics / Mobility work", "Physical Fitness"),
        DisciplineOption("Pilates", "Physical Fitness"),
        DisciplineOption("Team Sports (Soccer, Basketball, etc.)", "Physical Fitness"),
        
        DisciplineOption("Pomodoro Work Sessions", "Mental Focus"),
        DisciplineOption("Digital Detox (Zero-screen time blocks)", "Mental Focus"),
        DisciplineOption("Chess / Strategy Games", "Mental Focus"),
        DisciplineOption("Deep Work Blocks (2+ hours uninterrupted)", "Mental Focus"),
        DisciplineOption("Dual N-Back / Cognitive Brain Training", "Mental Focus"),
        DisciplineOption("Single-tasking practice", "Mental Focus"),
        DisciplineOption("Learning complex patterns / choreography", "Mental Focus"),
        DisciplineOption("Solving complex logic puzzles (Sudoku, etc.)", "Mental Focus"),
        DisciplineOption("Speed reading practice", "Mental Focus"),
        DisciplineOption("Fasting for mental clarity", "Mental Focus"),

        DisciplineOption("Daily/Weekly Budget Tracking", "Financial Wealth"),
        DisciplineOption("Stock Market / Index Fund Investing", "Financial Wealth"),
        DisciplineOption("Building a Side Hustle / Freelancing", "Financial Wealth"),
        DisciplineOption("Reading Financial / Economic Literature", "Financial Wealth"),
        DisciplineOption("Auditing and cutting unnecessary expenses", "Financial Wealth"),
        DisciplineOption("Building an Emergency Fund", "Financial Wealth"),
        DisciplineOption("Real Estate / Market Research", "Financial Wealth"),
        DisciplineOption("Crypto / Web3 Education", "Financial Wealth"),
        DisciplineOption("Negotiation practice for salary/rates", "Financial Wealth"),
        DisciplineOption("Selling unused physical assets", "Financial Wealth"),

        DisciplineOption("Creative Writing / Fiction Journaling", "Creative Output"),
        DisciplineOption("Drawing / Sketching / Painting", "Creative Output"),
        DisciplineOption("Digital Design (UI/UX, Graphic Design)", "Creative Output"),
        DisciplineOption("Playing a Musical Instrument", "Creative Output"),
        DisciplineOption("Music Production / Beatmaking", "Creative Output"),
        DisciplineOption("Coding a personal/passion project", "Creative Output"),
        DisciplineOption("Photography", "Creative Output"),
        DisciplineOption("Videography / Video Editing", "Creative Output"),
        DisciplineOption("Crafting / Woodworking / DIY", "Creative Output"),
        DisciplineOption("3D Modeling / Animation", "Creative Output"),

        DisciplineOption("Public Speaking (e.g., Toastmasters)", "Social Charisma"),
        DisciplineOption("Attending Networking Events", "Social Charisma"),
        DisciplineOption("Initiating conversations with strangers", "Social Charisma"),
        DisciplineOption("Hosting dinners or social gatherings", "Social Charisma"),
        DisciplineOption("Active Listening exercises", "Social Charisma"),
        DisciplineOption("Reconnecting with old friends/family", "Social Charisma"),
        DisciplineOption("Mentoring or teaching someone", "Social Charisma"),
        DisciplineOption("Volunteering in the community", "Social Charisma"),
        DisciplineOption("Taking acting or improv classes", "Social Charisma"),
        DisciplineOption("Debate / Persuasion practice", "Social Charisma"),

        DisciplineOption("Stoic Journaling (Reflecting on challenges)", "Emotional Resilience"),
        DisciplineOption("Cold Exposure / Ice Baths", "Emotional Resilience"),
        DisciplineOption("Therapy or Counseling sessions", "Emotional Resilience"),
        DisciplineOption("Daily Gratitude logging", "Emotional Resilience"),
        DisciplineOption("Shadow Work / Deep Self-reflection", "Emotional Resilience"),
        DisciplineOption("Rejection Therapy (Actively seeking minor rejections)", "Emotional Resilience"),
        DisciplineOption("Practicing positive reframing of negative events", "Emotional Resilience"),
        DisciplineOption("Identifying and breaking cognitive distortions", "Emotional Resilience"),
        DisciplineOption("Voluntary Discomfort (e.g., sleeping on the floor)", "Emotional Resilience"),
        DisciplineOption("Anger management / Pause-and-reflect exercises", "Emotional Resilience"),

        DisciplineOption("Reading Non-Fiction Books", "Deep Knowledge"),
        DisciplineOption("Academic Research / Reading Whitepapers", "Deep Knowledge"),
        DisciplineOption("Learning a New Language (Duolingo, tutoring)", "Deep Knowledge"),
        DisciplineOption("Taking Online Courses (Coursera, edX)", "Deep Knowledge"),
        DisciplineOption("Watching Educational Documentaries", "Deep Knowledge"),
        DisciplineOption("Listening to Long-form Educational Podcasts", "Deep Knowledge"),
        DisciplineOption("Flashcard / Spaced Repetition Study (Anki)", "Deep Knowledge"),
        DisciplineOption("Writing synthesis essays or blog posts on learned topics", "Deep Knowledge"),
        DisciplineOption("Attending academic lectures or seminars", "Deep Knowledge"),
        DisciplineOption("Decoding complex systems (e.g., Machine Unlearning architectures)", "Deep Knowledge"),

        DisciplineOption("Updating CV / Professional Portfolio", "Career Growth"),
        DisciplineOption("Applying for new roles, PhDs, or programs", "Career Growth"),
        DisciplineOption("Pitching to new clients / Lead generation", "Career Growth"),
        DisciplineOption("Studying for Professional Certifications", "Career Growth"),
        DisciplineOption("LinkedIn networking and content posting", "Career Growth"),
        DisciplineOption("Seeking active feedback from managers/advisors", "Career Growth"),
        DisciplineOption("Mentoring junior colleagues or students", "Career Growth"),
        DisciplineOption("Learning industry-specific software", "Career Growth"),
        DisciplineOption("Preparing and practicing for interviews", "Career Growth"),
        DisciplineOption("Shadowing a senior leader or professor", "Career Growth"),

        DisciplineOption("Breathwork (Wim Hof, Box Breathing)", "Mindfulness"),
        DisciplineOption("Nature Walks (Without technology)", "Mindfulness"),
        DisciplineOption("Guided Meditation (Headspace, Waking Up)", "Mindfulness"),
        DisciplineOption("Mindful Eating (No screens during meals)", "Mindfulness"),
        DisciplineOption("Restorative / Yin Yoga", "Mindfulness"),
        DisciplineOption("Sound Baths / Binaural Beats sessions", "Mindfulness"),
        DisciplineOption("Stargazing or Cloud watching", "Mindfulness"),
        DisciplineOption("Minimalist decluttering of physical space", "Mindfulness"),
        DisciplineOption("Dream journaling", "Mindfulness"),
        DisciplineOption("Body Scan Meditation", "Mindfulness"),

        DisciplineOption("Running / Jogging", "Physical Endurance"),
        DisciplineOption("Long-distance Cycling", "Physical Endurance"),
        DisciplineOption("Swimming", "Physical Endurance"),
        DisciplineOption("Rucking (Hiking with a weighted pack)", "Physical Endurance"),
        DisciplineOption("Rowing (Machine or water)", "Physical Endurance"),
        DisciplineOption("Hiking / Mountaineering", "Physical Endurance"),
        DisciplineOption("Jump Rope / Skipping", "Physical Endurance"),
        DisciplineOption("Stair climbing", "Physical Endurance"),
        DisciplineOption("Marathon / Triathlon training prep", "Physical Endurance"),
        DisciplineOption("Dance Cardio", "Physical Endurance")
    )

    private val allFlagshipQuests = listOf(
        // KNIGHT
        QuestEntity("k1", "KNIGHT", "Endurance Run", "DAILY", "Physical Endurance", "ACTIVE", 0, "map_1", 30, "min", 250, "gps"),
        QuestEntity("k2", "KNIGHT", "Strength Training", "DAILY", "Physical Fitness", "ACTIVE", 0, "map_1", 10, "sets", 300, "tap"),
        QuestEntity("k3", "KNIGHT", "Macro Scanner", "DAILY", "Mindfulness", "ACTIVE", 0, "map_1", 0, "", 150, "camera"),
        QuestEntity("k4", "KNIGHT", "HIIT Cardio", "WEEKLY", "Physical Endurance", "ACTIVE", 0, "map_1", 20, "min", 350, "timer"),
        QuestEntity("k5", "KNIGHT", "Deep Sleep", "DAILY", "Mindfulness", "ACTIVE", 0, "map_1", 8, "hours", 100, "rest"),
        // MAGE
        QuestEntity("m1", "MAGE", "Deep Work Session", "DAILY", "Mental Focus", "ACTIVE", 0, "map_2", 45, "min", 300, "timer"),
        QuestEntity("m2", "MAGE", "Active Reading", "DAILY", "Deep Knowledge", "ACTIVE", 0, "map_2", 30, "pages", 250, "rest"),
        QuestEntity("m3", "MAGE", "Skill Practice", "DAILY", "Deep Knowledge", "ACTIVE", 0, "map_2", 15, "min", 150, "tap"),
        QuestEntity("m4", "MAGE", "Daily Journal", "WEEKLY", "Emotional Resilience", "ACTIVE", 0, "map_2", 0, "", 200, "rest"),
        QuestEntity("m5", "MAGE", "Digital Detox", "DAILY", "Mental Focus", "ACTIVE", 0, "map_2", 60, "min", 100, "rest")
    )

    val quizQuestions = listOf(
        QuizQuestion("How many winters have you survived?", listOf(
            QuizOption("Less than 20 winters.", 0, 0, 0, 0),
            QuizOption("20 to 30 winters.", 0, 0, 0, 0),
            QuizOption("Over 30 winters.", 0, 0, 0, 0)
        )),
        QuizQuestion("A heavy Greatsword lies before you. Do you...", listOf(
            QuizOption("Wield it with raw force.", 3, 0, 0, 0),
            QuizOption("Study its balance and history.", 0, 2, 0, 1),
            QuizOption("Recast it into a finer blade.", 0, 0, 3, 0)
        )),
        QuizQuestion("The fog of uncertainty descends. How do you navigate?", listOf(
            QuizOption("Trust your instincts and adapt.", 0, 0, 0, 3),
            QuizOption("Map the terrain methodically.", 0, 3, 0, 0),
            QuizOption("Fortify your current position.", 3, 0, 0, 0)
        )),
        QuizQuestion("You find an ancient, broken artifact. You...", listOf(
            QuizOption("Fix it using traditional methods.", 0, 0, 3, 0),
            QuizOption("Decipher the runes powering it.", 0, 3, 0, 0),
            QuizOption("Salvage it for raw materials.", 1, 0, 0, 2)
        )),
        QuizQuestion("A dragon blocks your path. Your move?", listOf(
            QuizOption("Strike with relentless force.", 3, 0, 0, 0),
            QuizOption("Lure it into a clever trap.", 0, 1, 0, 3),
            QuizOption("Cast a spell of redirection.", 0, 3, 0, 0)
        )),
        QuizQuestion("What is your greatest weapon?", listOf(
            QuizOption("An unbreakable will.", 3, 0, 0, 0),
            QuizOption("A sharp and focused mind.", 0, 3, 0, 0),
            QuizOption("The hands of a master creator.", 0, 0, 3, 0),
            QuizOption("A spirit that knows no borders.", 0, 0, 0, 3)
        )),
        QuizQuestion("How do you handle a defeat?", listOf(
            QuizOption("Get back up and try harder.", 3, 0, 0, 0),
            QuizOption("Analyze what went wrong.", 0, 3, 0, 0),
            QuizOption("Find a creative alternative.", 0, 0, 3, 0)
        )),
        QuizQuestion("Are you ready to forge your legacy, regardless of the hardship?", listOf(
            QuizOption("I am ready.", 1, 1, 1, 1),
            QuizOption("By my oath, I shall become.", 2, 2, 2, 2)
        )),
        QuizQuestion("Finally, how many moons will you commit to this Grand Campaign?", listOf(
            QuizOption("3 Moons (A rapid sprint)", 0, 0, 0, 0, 3),
            QuizOption("6 Moons (A steady journey)", 0, 0, 0, 0, 6),
            QuizOption("12 Moons (A complete rebirth)", 0, 0, 0, 0, 12)
        ))
    )

    fun initialize(context: Context) {
        if (database == null) {
            database = BecomingDatabase.getDatabase(context)
            observeCharacter()
            observeQuests()
        }
    }

    private fun observeCharacter() {
        viewModelScope.launch {
            database?.characterDao()?.getCharacterState()?.collect { entity ->
                entity?.let {
                    Log.d("BECOMING", "Character Observed: ${it.heroName}, Streak: ${it.campfireStreak}, Attrs: ${it.selectedAttributeNames}")
                    _charState.value = CharacterState(
                        heroName = it.heroName, heroClass = it.heroClass,
                        globalLevel = it.globalLevel, currentXp = it.currentXp,
                        totalCampaignXp = 0,
                        campfireStreak = it.campfireStreak,
                        attrPhysicalFitness = it.attrPhysicalFitness,
                        attrMentalFocus = it.attrMentalFocus,
                        attrFinancialWealth = it.attrFinancialWealth,
                        attrCreativeOutput = it.attrCreativeOutput,
                        attrSocialCharisma = it.attrSocialCharisma,
                        attrEmotionalResilience = it.attrEmotionalResilience,
                        attrDeepKnowledge = it.attrDeepKnowledge,
                        attrCareerGrowth = it.attrCareerGrowth,
                        attrMindfulness = it.attrMindfulness,
                        attrPhysicalEndurance = it.attrPhysicalEndurance,
                        selectedAttributeNames = it.selectedAttributeNames.split(",").filter { s -> s.isNotEmpty() },
                        onboardingComplete = it.onboardingComplete, isSoundEnabled = it.isSoundEnabled
                    )
                } ?: run {
                    Log.d("BECOMING", "Character Entity is NULL")
                }
            }
        }
    }

    private fun observeQuests() {
        viewModelScope.launch {
            database?.questDao()?.getActiveQuests()?.collect { quests ->
                val now = System.currentTimeMillis()
                // Relax filter to include current minute
                val visibleQuests = quests.filter { it.createdAt <= now + 60000 }.take(20)
                Log.d("BECOMING", "Quests Observed: ${visibleQuests.size} / ${quests.size}")
                _activeQuests.value = visibleQuests
            }
        }
    }

    fun calculateSuggestion(selectedAttributes: List<AttributeOption>, answers: List<QuizOption>) {
        var k = 0; var m = 0; var a = 0; var r = 0; var timeline = 3
        selectedAttributes.forEach { k += it.kPts; m += it.mPts; a += it.aPts; r += it.rPts }
        answers.forEach {
            k += it.knightPts; m += it.magePts; a += it.artisanPts; r += it.rangerPts
            if (it.timelineMoons > 0) timeline = it.timelineMoons
        }
        _chosenTimelineMoons.value = timeline
        _suggestedClass.value = when {
            k >= m && k >= a && k >= r -> "KNIGHT"
            m >= k && m >= a && m >= r -> "MAGE"
            a >= k && a >= m && a >= r -> "ARTISAN"
            else -> "RANGER"
        }
    }

    suspend fun finalizeCharacterAndCampaign(
        name: String,
        heroClass: String,
        selectedAttributes: List<String>,
        baselines: Map<String, Float>,
        goals: Map<String, Float>,
        context: Context
    ) {
        Log.d("BECOMING", "Finalizing Character: $name as $heroClass")
        // Use withContext to ensure DB operations are on IO dispatcher
        kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
            // CLEAR OLD DATA FIRST to ensure clean slate
            database?.clearAllTables()
            
            val entity = CharacterEntity(
                id = 1,
                heroName = name, heroClass = heroClass.uppercase(),
                globalLevel = 1, currentXp = 0, campfireStreak = 1,
                selectedAttributeNames = selectedAttributes.joinToString(","),
                lastLoginMillis = System.currentTimeMillis(),
                onboardingComplete = true
            )
            database?.characterDao()?.updateCharacter(entity)
            
            // Generate Campaign Quests
            baselines.forEach { (discName, baseline) ->
                val goal = goals[discName] ?: (baseline * 1.5f)
                val disc = disciplineRegistry.find { d -> d.name == discName }
                val attributeCategory = disc?.attributeCategory ?: "Mindfulness"
                val config = getTacticalConfig(discName)
                
                val durationDays = _chosenTimelineMoons.value * 30
                
                val quests = SystematicQuestEngine.generateCampaignQuests(
                    heroClass = entity.heroClass,
                    activityName = discName,
                    attributeCategory = attributeCategory,
                    userGoal = goal,
                    durationDays = durationDays,
                    unit = config.unit
                )

                Log.d("BECOMING", "Generating ${quests.size} quests for $discName")
                quests.forEach { q -> 
                    database?.questDao()?.upsertQuest(q) 
                }
            }
        }
        
        Prefs.setOnboardingComplete(context, true)
        
        val totalXp = _chosenTimelineMoons.value * 10000
        val stories = when (heroClass.uppercase()) {
            "KNIGHT" -> listOf(
                CampaignStory("k1", "The Iron Vanguard", "Transform your body into an unbreakable fortress.", totalXp),
                CampaignStory("k2", "The Dragon's Ascent", "Push your cardiovascular limits.", totalXp),
                CampaignStory("k3", "Shield of the Realm", "Health is the highest form of defense.", totalXp)
            )
            "MAGE" -> listOf(
                CampaignStory("m1", "The Archmage's Trial", "Absorb the knowledge of the waking world.", totalXp),
                CampaignStory("m2", "The Scholar's Path", "Banish modern distractions.", totalXp),
                CampaignStory("m3", "The Chronomancer", "Master the flow of time.", totalXp)
            )
            "ARTISAN" -> listOf(
                CampaignStory("a1", "The Master's Opus", "Dedicate yourself fully to your primary craft.", totalXp),
                CampaignStory("a2", "The Golden Forge", "Build your financial independence.", totalXp),
                CampaignStory("a3", "The Architect's Dream", "Design the life you want to live.", totalXp)
            )
            else -> listOf(
                CampaignStory("r1", "The Pathfinder's Journey", "Explore the wild world.", totalXp),
                CampaignStory("r2", "The Wild Hunt", "Track down your anxieties.", totalXp),
                CampaignStory("r3", "The Apex Explorer", "Step out of your comfort zone.", totalXp)
            )
        }
        _generatedStories.value = stories
    }

    fun sealCampaign(story: CampaignStory, context: Context) {}

    fun addCustomQuest(baseTask: String, timeScale: String, attributeName: String) {
        viewModelScope.launch {
            val newQuest = QuestEntity(
                id = "custom_${System.currentTimeMillis()}",
                path = _charState.value.heroClass,
                baseTask = baseTask,
                timeScale = timeScale,
                associatedTrait = attributeName,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis(),
                mapId = "map_custom",
                xp = if (timeScale == "MONTHLY") 2000 else if (timeScale == "WEEKLY") 750 else 150
            )
            database?.questDao()?.upsertQuest(newQuest)
        }
    }

    fun generateQuestNarrative(quest: QuestEntity): QuestFlavor {
        return QuestFlavor(
            title = quest.baseTask.split(":").first().ifEmpty { "Vanguard's Duty" },
            narrativeDesc = quest.baseTask,
            aftermathLore = "The legend records your triumph."
        )
    }

    fun toggleSound(context: Context) {
        viewModelScope.launch {
            val entity = database?.characterDao()?.getCharacterState()?.first()
            entity?.let {
                database?.characterDao()?.updateCharacter(it.copy(isSoundEnabled = !it.isSoundEnabled))
            }
        }
    }

    fun grantBounty(questId: String, xp: Int) {
        viewModelScope.launch {
            val char = database?.characterDao()?.getCharacterState()?.first()
            val quest = _activeQuests.value.find { it.id == questId }
            
            char?.let {
                val trait = quest?.associatedTrait ?: "Mindfulness"
                
                var newPF = it.attrPhysicalFitness; var newMF = it.attrMentalFocus
                var newFW = it.attrFinancialWealth; var newCO = it.attrCreativeOutput
                var newSC = it.attrSocialCharisma; var newER = it.attrEmotionalResilience
                var newDK = it.attrDeepKnowledge; var newCG = it.attrCareerGrowth
                var newM = it.attrMindfulness; var newPE = it.attrPhysicalEndurance

                when(trait) {
                    "Physical Fitness" -> newPF += xp
                    "Mental Focus" -> newMF += xp
                    "Financial Wealth" -> newFW += xp
                    "Creative Output" -> newCO += xp
                    "Social Charisma" -> newSC += xp
                    "Emotional Resilience" -> newER += xp
                    "Deep Knowledge" -> newDK += xp
                    "Career Growth" -> newCG += xp
                    "Mindfulness" -> newM += xp
                    "Physical Endurance" -> newPE += xp
                }

                var newXp = it.currentXp + xp
                var newLevel = it.globalLevel
                if (newXp >= it.globalLevel * 1000) {
                    newXp -= it.globalLevel * 1000
                    newLevel++
                }

                database?.characterDao()?.updateCharacter(it.copy(
                    globalLevel = newLevel, currentXp = newXp,
                    attrPhysicalFitness = newPF, attrMentalFocus = newMF,
                    attrFinancialWealth = newFW, attrCreativeOutput = newCO,
                    attrSocialCharisma = newSC, attrEmotionalResilience = newER,
                    attrDeepKnowledge = newDK, attrCareerGrowth = newCG,
                    attrMindfulness = newM, attrPhysicalEndurance = newPE
                ))
                database?.questDao()?.completeQuest(questId)
            }
        }
    }

    fun grantPartialBounty(questId: String, completedValue: Int, targetValue: Int, xp: Int) {
        val fraction = if (targetValue > 0) completedValue.toFloat() / targetValue.toFloat() else 1f
        val xpToGrant = (xp * fraction).roundToInt()
        grantBounty(questId, xpToGrant)
    }

    fun resetProgress(context: Context) {
        viewModelScope.launch {
            kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                database?.clearAllTables()
            }
            Prefs.setOnboardingComplete(context, false)
            _charState.value = CharacterState()
            _activeQuests.value = emptyList()
        }
    }

    fun loadData(context: Context): Boolean {
        initialize(context)
        return false
    }
}
