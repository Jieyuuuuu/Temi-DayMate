package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date

class MealRecordViewModel(
    private val mealRepository: MealRepository
) : ViewModel() {
    
    private val _todayMealRecords = MutableStateFlow<List<MealRecord>>(emptyList())
    val todayMealRecords: StateFlow<List<MealRecord>> = _todayMealRecords.asStateFlow()
    
    private val _dailyWaterIntake = MutableStateFlow(0)
    val dailyWaterIntake: StateFlow<Int> = _dailyWaterIntake.asStateFlow()
    
    fun loadTodayRecords() {
        viewModelScope.launch {
            mealRepository.getMealRecordsByDate(Date()).collect { records ->
                _todayMealRecords.value = records
            }
        }
    }
    
    fun loadDailyWaterIntake() {
        viewModelScope.launch {
            mealRepository.getDailyWaterIntake(Date()).collect { intake ->
                _dailyWaterIntake.value = intake ?: 0
            }
        }
    }
    
    fun addMealRecord(mealRecord: MealRecord) {
        viewModelScope.launch {
            mealRepository.insertMealRecord(mealRecord)
            loadTodayRecords()
        }
    }
    
    fun updateMealRecord(mealRecord: MealRecord) {
        viewModelScope.launch {
            mealRepository.updateMealRecord(mealRecord)
            loadTodayRecords()
        }
    }
    
    fun deleteMealRecord(mealRecord: MealRecord) {
        viewModelScope.launch {
            mealRepository.deleteMealRecord(mealRecord)
            loadTodayRecords()
        }
    }
    
    fun addWaterIntake(amount: Int) {
        viewModelScope.launch {
            val currentIntake = _dailyWaterIntake.value
            val newIntake = currentIntake + amount
            
            // Create a new meal record for water intake
            val waterRecord = MealRecord(
                timestamp = Date(),
                mealType = "water",
                waterIntake = amount,
                description = "Drink water ${amount}ml"
            )
            
            mealRepository.insertMealRecord(waterRecord)
            loadDailyWaterIntake()
        }
    }
} 