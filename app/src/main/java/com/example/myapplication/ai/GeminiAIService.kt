package com.example.myapplication.ai

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.net.HttpURLConnection
import java.io.OutputStreamWriter
import android.util.Log
import com.example.myapplication.ScheduleEntity
import com.example.myapplication.data.MedicationEntity
import com.example.myapplication.ScheduleRepository
import com.example.myapplication.data.MedicationRepository
import com.example.myapplication.MealRepository
import com.example.myapplication.MealRecord

class GeminiAIService(
    private val apiKey: String,
    private val moduleRegistry: ModuleRegistry,
    private val scheduleRepository: ScheduleRepository,
    private val medicationRepository: MedicationRepository,
    private val mealRepository: MealRepository // added
) : AIService {
    
    private val contextFlow = MutableStateFlow(UserContext())
    // Use Gemini 1.5 Flash model
    private val modelName = "gemini-1.5-flash"
    private val baseUrl = "https://generativelanguage.googleapis.com/v1/models/$modelName:generateContent"
    
    override suspend fun processUserQuery(query: String, context: UserContext): AIResponse {
        val prompt = buildPrompt(query, context)
        
        return try {
            val response = withContext(Dispatchers.IO) { callGeminiAPI(prompt) }
            parseAIResponse(response, context)
        } catch (e: Exception) {
            Log.e("GeminiAIService", "processUserQuery error", e)
            AIResponse(
                response = "Sorry, I am unable to respond right now. Please try again later.",
                confidence = 0.0f,
                suggestedActions = emptyList(),
                contextUsed = emptyList()
            )
        }
    }
    
    override suspend fun getContextualResponse(context: UserContext): AIResponse {
        val prompt = buildContextualPrompt(context)
        
        return try {
            val response = withContext(Dispatchers.IO) { callGeminiAPI(prompt) }
            parseAIResponse(response, context)
        } catch (e: Exception) {
            Log.e("GeminiAIService", "getContextualResponse error", e)
            AIResponse(
                response = "Unable to get contextual response.",
                confidence = 0.0f,
                suggestedActions = emptyList(),
                contextUsed = emptyList()
            )
        }
    }
    
    override suspend fun updateContext(context: UserContext) {
        contextFlow.value = context
    }
    
    override fun getContextFlow(): StateFlow<UserContext> {
        return contextFlow.asStateFlow()
    }
    
    // No longer fetches schedule from context, always fetches live from repository
    private suspend fun buildPrompt(query: String, context: UserContext): String {
        val schedules: List<ScheduleEntity> = scheduleRepository.getAll().first()
        val medications: List<MedicationEntity> = medicationRepository.getAllMedications().first()
        val meals: List<MealRecord> = mealRepository.getAllMealRecords().first()
        // You can add more modules here as needed
        // Example: val medicationData = moduleRegistry.getModule("medication")?.getModuleData()

        // Dynamic retrieval based on query keywords
        val relevantScheduleItems: List<ScheduleEntity> = if (query.contains("schedule", ignoreCase = true) ||
                                       query.contains("today", ignoreCase = true) ||
                                       query.contains("schedule", ignoreCase = true)) {
            schedules
        } else {
            emptyList()
        }
        
        val relevantMedications: List<MedicationEntity> = if (query.contains("medication", ignoreCase = true) ||
                                     query.contains("medicine", ignoreCase = true) ||
                                     query.contains("pill", ignoreCase = true) ||
                                     query.contains("drug", ignoreCase = true) ||
                                    query.contains("medication", ignoreCase = true) ||
                                    query.contains("medicine", ignoreCase = true)) {
            medications
        } else {
            emptyList()
        }
        val relevantMeals: List<MealRecord> = if (query.contains("meal", ignoreCase = true) ||
                                         query.contains("food", ignoreCase = true) ||
                                         query.contains("eat", ignoreCase = true) ||
                                         query.contains("breakfast", ignoreCase = true) ||
                                         query.contains("lunch", ignoreCase = true) ||
                                         query.contains("dinner", ignoreCase = true) ||
                                         query.contains("snack", ignoreCase = true) ||
                                         query.contains("water", ignoreCase = true) ||
                                        query.contains("meal", ignoreCase = true) ||
                                        query.contains("food", ignoreCase = true)) {
            meals
        } else {
            emptyList()
        }

        return """
You are DayMate, a friendly AI companion for dementia patients. You help users manage their daily life through various app modules like schedules, medication, meals, exercise, social activities, and memory games.

Always be supportive, clear, and proactive. If the user asks about their schedule, medication, meal records, or any app feature, provide helpful information and suggestions.

User query: $query

Current time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(context.currentTime))}

User profile:
${context.userProfile?.let { "Name: ${it.name}, Age: ${it.age}" } ?: "Not set"}

Today's schedule:
${if (relevantScheduleItems.isNotEmpty()) {
    relevantScheduleItems.joinToString("\n") { schedule -> "- ${schedule.title} (${schedule.time})" }
} else {
    "No scheduled activities for today"
}}

Current medications:
${if (relevantMedications.isNotEmpty()) {
    relevantMedications.joinToString("\n") { med -> 
        "- ${med.name} (${med.dosage}) at ${med.reminderTime}${if (med.isActive) " [Active]" else " [Inactive]"}"
    }
} else {
    "No medications recorded"
}}

Recent meal records:
${if (relevantMeals.isNotEmpty()) {
    relevantMeals.joinToString("\n") { meal ->
        "- ${meal.mealType.capitalize()} at ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(meal.timestamp)}: ${meal.description ?: "No description"}, Mood: ${meal.mood ?: "-"}, Water: ${meal.waterIntake}ml"
    }
} else {
    "No meal records found"
}}

Module information:
${context.moduleStates.entries.joinToString("\n") { "- ${it.key}: ${if (it.value.isActive) "active" else "inactive"}" }}

Please provide:
1. A direct, friendly answer to the user's query
2. Relevant suggestions based on their schedule, medications, meal records, and app modules
3. Proactive reminders if needed (especially for medication times and meals)
4. Encouraging and supportive tone
5. If medication-related, remind about proper dosage and timing
6. If meal-related, summarize recent meals and water intake

Response format:
{
  "response": "Your friendly response here",
  "suggestedActions": [
    {"action": "Action name", "module": "Module name", "parameters": {}, "priority": 1}
  ],
  "contextUsed": ["Context items used"]
}
        """.trimIndent()
    }
    
    private suspend fun buildContextualPrompt(context: UserContext): String {
        val schedules: List<ScheduleEntity> = scheduleRepository.getAll().first()
        val medications: List<MedicationEntity> = medicationRepository.getAllMedications().first()
        val meals: List<MealRecord> = mealRepository.getAllMealRecords().first()
        return """
Based on the current situation, please provide proactive suggestions as DayMate:

Current time: ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(java.util.Date(context.currentTime))}

Today's schedule:
${if (schedules.isNotEmpty()) {
    schedules.joinToString("\n") { schedule -> "- ${schedule.title} (${schedule.time})" }
} else {
    "No scheduled activities for today"
}}

Current medications:
${if (medications.isNotEmpty()) {
    medications.joinToString("\n") { med -> 
        "- ${med.name} (${med.dosage}) at ${med.reminderTime}${if (med.isActive) " [Active]" else " [Inactive]"}"
    }
} else {
    "No medications recorded"
}}

Recent meal records:
${if (meals.isNotEmpty()) {
    meals.take(5).joinToString("\n") { meal ->
        "- ${meal.mealType.capitalize()} at ${java.text.SimpleDateFormat("yyyy-MM-dd HH:mm").format(meal.timestamp)}: ${meal.description ?: "No description"}, Mood: ${meal.mood ?: "-"}, Water: ${meal.waterIntake}ml"
    }
} else {
    "No meal records found"
}}

Please provide:
1. Time-related reminders
2. Medication reminders (check if it's time for any medication)
3. Meal reminders (check if it's time to eat or drink water)
4. Health suggestions
5. Activity suggestions
6. Safety reminders

Response format same as above.
        """.trimIndent()
    }
    
    private suspend fun callGeminiAPI(prompt: String): String {
        val url = URL("$baseUrl?key=$apiKey")
        val connection = url.openConnection() as HttpURLConnection
        
        connection.requestMethod = "POST"
        connection.setRequestProperty("Content-Type", "application/json")
        connection.doOutput = true
        
        val requestBody = JSONObject().apply {
            put("contents", JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            })
            put("generationConfig", JSONObject().apply {
                put("temperature", 0.7)
                put("topK", 40)
                put("topP", 0.95)
                put("maxOutputTokens", 1024)
            })
        }
        
        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(requestBody.toString())
        }
        
        // Improved error handling: read error stream if response code is not 200
        val responseCode = connection.responseCode
        return if (responseCode == 200) {
            connection.inputStream.bufferedReader().use { it.readText() }
        } else {
            val errorMsg = connection.errorStream?.bufferedReader()?.use { it.readText() } ?: "Unknown error"
            Log.e("GeminiAIService", "Gemini API error: HTTP $responseCode, $errorMsg")
            throw Exception("Gemini API error: HTTP $responseCode, $errorMsg")
        }
    }
    
    private fun parseAIResponse(response: String, context: UserContext): AIResponse {
        return try {
            val jsonResponse = JSONObject(response)
            val candidates = jsonResponse.getJSONArray("candidates")
            val content = candidates.getJSONObject(0).getJSONObject("content")
            val parts = content.getJSONArray("parts")
            val text = parts.getJSONObject(0).getString("text")
            
            // Try to parse JSON format response
            val jsonMatch = Regex("\\{.*\\}", RegexOption.DOT_MATCHES_ALL).find(text)
            if (jsonMatch != null) {
                val responseJson = JSONObject(jsonMatch.value)
                AIResponse(
                    response = responseJson.optString("response", text),
                    confidence = 0.9f,
                    suggestedActions = parseSuggestedActions(responseJson),
                    contextUsed = parseContextUsed(responseJson)
                )
            } else {
                AIResponse(
                    response = text,
                    confidence = 0.8f,
                    suggestedActions = emptyList(),
                    contextUsed = emptyList()
                )
            }
        } catch (e: Exception) {
            Log.e("GeminiAIService", "parseAIResponse error", e)
            AIResponse(
                response = "Error parsing AI response.",
                confidence = 0.0f,
                suggestedActions = emptyList(),
                contextUsed = emptyList()
            )
        }
    }
    
    private fun parseSuggestedActions(responseJson: JSONObject): List<SuggestedAction> {
        val actions = mutableListOf<SuggestedAction>()
        val actionsArray = responseJson.optJSONArray("suggestedActions")
        
        actionsArray?.let {
            for (i in 0 until it.length()) {
                val action = it.getJSONObject(i)
                actions.add(
                    SuggestedAction(
                        action = action.getString("action"),
                        module = action.getString("module"),
                        parameters = parseParameters(action.optJSONObject("parameters")),
                        priority = action.optInt("priority", 1)
                    )
                )
            }
        }
        
        return actions
    }
    
    private fun parseParameters(paramsJson: JSONObject?): Map<String, Any> {
        if (paramsJson == null) return emptyMap()
        
        val params = mutableMapOf<String, Any>()
        val keys = paramsJson.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            params[key] = paramsJson.get(key)
        }
        return params
    }
    
    private fun parseContextUsed(responseJson: JSONObject): List<String> {
        val contextUsed = mutableListOf<String>()
        val contextArray = responseJson.optJSONArray("contextUsed")
        
        contextArray?.let {
            for (i in 0 until it.length()) {
                contextUsed.add(it.getString(i))
            }
        }
        
        return contextUsed
    }
} 