package com.example.becoming.domain

object DisciplineLexicon {
    fun getLoreAction(disciplineName: String, heroClass: String): String {
        return when (heroClass.uppercase()) {
            "KNIGHT" -> when {
                disciplineName.contains("Weightlifting") -> "striking the heavy training dummies to forge your iron will"
                disciplineName.contains("Running") -> "patrolling the high ridges to scout for Obsidian scouts"
                disciplineName.contains("Yoga") -> "centering your balance before the coming siege"
                disciplineName.contains("Deep Work") -> "fortifying your focus to decipher the enemy's formation"
                else -> "performing the duties of the Vanguard"
            }
            "MAGE" -> when {
                disciplineName.contains("Research") -> "scrying the ancient runes to uncover the latent forces"
                disciplineName.contains("Coding") -> "weaving the runic formulas into a stable reality"
                disciplineName.contains("Meditation") -> "regenerating your mana in the silence of the sanctum"
                else -> "conducting the rites of enlightenment"
            }
            "ARTISAN" -> when {
                disciplineName.contains("Design") -> "refining the blueprints of the great cathedral"
                disciplineName.contains("Crafting") -> "shaping the raw essence into an enduring masterpiece"
                else -> "dedicating your soul to the atelier"
            }
            "RANGER" -> when {
                disciplineName.contains("Steps") -> "synchronizing your rhythm with the Great Grid's pulse"
                disciplineName.contains("Walking") -> "scanning the forest floor for leylines fractures"
                else -> "maintaining the warden's watch"
            }
            else -> "pursuing your legendary path"
        }
    }
}
