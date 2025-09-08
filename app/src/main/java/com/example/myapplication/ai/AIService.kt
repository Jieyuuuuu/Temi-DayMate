package com.example.myapplication.ai

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import com.example.myapplication.ScheduleEntity

// AI service core interface
interface AIService {
    suspend fun processUserQuery(query: String, context: UserContext): AIResponse
    suspend fun getContextualResponse(context: UserContext): AIResponse
    suspend fun updateContext(context: UserContext)
    fun getContextFlow(): StateFlow<UserContext>
}

// AI response structure
data class AIResponse(
    val response: String,
    val confidence: Float,
    val suggestedActions: List<SuggestedAction>,
    val contextUsed: List<String>
)

// Suggested actions
data class SuggestedAction(
    val action: String,
    val module: String,
    val parameters: Map<String, Any>,
    val priority: Int
)

// User context
data class UserContext(
    val currentTime: Long = System.currentTimeMillis(),
    val userProfile: UserProfile? = null,
    val currentModule: String = "",
    val scheduleItems: List<ScheduleEntity> = emptyList(),
    val recentActivities: List<Activity> = emptyList(),
    val deviceInfo: DeviceInfo = DeviceInfo(),
    val location: Location? = null,
    val healthData: HealthData? = null,
    val moduleStates: Map<String, ModuleState> = emptyMap(),
    val conversationHistory: List<ConversationEntry> = emptyList()
)

// User profile
data class UserProfile(
    val userId: String,
    val name: String,
    val age: Int,
    val preferences: Map<String, Any>,
    val medicalConditions: List<String>
)

// Activity log
data class Activity(
    val id: String,
    val type: String,
    val timestamp: Long,
    val duration: Long,
    val data: Map<String, Any>
)

// Device info
data class DeviceInfo(
    val batteryLevel: Int = 100,
    val isCharging: Boolean = false,
    val networkType: String = "WiFi",
    val screenBrightness: Int = 128
)

// Location info
data class Location(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float,
    val timestamp: Long
)

// Health data
data class HealthData(
    val heartRate: Int?,
    val steps: Int,
    val sleepHours: Float?,
    val mood: String?
)

// Module state
data class ModuleState(
    val moduleName: String,
    val isActive: Boolean,
    val lastInteraction: Long,
    val data: Map<String, Any>
)

// Conversation history
data class ConversationEntry(
    val timestamp: Long,
    val userInput: String,
    val aiResponse: String,
    val context: Map<String, Any>
) 