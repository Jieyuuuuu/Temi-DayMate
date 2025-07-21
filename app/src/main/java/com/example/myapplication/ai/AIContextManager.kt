package com.example.myapplication.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.content.Context
import android.location.LocationManager
import android.os.BatteryManager
import android.provider.Settings
import com.example.myapplication.ScheduleEntity

class AIContextManager(
    private val context: Context,
    private val moduleRegistry: ModuleRegistry,
    private val aiService: AIService
) {
    
    private val userContextFlow = MutableStateFlow(UserContext())
    private val scope = CoroutineScope(Dispatchers.IO)
    
    init {
        // 註冊模組
        registerModules()
        
        // 定期更新上下文
        startContextUpdates()
    }
    
    private fun registerModules() {
        // 這裡會在 MainActivity 中註冊實際的模組
        // 例如：moduleRegistry.registerModule("schedule", scheduleModuleProvider)
    }
    
    private fun startContextUpdates() {
        scope.launch {
            while (true) {
                updateContext()
                kotlinx.coroutines.delay(30000) // 每30秒更新一次
            }
        }
    }
    
    suspend fun updateContext() {
        val currentContext = userContextFlow.value
        
        val updatedContext = currentContext.copy(
            currentTime = System.currentTimeMillis(),
            deviceInfo = getDeviceInfo(),
            location = getLocation(),
            moduleStates = moduleRegistry.getAllModuleStates()
        )
        
        userContextFlow.value = updatedContext
        aiService.updateContext(updatedContext)
    }
    
    suspend fun processUserQuery(query: String): AIResponse {
        val currentContext = userContextFlow.value
        return aiService.processUserQuery(query, currentContext)
    }
    
    suspend fun getContextualResponse(): AIResponse {
        val currentContext = userContextFlow.value
        return aiService.getContextualResponse(currentContext)
    }
    
    fun getContextFlow(): StateFlow<UserContext> {
        return userContextFlow.asStateFlow()
    }
    
    private fun getDeviceInfo(): DeviceInfo {
        val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
        val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
        val isCharging = batteryManager.isCharging
        
        val networkType = when {
            isWifiConnected() -> "WiFi"
            isMobileConnected() -> "Mobile"
            else -> "None"
        }
        
        val screenBrightness = Settings.System.getInt(
            context.contentResolver,
            Settings.System.SCREEN_BRIGHTNESS,
            128
        )
        
        return DeviceInfo(
            batteryLevel = batteryLevel,
            isCharging = isCharging,
            networkType = networkType,
            screenBrightness = screenBrightness
        )
    }
    
    private fun getLocation(): Location? {
        return try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            val lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            
            lastKnownLocation?.let {
                Location(
                    latitude = it.latitude,
                    longitude = it.longitude,
                    accuracy = it.accuracy,
                    timestamp = it.time
                )
            }
        } catch (e: Exception) {
            null
        }
    }
    
    private fun isWifiConnected(): Boolean {
        // 簡化實現，實際應該檢查 WiFi 連接狀態
        return true
    }
    
    private fun isMobileConnected(): Boolean {
        // 簡化實現，實際應該檢查行動網路連接狀態
        return false
    }
    
    fun updateScheduleData(schedules: List<ScheduleEntity>) {
        val currentContext = userContextFlow.value
        val updatedContext = currentContext.copy(
            scheduleItems = schedules
        )
        userContextFlow.value = updatedContext
    }
    
    fun updateUserProfile(profile: UserProfile) {
        val currentContext = userContextFlow.value
        val updatedContext = currentContext.copy(
            userProfile = profile
        )
        userContextFlow.value = updatedContext
    }
    
    fun addActivity(activity: Activity) {
        val currentContext = userContextFlow.value
        val updatedActivities = currentContext.recentActivities.toMutableList()
        updatedActivities.add(activity)
        
        // 只保留最近100個活動
        if (updatedActivities.size > 100) {
            updatedActivities.removeAt(0)
        }
        
        val updatedContext = currentContext.copy(
            recentActivities = updatedActivities
        )
        userContextFlow.value = updatedContext
    }
    
    fun addConversationEntry(entry: ConversationEntry) {
        val currentContext = userContextFlow.value
        val updatedHistory = currentContext.conversationHistory.toMutableList()
        updatedHistory.add(entry)
        
        // 只保留最近50個對話
        if (updatedHistory.size > 50) {
            updatedHistory.removeAt(0)
        }
        
        val updatedContext = currentContext.copy(
            conversationHistory = updatedHistory
        )
        userContextFlow.value = updatedContext
    }
} 