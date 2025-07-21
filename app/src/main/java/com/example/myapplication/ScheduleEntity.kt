package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "schedules")
data class ScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val time: String,
    val status: String = "PENDING",
    val icon: String = "⏰",
    val note: String = "",
    val isDone: Boolean = false
) 