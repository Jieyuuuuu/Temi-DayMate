package com.example.myapplication

data class ScheduleItem(
    val id: Long,
    val title: String,
    val time: String, // e.g. "08:00"
    val status: ScheduleStatus = ScheduleStatus.PENDING,
    val icon: String = "⏰",
    val note: String = "",
    val isDone: Boolean = false
)

enum class ScheduleStatus {
    PENDING, DONE, SKIPPED, LATER
} 