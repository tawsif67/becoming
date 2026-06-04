package com.example.becoming

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class AlchemistResult(
    val foodName: String,
    val proteinGrams: Int,
    val carbsGrams: Int,
    val energyType: String, // "Elixir of Strength", "Stamina Ration", "Sirens' Feast"
    val comment: String
)

class GeminiService {
    private val apiKey = "AQ.Ab8RN6I6frdaWOy-Ia-9ptb-xPpAM6QDTW0qWimyJGGHVWuq4Q"
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeFood(bitmap: Bitmap): AlchemistResult? {
        val prompt = """
            Analyze this food image. 
            Identify the food and estimate the grams of protein and carbs.
            Based on the nutritional value, categorize it as:
            1. 'Elixir of Strength' if high in protein (>15g).
            2. 'Stamina Ration' if high in carbs but healthy.
            3. 'Sirens\' Feast' if it is junk food or very unhealthy.
            
            Return the result strictly in this JSON format:
            {
              "foodName": "string",
              "proteinGrams": int,
              "carbsGrams": int,
              "energyType": "string",
              "comment": "a short, medieval-style fantasy comment about the food"
            }
        """.trimIndent()

        return try {
            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            val text = response.text?.trim()?.removeSurrounding("```json", "```")
            if (text != null) {
                json.decodeFromString<AlchemistResult>(text)
            } else null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun verifyQuest(bitmap: Bitmap, questRequirement: String): Boolean {
        val prompt = """
            Verify if this photo shows proof of the following quest: $questRequirement.
            If the image shows the requirement (e.g., a glass of water, a sunrise, a gym), return 'VERIFIED'.
            Otherwise, return 'REJECTED'.
            Only return the single word.
        """.trimIndent()

        return try {
            val response = model.generateContent(
                content {
                    image(bitmap)
                    text(prompt)
                }
            )
            response.text?.trim()?.equals("VERIFIED", ignoreCase = true) == true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun generateRawText(prompt: String): String? {
        return try {
            val response = model.generateContent(prompt)
            response.text?.trim()?.removeSurrounding("```json", "```")
        } catch (e: Exception) {
            null
        }
    }
}
