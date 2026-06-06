package com.example.becoming.domain

import androidx.compose.ui.graphics.vector.ImageVector

enum class TacticalTool { LD, AM, HG, LT, TG }

data class TacticalConfiguration(
    val tool: TacticalTool,
    val unit: String = "",
    val label: String = "",
    val maxValue: Int = 100,
    val frequency: String = "Daily"
)

fun getTacticalConfig(activityName: String): TacticalConfiguration {
    return when (activityName) {
        // 1. Physical Fitness
        "Weightlifting / Powerlifting", "Calisthenics (Bodyweight training)", "Martial Arts (Striking)",
        "Brazilian Jiu-Jitsu / Grappling", "Gymnastics / Mobility work", "Pilates", "Team Sports (Soccer, Basketball, etc.)" -> 
            TacticalConfiguration(TacticalTool.LD, unit = "Sets/Reps")
        "Bouldering / Rock Climbing" -> TacticalConfiguration(TacticalTool.TG, label = "Difficulty")
        "High-Intensity Interval Training (HIIT)", "Power Yoga / Core training" -> 
            TacticalConfiguration(TacticalTool.HG, unit = "Minutes", maxValue = 120)

        // 2. Mental Focus
        "Pomodoro Work Sessions", "Deep Work Blocks (2+ hours uninterrupted)", "Single-tasking practice", "Speed reading practice" -> 
            TacticalConfiguration(TacticalTool.HG, unit = "Minutes", maxValue = 240)
        "Chess / Strategy Games", "Dual N-Back / Cognitive Brain Training", "Solving complex logic puzzles (Sudoku, etc.)" -> 
            TacticalConfiguration(TacticalTool.LT, frequency = "Sessions")
        "Learning complex patterns / choreography", "Digital Detox (Zero-screen time blocks)", "Fasting for mental clarity" -> 
            TacticalConfiguration(TacticalTool.TG, label = "Intensity")

        // 3. Financial Wealth
        "Daily/Weekly Budget Tracking", "Auditing and cutting unnecessary expenses", "Building an Emergency Fund" -> 
            TacticalConfiguration(TacticalTool.LT, label = "Checklist")
        "Stock Market / Index Fund Investing", "Building a Side Hustle / Freelancing", "Selling unused physical assets" -> 
            TacticalConfiguration(TacticalTool.AM, label = "Capital Value")
        "Reading Financial / Economic Literature", "Negotiation practice for salary/rates", 
        "Real Estate / Market Research", "Crypto / Web3 Education" -> 
            TacticalConfiguration(TacticalTool.AM, label = "KPIs")

        // 4. Creative Output (All AM)
        "Creative Writing / Fiction Journaling", "Drawing / Sketching / Painting", "Digital Design (UI/UX, Graphic Design)",
        "Playing a Musical Instrument", "Music Production / Beatmaking", "Coding a personal/passion project",
        "Photography", "Videography / Video Editing", "Crafting / Woodworking / DIY", "3D Modeling / Animation" -> 
            TacticalConfiguration(TacticalTool.AM, label = "Project Piece")

        // 5. Social Charisma
        "Public Speaking (e.g., Toastmasters)", "Attending Networking Events", "Initiating conversations with strangers" -> 
            TacticalConfiguration(TacticalTool.LT, label = "Instances")
        "Hosting dinners or social gatherings", "Active Listening exercises", "Reconnecting with old friends/family", "Mentoring or teaching someone" -> 
            TacticalConfiguration(TacticalTool.LT, label = "Sessions")
        "Volunteering in the community", "Taking acting or improv classes", "Debate / Persuasion practice" -> 
            TacticalConfiguration(TacticalTool.LT, label = "Events")

        // 6. Emotional Resilience
        "Cold Exposure / Ice Baths", "Voluntary Discomfort (e.g., sleeping on the floor)" -> 
            TacticalConfiguration(TacticalTool.TG, unit = "Seconds")
        "Stoic Journaling (Reflecting on challenges)", "Daily Gratitude logging", "Shadow Work / Deep Self-reflection" -> 
            TacticalConfiguration(TacticalTool.LT, label = "Entries")
        "Therapy or Counseling sessions", "Practicing positive reframing of negative events", 
        "Identifying and breaking cognitive distortions", "Practicing positive reframing of negative events", 
        "Anger management / Pause-and-reflect exercises" -> 
            TacticalConfiguration(TacticalTool.LT, label = "Sessions")

        // 7. Deep Knowledge
        "Reading Non-Fiction Books", "Academic Research / Reading Whitepapers", "Taking Online Courses (Coursera, edX)", "Watching Educational Documentaries" -> 
            TacticalConfiguration(TacticalTool.AM, label = "Target Count")
        "Listening to Long-form Educational Podcasts", "Flashcard / Spaced Repetition Study (Anki)", 
        "Writing synthesis essays or blog posts on learned topics", "Attending academic lectures or seminars" -> 
            TacticalConfiguration(TacticalTool.AM, label = "Volumes")
        "Learning a New Language (Duolingo, tutoring)", "Decoding complex systems (e.g., Machine Unlearning architectures)" -> 
            TacticalConfiguration(TacticalTool.HG, label = "Study Hours")

        // 8. Career Growth
        "Updating CV / Professional Portfolio", "Applying for new roles, PhDs, or programs", 
        "Pitching to new clients / Lead generation", "Studying for Professional Certifications" -> 
            TacticalConfiguration(TacticalTool.AM, label = "Milestones")
        "LinkedIn networking and content posting", "Seeking active feedback from managers/advisors", "Mentoring junior colleagues or students" -> 
            TacticalConfiguration(TacticalTool.LT, label = "Weekly Goal")
        "Learning industry-specific software", "Preparing and practicing for interviews", "Shadowing a senior leader or professor" -> 
            TacticalConfiguration(TacticalTool.HG, label = "Hours Spent")

        // 9. Mindfulness
        "Breathwork (Wim Hof, Box Breathing)", "Guided Meditation (Headspace, Waking Up)", "Sound Baths / Binaural Beats sessions" -> 
            TacticalConfiguration(TacticalTool.TG, unit = "Seconds/Min")
        "Nature Walks (Without technology)", "Mindful Eating (No screens during meals)", "Restorative / Yin Yoga" -> 
            TacticalConfiguration(TacticalTool.HG, unit = "Minutes")
        "Stargazing or Cloud watching", "Minimalist decluttering of physical space", "Dream journaling", "Body Scan Meditation" -> 
            TacticalConfiguration(TacticalTool.LT, label = "Sessions")

        // 10. Physical Endurance
        "Running / Jogging", "Long-distance Cycling", "Swimming", "Rucking (Hiking with a weighted pack)", "Rowing (Machine or water)" -> 
            TacticalConfiguration(TacticalTool.LD, unit = "Distance")
        "Hiking / Mountaineering", "Jump Rope / Skipping", "Stair climbing", "Marathon / Triathlon training prep" -> 
            TacticalConfiguration(TacticalTool.LD, unit = "Dist/Count")
        "Dance Cardio" -> TacticalConfiguration(TacticalTool.HG, unit = "Duration")

        else -> TacticalConfiguration(TacticalTool.LT, label = "Completion")
    }
}
