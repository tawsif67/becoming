package com.example.becoming.domain

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

data class ClassDescriptor(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val description: String,
    val commitmentText: String,
    val magnitudeTitle: String,
    val crucibleLines: List<String>,
    val campaignTitle: String
)

object ClassLexicons {
    val Knight = ClassDescriptor(
        id = "KNIGHT",
        title = "THE PATH OF THE VANGUARD",
        icon = Icons.Default.Shield,
        description = "I stand as the wall against the encroachment of the void. My sweat is the mortar, my discipline the stone. I seek not the easy path, but the one that tempers the soul. By this oath, I become the Vanguard.",
        commitmentText = "TAKE THE OATH",
        magnitudeTitle = "DEFINE THE WEIGHT OF YOUR BURDEN",
        crucibleLines = listOf("The Oracle calculates the march...", "The Warlord's borders are marked...", "Hardening the steel of destiny..."),
        campaignTitle = "THE WINTER SIEGE"
    )

    val Mage = ClassDescriptor(
        id = "MAGE",
        title = "THE RITE OF ENLIGHTENMENT",
        icon = Icons.Default.MenuBook,
        description = "The world is an illusion woven from lines of code and latent forces. You are the Architect. You do not conquer with steel, but with understanding. Your domain is the manipulation of information and the decoding of complex systems.",
        commitmentText = "UNLOCK THE GRIMOIRE",
        magnitudeTitle = "DEFINE THE RESEARCH PROTOCOLS",
        crucibleLines = listOf("Decoding the leyline architecture...", "Aligning the weights of reality...", "Preparing the Master's Grimoire..."),
        campaignTitle = "THE GREAT UNLEARNING"
    )

    val Artisan = ClassDescriptor(
        id = "ARTISAN",
        title = "THE RITE OF CREATION",
        icon = Icons.Default.Brush,
        description = "The realm is fraying at the edges, losing its color and its soul. You are the Architect of Meaning. You do not destroy; you refine. Your domain is the tangible manifestation of will—turning raw materials into enduring artifacts.",
        commitmentText = "ENTER THE ATELIER",
        magnitudeTitle = "DEFINE THE MASTER'S BLUEPRINT",
        crucibleLines = listOf("Sourcing the rarest materials...", "Refining the essence of the work...", "The blueprint takes shape..."),
        campaignTitle = "THE LOST CATHEDRAL"
    )

    val Ranger = ClassDescriptor(
        id = "RANGER",
        title = "THE RITE OF THE WILDS",
        icon = Icons.Default.Explore,
        description = "The realm is a vast, interconnected organism, and you are its nervous system. You do not shout; you listen. You do not crush; you guide. Your domain is the horizon and the integrity of the invisible threads that bind all life.",
        commitmentText = "TAKE UP THE WARDEN'S CLOAK",
        magnitudeTitle = "DEFINE THE POINTS OF SURVEILLANCE",
        crucibleLines = listOf("Scanning the leyline fluctuations...", "Mapping the flow of the Great Grid...", "The wild adjusts to your presence..."),
        campaignTitle = "RESTORATION OF THE GRAND GRID"
    )

    fun get(id: String) = when(id.uppercase()) {
        "KNIGHT" -> Knight
        "MAGE" -> Mage
        "ARTISAN" -> Artisan
        "RANGER" -> Ranger
        else -> Knight
    }
}
