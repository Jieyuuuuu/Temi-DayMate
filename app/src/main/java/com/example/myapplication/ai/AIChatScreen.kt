package com.example.myapplication.ai

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import com.example.myapplication.ai.AIVoiceSpeaker
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.ai.AIRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Composable
fun AIChatScreen(
    aiContextManager: AIContextManager,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val voiceSpeaker = remember { AIVoiceSpeaker(context) }
    var userInput by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var messages by remember { mutableStateOf(listOf<ChatMessage>()) }
    val listState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    var isListening by remember { mutableStateOf(false) }
    var sttError by remember { mutableStateOf("") }
    var sttPartial by remember { mutableStateOf("") }
    // STT: AIRecognizer
    val aiRecognizer = remember {
        AIRecognizer(
            context = context,
            onResult = { result ->
                userInput = result
                isListening = false
                sttPartial = ""
                sttError = ""
                // Auto send to AI
                scope.launch {
                    isProcessing = true
                    val userMessage = ChatMessage(
                        text = result,
                        isUser = true,
                        timestamp = System.currentTimeMillis()
                    )
                    messages = messages + userMessage
                    try {
                        val aiResponse = aiContextManager.processUserQuery(result)
                        val aiMessage = ChatMessage(
                            text = aiResponse.response,
                            isUser = false,
                            timestamp = System.currentTimeMillis(),
                            suggestedActions = aiResponse.suggestedActions
                        )
                        messages = messages + aiMessage
                        aiContextManager.addConversationEntry(
                            ConversationEntry(
                                timestamp = System.currentTimeMillis(),
                                userInput = result,
                                aiResponse = aiResponse.response,
                                context = mapOf("confidence" to aiResponse.confidence)
                            )
                        )
                    } catch (e: Exception) {
                        val errorMessage = ChatMessage(
                            text = "Sorry, an error occurred while processing your request.",
                            isUser = false,
                            timestamp = System.currentTimeMillis()
                        )
                        messages = messages + errorMessage
                    }
                    userInput = ""
                    isProcessing = false
                }
            },
            onPartialResult = { partial ->
                sttPartial = partial
                userInput = partial
            },
            onError = { err ->
                sttError = err
                isListening = false
            }
        )
    }
    
    // Auto scroll to bottom
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    // TTS: Auto speak latest AI message
    LaunchedEffect(messages) {
        val last = messages.lastOrNull()
        if (last != null && !last.isUser && last.text.isNotBlank()) {
            voiceSpeaker.speak(last.text)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            voiceSpeaker.shutdown()
            aiRecognizer.destroy()
        }
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Title + STT status
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "AI Assistant",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            val recordAudioPermission = Manifest.permission.RECORD_AUDIO
            val permissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    aiRecognizer.startListening()
                    isListening = true
                } else {
                    sttError = "Microphone permission denied"
                }
            }
            Button(
                onClick = {
                    if (!isListening && !isProcessing) {
                        sttError = ""
                        sttPartial = ""
                        if (ContextCompat.checkSelfPermission(context, recordAudioPermission) == PackageManager.PERMISSION_GRANTED) {
                            aiRecognizer.startListening()
                            isListening = true
                        } else {
                            permissionLauncher.launch(recordAudioPermission)
                        }
                    } else if (isListening) {
                        aiRecognizer.stopListening()
                        isListening = false
                    }
                },
                enabled = !isProcessing
            ) {
                Text(if (isListening) "Stop Listening" else "Voice Input")
            }
        }
        if (isListening) {
            Text(
                text = if (sttPartial.isNotBlank()) "Listening: $sttPartial" else "Listening...",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = 16.dp, bottom = 0.dp)
            )
        }
        if (sttError.isNotBlank()) {
            Text(
                text = "STT Error: $sttError",
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 16.dp, bottom = 0.dp)
            )
        }
        
        // Chat history
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(messages) { message ->
                ChatMessageItem(message = message)
            }
        }
        
        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            OutlinedTextField(
                value = userInput,
                onValueChange = { userInput = it },
                label = { Text("Enter your message...") },
                modifier = Modifier.weight(1f),
                enabled = !isProcessing,
                maxLines = 3
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Button(
                onClick = {
                    if (userInput.isNotBlank() && !isProcessing) {
                        scope.launch {
                            isProcessing = true
                            val userMessage = ChatMessage(
                                text = userInput,
                                isUser = true,
                                timestamp = System.currentTimeMillis()
                            )
                            messages = messages + userMessage
                            
                            try {
                                val aiResponse = aiContextManager.processUserQuery(userInput)
                                val aiMessage = ChatMessage(
                                    text = aiResponse.response,
                                    isUser = false,
                                    timestamp = System.currentTimeMillis(),
                                    suggestedActions = aiResponse.suggestedActions
                                )
                                messages = messages + aiMessage
                                
                                // Record conversation
                                aiContextManager.addConversationEntry(
                                    ConversationEntry(
                                        timestamp = System.currentTimeMillis(),
                                        userInput = userInput,
                                        aiResponse = aiResponse.response,
                                        context = mapOf("confidence" to aiResponse.confidence)
                                    )
                                )
                            } catch (e: Exception) {
                                val errorMessage = ChatMessage(
                                    text = "Sorry, an error occurred while processing your request.",
                                    isUser = false,
                                    timestamp = System.currentTimeMillis()
                                )
                                messages = messages + errorMessage
                            }
                            
                            userInput = ""
                            isProcessing = false
                        }
                    }
                },
                enabled = userInput.isNotBlank() && !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send")
                }
            }
        }
    }
}

@Composable
fun ChatMessageItem(message: ChatMessage) {
    val alignment = if (message.isUser) Alignment.End else Alignment.Start
    val backgroundColor = if (message.isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = alignment
    ) {
        Card(
            modifier = Modifier.widthIn(max = 280.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // Show suggested actions
                if (message.suggestedActions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Suggested actions:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    message.suggestedActions.forEach { action ->
                        Text(
                            text = "• ${action.action} (${action.module})",
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
                
                Text(
                    text = formatTime(message.timestamp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long,
    val suggestedActions: List<SuggestedAction> = emptyList()
)

private fun formatTime(timestamp: Long): String {
    val date = java.util.Date(timestamp)
    val formatter = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return formatter.format(date)
} 