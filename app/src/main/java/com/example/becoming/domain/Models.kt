package com.example.becoming.domain

import kotlinx.serialization.Serializable

enum class QuestType { TIMER, REPS, CHECK, INPUT, GPS }

@Serializable
data class CampaignStory(val id: String, val title: String, val loreSummary: String, val totalRequiredXp: Int)

@Serializable
data class ProceduralNarrative(
    val briefing: String,
    val aftermath: String,
    val title: String
)
