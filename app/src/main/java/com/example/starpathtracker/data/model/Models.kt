package com.example.starpathtracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class StarPathDuty(
    val id: String,
    val title: String,
    val howToComplete: String = "",
    val requirement: String = "",
    val tokenReward: Int = 0,
    val section: String = "Main Duties",
    val isCompleted: Boolean = false,
    val notes: String = ""
) {
    val isWeeklyDuty: Boolean
        get() = section.contains("routine", ignoreCase = true) ||
                section.contains("weekly", ignoreCase = true) ||
                section.contains("week", ignoreCase = true)
}

@Serializable
data class StarPathList(
    val id: String,
    val title: String,
    val sourceUrl: String,
    val duties: List<StarPathDuty> = emptyList(),
    val lastUpdated: Long = System.currentTimeMillis()
) {
    val totalDuties: Int get() = duties.size
    val completedDuties: Int get() = duties.count { it.isCompleted }
    val progressFraction: Float get() = if (totalDuties > 0) completedDuties.toFloat() / totalDuties else 0f
    val progressPercentage: Int get() = (progressFraction * 100).toInt()
    
    val totalTokens: Int get() = duties.sumOf { it.tokenReward }
    val earnedTokens: Int get() = duties.filter { it.isCompleted }.sumOf { it.tokenReward }
    val remainingTokens: Int get() = totalTokens - earnedTokens
    
    val pathDuties: List<StarPathDuty> get() = duties.filter { !it.isWeeklyDuty }
    val weeklyDuties: List<StarPathDuty> get() = duties.filter { it.isWeeklyDuty }
    
    val sections: List<String> get() = duties.map { it.section }.distinct()
}

@Serializable
data class StarPathEntry(
    val title: String,
    val url: String
)

@Serializable
data class StarPathPreset(
    val id: String,
    val title: String,
    val url: String,
    val description: String = "",
    val assetFileName: String = ""
)
