package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "meal_records")
data class MealRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Date,
    val mealType: String, // "breakfast", "lunch", "dinner", "snack"
    val photoPath: String? = null,
    val mood: String? = null, // "happy", "sad", "neutral", "excited"
    val description: String? = null,
    val waterIntake: Int = 0, // Water intake (ml)
    val isCompleted: Boolean = false
) 