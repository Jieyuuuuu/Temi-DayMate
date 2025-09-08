package com.example.myapplication

data class FeatureDestination(
    val name: String,
    val iconType: Int, // 0:circle, 1:square, 2:triangle, 3:star, 4:heart, 5:oval, 6:diamond, 7:hexagon, 8:semicircle
    val route: String
) {
    companion object {
        val allFeatures = listOf(
            FeatureDestination("My Schedule", 0, "daily_schedule"),
            FeatureDestination("Medication Reminder", 1, "medication_reminder"),
            FeatureDestination("Meal Record", 2, "meal_record"),
            FeatureDestination("Exercise Coach", 3, "exercise_coach"),
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