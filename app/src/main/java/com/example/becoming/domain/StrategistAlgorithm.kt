package com.example.becoming.domain

import com.example.becoming.data.QuestEntity
import java.util.UUID

object StrategistAlgorithm {
    /**
     * Advanced Linear Progression Model
     * Calculates specific, incremental goals based on a baseline and an ultimate target.
     */
    fun scheduleKnightCampaign(
        activityName: String,
        attributeCategory: String,
        baseline: Float,
        ultimateBounty: Float,
        unit: String,
        durationMoons: Int,
        heroClass: String = "KNIGHT"
    ): List<QuestEntity> {
        val totalDays = durationMoons * 30
        val totalWeeks = durationMoons * 4
        val quests = mutableListOf<QuestEntity>()
        
        val incrementalStep = (ultimateBounty - baseline) / totalDays
        val loreMap = mapOf(
            "Physical Endurance" to "Scout the Obsidian Border",
            "Physical Fitness" to "Fortify the Northern Wall",
            "Mental Focus" to "Decipher Enemy Ciphers",
            "Deep Knowledge" to "Study the Ancient Tactics",
            "Strength" to "Training with the Master-at-Arms",
            "Mindfulness" to "Guard the Silent Sanctuary",
            "Devotion" to "Vigil of the Fallen",
            "Financial Wealth" to "Secure the War Chest",
            "Creative Output" to "Forge the Vanguard's Equipment",
            "Social Charisma" to "Inspire the New Recruits",
            "Emotional Resilience" to "Temper the Spirit in the Storm"
        )
        
        val loreObjective = loreMap[attributeCategory] ?: "Patrol the Perimeter"

        // 1. Generate Daily Quests with Linear Progression
        for (day in 1..totalDays) {
            val dailyTarget = (baseline + (day * incrementalStep)).toInt().coerceAtLeast(1)
            quests.add(QuestEntity(
                id = UUID.randomUUID().toString(),
                path = heroClass,
                baseTask = "$loreObjective: $activityName",
                timeScale = "DAILY",
                associatedTrait = attributeCategory,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() + (day * 1000),
                mapId = "knight_campaign",
                targetValue = dailyTarget,
                unit = unit,
                xp = 150,
                iconType = if (attributeCategory.contains("Endurance")) "gps" else "tap"
            ))
        }

        // 2. Generate Weekly Skirmishes (Paced at 1.5x Daily)
        for (week in 1..totalWeeks) {
            val weekEndDay = week * 7
            val currentDailyBase = baseline + (weekEndDay * incrementalStep)
            val weeklyMagnitude = (currentDailyBase * 1.5f).toInt()
            
            quests.add(QuestEntity(
                id = UUID.randomUUID().toString(),
                path = heroClass,
                baseTask = "SKIRMISH: Breach the $loreObjective Lines",
                timeScale = "WEEKLY",
                associatedTrait = attributeCategory,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() + (week * 7 * 2000),
                mapId = "knight_campaign",
                targetValue = weeklyMagnitude,
                unit = unit,
                xp = 750,
                iconType = "timer"
            ))
        }

        // 3. Generate Monthly Bosses (Paced at 2x Daily)
        for (moon in 1..durationMoons) {
            val moonEndDay = moon * 30
            val currentDailyBase = baseline + (moonEndDay * incrementalStep)
            val bossMagnitude = (currentDailyBase * 2.0f).toInt()
            
            quests.add(QuestEntity(
                id = UUID.randomUUID().toString(),
                path = heroClass,
                baseTask = "BOSS: Conquering the ${attributeCategory.split(" ").last()} Climax",
                timeScale = "MONTHLY",
                associatedTrait = attributeCategory,
                status = "ACTIVE",
                createdAt = System.currentTimeMillis() + (moon * 30 * 3000),
                mapId = "knight_campaign",
                targetValue = bossMagnitude,
                unit = unit,
                xp = 2000,
                iconType = "camera"
            ))
        }

        return quests
    }
}
