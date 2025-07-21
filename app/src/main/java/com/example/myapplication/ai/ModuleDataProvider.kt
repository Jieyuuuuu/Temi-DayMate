package com.example.myapplication.ai

// 模組資料提供者介面
interface ModuleDataProvider {
    suspend fun getModuleData(): ModuleData
    suspend fun getModuleState(): ModuleState
    fun getModuleActions(): List<ModuleAction>
    suspend fun getModuleContext(): Map<String, Any>
}

// 模組資料
data class ModuleData(
    val moduleName: String,
    val data: Map<String, Any>,
    val lastUpdated: Long,
    val priority: Int,
    val dataType: String
)

// 模組動作
data class ModuleAction(
    val actionName: String,
    val description: String,
    val parameters: Map<String, Any>,
    val isAvailable: Boolean
)

// 模組註冊器
class ModuleRegistry {
    private val modules = mutableMapOf<String, ModuleDataProvider>()
    
    fun registerModule(name: String, provider: ModuleDataProvider) {
        modules[name] = provider
    }
    
    fun getModule(name: String): ModuleDataProvider? {
        return modules[name]
    }
    
    fun getAllModules(): Map<String, ModuleDataProvider> {
        return modules.toMap()
    }
    
    suspend fun getAllModuleData(): List<ModuleData> {
        val result = mutableListOf<ModuleData>()
        for (provider in modules.values) {
            result.add(provider.getModuleData())
        }
        return result
    }
    
    suspend fun getAllModuleStates(): Map<String, ModuleState> {
        val result = mutableMapOf<String, ModuleState>()
        for ((name, provider) in modules) {
            result[name] = provider.getModuleState()
        }
        return result
    }
} 