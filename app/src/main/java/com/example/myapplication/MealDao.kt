package com.example.myapplication

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import java.util.Date

@Dao
interface MealDao {
    @Query("SELECT * FROM meal_records ORDER BY timestamp DESC")
    fun getAllMealRecords(): Flow<List<MealRecord>>
    
    @Query("SELECT * FROM meal_records WHERE mealType = :mealType ORDER BY timestamp DESC")
    fun getMealRecordsByType(mealType: String): Flow<List<MealRecord>>
    
    @Query("SELECT * FROM meal_records WHERE DATE(timestamp/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch') ORDER BY timestamp DESC")
    fun getMealRecordsByDate(date: Date): Flow<List<MealRecord>>
    
    @Query("SELECT SUM(waterIntake) FROM meal_records WHERE DATE(timestamp/1000, 'unixepoch') = DATE(:date/1000, 'unixepoch')")
    fun getDailyWaterIntake(date: Date): Flow<Int?>
    
    @Insert
    suspend fun insertMealRecord(mealRecord: MealRecord): Long
    
    @Update
    suspend fun updateMealRecord(mealRecord: MealRecord)
    
    @Delete
    suspend fun deleteMealRecord(mealRecord: MealRecord)
    
    @Query("DELETE FROM meal_records WHERE id = :id")
    suspend fun deleteMealRecordById(id: Long)
} 