package com.example.becoming.domain

import com.example.becoming.data.QuestEntity
import kotlinx.serialization.Serializable

@Serializable
data class QuestFlavor(
    val title: String,
    val narrativeDesc: String,
    val aftermathLore: String
)

object NarrativeEngine {

    fun generateQuestFlavor(
        heroClass: String,
        associatedTrait: String,
        disciplineName: String,
        targetValue: Int,
        unit: String,
        timeScale: String
    ): QuestFlavor {
        
        val unitLabel = if (unit.isEmpty()) "Sessions" else unit
        val loreAction = DisciplineLexicon.getLoreAction(disciplineName, heroClass)
        
        val title = when (timeScale.uppercase()) {
            "MONTHLY" -> "CHRONICLE CLIMAX: ${associatedTrait.uppercase()}"
            "WEEKLY" -> "THE SKIRMISH OF THE ${associatedTrait.split(" ").last().uppercase()}"
            else -> "Vanguard's Duty: $disciplineName"
        }

        val briefing = when (timeScale.uppercase()) {
            "DAILY" -> "The vanguard does not rest. You must $loreAction. The perimeter is specific: you are to complete $targetValue $unitLabel. Do not falter."
            "WEEKLY" -> "A tremor in the weave! To stabilize the region, you must $loreAction under extreme duress. Magnitude required: $targetValue $unitLabel."
            "MONTHLY" -> "THE FINAL SIEGE. The Warlord's fortress is at the gates. Your entire campaign has prepared you to $loreAction at the highest level. Reach $targetValue $unitLabel to break the siege!"
            else -> "Complete $targetValue $unitLabel of $disciplineName."
        }

        return QuestFlavor(title, briefing, "The legend records your triumph in $disciplineName.")
    }
}
