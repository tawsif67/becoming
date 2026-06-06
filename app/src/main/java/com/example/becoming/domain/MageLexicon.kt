package com.example.becoming.domain

object MageLexicon {
    val description = """
        The world is an illusion woven from lines of code and latent forces. 
        You are the Architect. You do not conquer with steel, but with understanding. 
        Your domain is the manipulation of information, the decoding of complex systems, 
        and the preservation of truth in a chaotic realm.
    """.trimIndent()

    val taskMap = mapOf(
        "Reading Non-Fiction Books" to "Scrying the Ancient Runes",
        "Academic Research / Reading Whitepapers" to "Scrying the Ancient Runes",
        "Deep Work Blocks (2+ hours uninterrupted)" to "Forging the Runic Formula",
        "Coding a personal/passion project" to "Forging the Runic Formula",
        "Creative Writing / Fiction Journaling" to "Transcribing Forbidden Knowledge",
        "Data Processing" to "Filtering the Chaos of the Leylines",
        "Solving complex logic puzzles (Sudoku, etc.)" to "Decoding the Leyline Architecture"
    )

    val crucibleLines = listOf(
        "Decoding the leyline architecture...",
        "Aligning the weights of reality...",
        "Preparing the Master's Grimoire..."
    )

    fun getArcaneTask(baseTask: String): String {
        return taskMap[baseTask] ?: "Performing the Arcane Ritual"
    }
}
