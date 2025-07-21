package com.example.myapplication

import kotlinx.coroutines.flow.Flow
import java.util.Date

class MealRepository(private val mealDao: MealDao) {
    
    fun getAllMealRecords(): Flow<List<MealRecord>> {
        return mealDao.getAllMealRecords()
    }
    
    fun getMealRecordsByType(mealType: String): Flow<List<MealRecord>> {
        return mealDao.getMealRecordsByType(mealType)
    }
    
    fun getMealRecordsByDate(date: Date): Flow<List<MealRecord>> {
        return mealDao.getMealRecordsByDate(date)
    }
    
    fun getDailyWaterIntake(date: Date): Flow<Int?> {
        return mealDao.getDailyWaterIntake(date)
    }
    
    suspend fun insertMealRecord(mealRecord: MealRecord): Long {
        return mealDao.insertMealRecord(mealRecord)
    }
    
    suspend fun updateMealRecord(mealRecord: MealRecord) {
        mealDao.updateMealRecord(mealRecord)
    }
    
    suspend fun deleteMealRecord(mealRecord: MealRecord) {
        mealDao.deleteMealRecord(mealRecord)
    }
    
    suspend fun deleteMealRecordById(id: Long) {
        mealDao.deleteMealRecordById(id)
    }
} 