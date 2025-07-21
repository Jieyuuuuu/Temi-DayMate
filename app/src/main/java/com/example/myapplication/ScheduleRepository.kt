package com.example.myapplication

import kotlinx.coroutines.flow.Flow

class ScheduleRepository(private val dao: ScheduleDao) {
    fun getAll(): Flow<List<ScheduleEntity>> = dao.getAll()
    suspend fun insert(item: ScheduleEntity) = dao.insert(item)
    suspend fun delete(item: ScheduleEntity) = dao.delete(item)
    suspend fun update(item: ScheduleEntity) = dao.update(item)
} 