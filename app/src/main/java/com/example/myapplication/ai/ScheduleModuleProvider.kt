package com.example.myapplication.ai

import com.example.myapplication.ScheduleEntity
import com.example.myapplication.ScheduleRepository
import kotlinx.coroutines.flow.first

class ScheduleModuleProvider(
    private val scheduleRepository: ScheduleRepository
) : ModuleDataProvider {
    override suspend fun getModuleData(): ModuleData {
        val schedules = scheduleRepository.getAll().first()
        val completedCount = schedules.count { schedule -> schedule.status == "DONE" }
        val totalCount = schedules.size
        val todaySchedules = schedules
        val scheduleList = schedules.map { schedule ->
            mapOf(
                "id" to schedule.id,
                "title" to schedule.title,
                "time" to schedule.time,
                "status" to schedule.status,
                "icon" to schedule.icon,
                "note" to schedule.note,
                "isDone" to schedule.isDone
            )
        }
        val dataMap = mutableMapOf<String, Any>()
        dataMap["totalSchedules"] = totalCount
        dataMap["completedSchedules"] = completedCount
        dataMap["todaySchedules"] = todaySchedules.size
        dataMap["completionRate"] = if (totalCount > 0) (completedCount.toFloat() / totalCount) else 0f
        dataMap["nextSchedule"] = schedules.firstOrNull { schedule -> schedule.status != "DONE" }?.title ?: "None"
        dataMap["schedules"] = scheduleList
        return ModuleData(
            moduleName = "My Schedule",
            data = dataMap,
            lastUpdated = System.currentTimeMillis(),
            priority = 1,
            dataType = "schedule"
        )
    }
    override suspend fun getModuleState(): ModuleState {
        val schedules = scheduleRepository.getAll().first()
        val hasActiveSchedules = schedules.any { schedule -> schedule.status != "DONE" }
        val stateMap = mutableMapOf<String, Any>()
        stateMap["activeSchedules"] = schedules.count { schedule -> schedule.status != "DONE" }
        stateMap["totalSchedules"] = schedules.size
        return ModuleState(
            moduleName = "My Schedule",
            isActive = hasActiveSchedules,
            lastInteraction = System.currentTimeMillis(),
            data = stateMap
        )
    }
    override fun getModuleActions(): List<ModuleAction> {
        return listOf(
            ModuleAction(
                actionName = "addSchedule",
                description = "Add schedule",
                parameters = mapOf(
                    "title" to "",
                    "time" to ""
                ),
                isAvailable = true
            ),
            ModuleAction(
                actionName = "completeSchedule",
                description = "Complete schedule",
                parameters = mapOf(
                    "scheduleId" to ""
                ),
                isAvailable = true
            ),
            ModuleAction(
                actionName = "deleteSchedule",
                description = "Delete schedule",
                parameters = mapOf(
                    "scheduleId" to ""
                ),
                isAvailable = true
            ),
            ModuleAction(
                actionName = "viewTodaySchedules",
                description = "View today's schedules",
                parameters = emptyMap(),
                isAvailable = true
            )
        )
    }
    override suspend fun getModuleContext(): Map<String, Any> {
        val schedules = scheduleRepository.getAll().first()
        val todaySchedules = schedules
        val nextSchedule = schedules.firstOrNull { schedule -> schedule.status != "DONE" }
        val contextMap = mutableMapOf<String, Any>()
        contextMap["moduleName"] = "My Schedule"
        contextMap["todaySchedules"] = todaySchedules.size
        contextMap["nextSchedule"] = nextSchedule?.title ?: "None"
        contextMap["nextScheduleTime"] = nextSchedule?.time ?: ""
        contextMap["completionRate"] = if (schedules.isNotEmpty()) {
            schedules.count { schedule -> schedule.status == "DONE" }.toFloat() / schedules.size
        } else 0f
        return contextMap
    }
} 