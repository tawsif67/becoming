package com.example.becoming.domain

object KnightLexicon {
    val oath = """
        I stand as the wall against the encroachment of the void.
        My sweat is the mortar, my discipline the stone.
        I seek not the easy path, but the one that tempers the soul.
        By this oath, I become the Vanguard.
    """.trimIndent()

    val template = "The enemy is {magnitude} {unit} away, you must {activity_verb} to secure the perimeter. Discipline: [TASK]"
    
    val dailyHooks = listOf(
        "The morning frost is bitter, Vanguard. Secure the perimeter.",
        "The armory is silent. Prepare your vessel for the coming clash.",
        "The walls do not build themselves. Carry the weight."
    )

    val weeklyHooks = listOf(
        "Enemy scouts have been spotted! Push your limits to run them down.",
        "The skirmish line is buckling. Fortify the position with your forged strength.",
        "A supply caravan needs an escort. Your endurance is our only shield."
    )

    val bossHooks = listOf(
        "The Obsidian Warlord's fortress stands before us. Every kilometer you've traversed leads to this moment.",
        "The Siege of the Obsidian Peaks has reached its climax. Hold the line or the realm falls.",
        "The final test of a true Knight. Prove your physical mastery is undeniable."
    )
}
