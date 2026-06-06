package com.example.becoming.domain

import com.example.becoming.data.QuestEntity
import java.util.Calendar
import java.util.UUID
import kotlin.math.roundToInt

enum class QuestEngineType { LOGISTICS, ARCHIVE, LEDGER, HOURGLASS, THRESHOLD }

object SystematicQuestEngine {

    fun generateCampaignQuests(
        heroClass: String,
        activityName: String,
        attributeCategory: String,
        userGoal: Float,
        durationDays: Int,
        unit: String
    ): List<QuestEntity> {
        val quests = mutableListOf<QuestEntity>()
        val engineType = mapActivityToEngine(activityName, attributeCategory)
        val baseline = userGoal / durationDays.coerceAtLeast(1)
        
        val currentTime = System.currentTimeMillis()
        val calendar = Calendar.getInstance()

        for (day in 0 until durationDays) {
            val questTime = currentTime + (day.toLong() * 24 * 60 * 60 * 1000)
            calendar.timeInMillis = questTime
            val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
            
            val multiplier = getProgressionMultiplier(dayOfWeek)
            val quantity = (baseline * multiplier).roundToInt().coerceAtLeast(1)
            
            val flavor = getFlavorHook(heroClass, engineType, multiplier >= 1.5f)
            val verb = getActionVerb(activityName, engineType)
            val metric = if (engineType == QuestEngineType.LEDGER) "session" else unit.ifEmpty { getDefaultMetric(engineType) }
            
            val finalQuantity = if (engineType == QuestEngineType.LEDGER) 1 else quantity
            
            quests.add(QuestEntity(
                id = UUID.randomUUID().toString(),
                path = heroClass,
                baseTask = "$flavor $verb $finalQuantity $metric.",
                timeScale = if (multiplier >= 1.5f) "WEEKLY" else "DAILY",
                associatedTrait = attributeCategory,
                status = "ACTIVE",
                createdAt = questTime,
                mapId = "campaign_${heroClass.lowercase()}",
                targetValue = finalQuantity,
                unit = metric,
                xp = (150 * multiplier).toInt(),
                iconType = getIconType(engineType)
            ))
        }
        return quests
    }

    private fun getProgressionMultiplier(dayOfWeek: Int): Float {
        return when (dayOfWeek) {
            Calendar.MONDAY, Calendar.TUESDAY -> 0.8f
            Calendar.WEDNESDAY, Calendar.THURSDAY, Calendar.FRIDAY -> 1.0f
            Calendar.SATURDAY -> 1.5f
            Calendar.SUNDAY -> 0.5f
            else -> 1.0f
        }
    }

    private fun mapActivityToEngine(name: String, category: String): QuestEngineType {
        return when (category) {
            "Physical Fitness", "Physical Endurance" -> QuestEngineType.LOGISTICS
            "Mental Focus" -> if (name.contains("Deep Work") || name.contains("Reading") || name.contains("Pomodoro")) QuestEngineType.HOURGLASS else QuestEngineType.LEDGER
            "Financial Wealth" -> if (name.contains("Budgeting") || name.contains("Audits")) QuestEngineType.LEDGER else QuestEngineType.ARCHIVE
            "Creative Output" -> QuestEngineType.ARCHIVE
            "Social Charisma" -> QuestEngineType.LEDGER
            "Emotional Resilience" -> if (name.contains("Cold") || name.contains("Discomfort")) QuestEngineType.THRESHOLD else QuestEngineType.LEDGER
            "Deep Knowledge" -> if (name.contains("Reading") || name.contains("Whitepapers") || name.contains("Flashcards")) QuestEngineType.ARCHIVE else QuestEngineType.HOURGLASS
            "Career Growth" -> if (name.contains("Portfolio") || name.contains("CV") || name.contains("Certs")) QuestEngineType.ARCHIVE else QuestEngineType.HOURGLASS
            "Mindfulness" -> if (name.contains("Breathwork") || name.contains("Meditation") || name.contains("Yoga")) QuestEngineType.THRESHOLD else QuestEngineType.LEDGER
            else -> QuestEngineType.LOGISTICS
        }
    }

    private fun getFlavorHook(heroClass: String, engine: QuestEngineType, isBoss: Boolean): String {
        if (isBoss) return "BOSS FIGHT: Push past your limits!"
        
        return when (heroClass.uppercase()) {
            "KNIGHT" -> "The perimeter is failing."
            "MAGE" -> "The tome reveals its secrets."
            "ARTISAN" -> "The blueprint takes shape."
            "RANGER" -> "The node requires calibration."
            else -> "The path opens."
        }
    }

    private fun getActionVerb(name: String, engine: QuestEngineType): String {
        return when (engine) {
            QuestEngineType.LOGISTICS -> if (name.contains("Run")) "Run" else if (name.contains("Lift")) "Lift" else "Complete"
            QuestEngineType.ARCHIVE -> if (name.contains("Read")) "Read" else if (name.contains("Write")) "Write" else "Draft"
            QuestEngineType.LEDGER -> if (name.contains("Audit")) "Audit" else if (name.contains("Connect")) "Connect" else "Complete"
            QuestEngineType.HOURGLASS -> "Focus"
            QuestEngineType.THRESHOLD -> if (name.contains("Breathe")) "Breathe" else "Submerge"
        }
    }

    private fun getDefaultMetric(engine: QuestEngineType): String {
        return when (engine) {
            QuestEngineType.LOGISTICS -> "units"
            QuestEngineType.ARCHIVE -> "pages"
            QuestEngineType.LEDGER -> "session"
            QuestEngineType.HOURGLASS -> "minutes"
            QuestEngineType.THRESHOLD -> "seconds"
        }
    }

    private fun getIconType(engine: QuestEngineType): String {
        return when (engine) {
            QuestEngineType.LOGISTICS -> "tap"
            QuestEngineType.ARCHIVE -> "scroll"
            QuestEngineType.LEDGER -> "rest"
            QuestEngineType.HOURGLASS -> "timer"
            QuestEngineType.THRESHOLD -> "camera"
        }
    }
}
