package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MyScheduleViewModel(private val repo: ScheduleRepository) : ViewModel() {
    val schedules: StateFlow<List<ScheduleEntity>> =
        repo.getAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addSchedule(item: ScheduleEntity) {
        viewModelScope.launch { repo.insert(item) }
    }
    fun removeSchedule(item: ScheduleEntity) {
        viewModelScope.launch { repo.delete(item) }
    }
    fun markDone(item: ScheduleEntity) {
        viewModelScope.launch {
            repo.update(item.copy(status = "DONE"))
        }
    }
} 