package com.example.becoming.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "character_state")
data class CharacterEntity(
    @PrimaryKey val id: Int = 1, // Singleton row
    val heroName: String,
    val heroClass: String, // "KNIGHT", "MAGE", "ARTISAN", "RANGER"
    val globalLevel: Int,
    val currentXp: Int,
    val campfireStreak: Int,
    // Attributes (Levels/XP for each)
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
    // Store which names were actually chosen
    val selectedAttributeNames: String = "", // Comma separated
    val lastLoginMillis: Long = 0,
    val onboardingComplete: Boolean = false,
    val isSoundEnabled: Boolean = true
)

@Serializable
@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String,
    val path: String, 
    val baseTask: String,
    val timeScale: String, 
    val associatedTrait: String, // Matches the Attribute names
    val status: String, 
    val createdAt: Long,
    val mapId: String,
    val targetValue: Int = 0,
    val unit: String = "",
    val xp: Int = 0,
    val iconType: String = "scroll"
)

@Serializable
@Entity(tableName = "chronicles")
data class ChronicleEntity(
    @PrimaryKey val id: String,
    val questId: String,
    val generatedLore: String,
    val completedAt: Long
)
