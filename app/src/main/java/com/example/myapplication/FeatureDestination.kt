package com.example.myapplication

data class FeatureDestination(
    val name: String,
    val iconType: Int, // 0:圓, 1:方, 2:三角, 3:星, 4:心, 5:橢圓, 6:菱形, 7:六角, 8:半圓
    val route: String
) {
    companion object {
        val allFeatures = listOf(
            FeatureDestination("My Schedule", 0, "daily_schedule"),
            FeatureDestination("Medication Reminder", 1, "medication_reminder"),
            FeatureDestination("Meal Record", 2, "meal_record"),
            FeatureDestination("Exercise & Wall", 3, "exercise_wall"),
            FeatureDestination("Social", 4, "socialize"),
            FeatureDestination("Memory Game", 5, "memory_games"),
            FeatureDestination("My Memories", 6, "my_memories"),
            FeatureDestination("Sleep Tracking", 7, "sleep_tracker"),
            FeatureDestination("Settings/Caregiver", 8, "settings_caregiver"),
            FeatureDestination("AI Assistant", 9, "ai_chat"),
            FeatureDestination("Face Age Detection", 0, "face_age_detection")
        )
    }
} 