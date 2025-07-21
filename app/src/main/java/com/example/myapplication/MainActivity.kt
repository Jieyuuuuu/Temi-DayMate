package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.HomeScreen
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.myapplication.ScheduleItem
import com.example.myapplication.ScheduleStatus
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import androidx.compose.runtime.collectAsState
import com.example.myapplication.ScheduleEntity
import com.example.myapplication.ScheduleRepository
import com.example.myapplication.ai.*
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.MIGRATION_2_3
import com.example.myapplication.data.MIGRATION_3_4
import com.example.myapplication.data.MedicationRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface {
                    MainNavHost()
                }
            }
        }
    }
}

@Composable
fun MainNavHost() {
    val navController = rememberNavController()
    
    // AI 服務初始化
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app-db"
        ).addMigrations(MIGRATION_2_3, MIGRATION_3_4).fallbackToDestructiveMigration().build()
    }
    val scheduleRepository = remember { ScheduleRepository(db.scheduleDao()) }
    val medicationRepository = remember { MedicationRepository(db.medicationDao()) }
    val mealRepository = remember { MealRepository(db.mealDao()) }
    val moduleRegistry = remember { ModuleRegistry() }
    val scheduleModuleProvider = remember { ScheduleModuleProvider(scheduleRepository) }
    val aiService = remember { 
        GeminiAIService(
            apiKey = "AIzaSyCfIA23X1eSo0kH7K58QnHn8x3YdnViA_s",
            moduleRegistry = moduleRegistry,
            scheduleRepository = scheduleRepository,
            medicationRepository = medicationRepository,
            mealRepository = mealRepository
        )
    }
    val aiContextManager = remember { 
        AIContextManager(
            context = context,
            moduleRegistry = moduleRegistry,
            aiService = aiService
        )
    }
    
    // 註冊模組
    LaunchedEffect(Unit) {
        moduleRegistry.registerModule("schedule", scheduleModuleProvider)
    }
    
    NavHost(navController = navController, startDestination = "home") {
        composable("home") { HomeScreen(navController) }
        composable("daily_schedule") { MyScheduleScreen() }
        composable("ai_chat") { AIChatScreen(aiContextManager) }
        composable("medication_reminder") { MedicationReminderScreen(medicationRepository) }
        composable("meal_record") { MealRecordScreen(navController) }
        composable("exercise_wall") { PlaceholderScreen("Exercise & Wall") }
        composable("socialize") { PlaceholderScreen("Social") }
        composable("memory_games") { PlaceholderScreen("Memory Game") }
        composable("my_memories") { PlaceholderScreen("My Memories") }
        composable("sleep_tracker") { PlaceholderScreen("Sleep Tracking") }
        composable("settings_caregiver") { PlaceholderScreen("Settings/Caregiver") }
    }
}

@Composable
fun MyScheduleScreen() {
    val context = LocalContext.current
    // 單例 Room DB
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app-db"
        ).addMigrations(MIGRATION_2_3, MIGRATION_3_4).fallbackToDestructiveMigration().build()
    }
    val repo = remember { ScheduleRepository(db.scheduleDao()) }
    val vm = remember { MyScheduleViewModel(repo) }
    val schedules by vm.schedules.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var newTitle by remember { mutableStateOf("") }
    var newTime by remember { mutableStateOf("") }
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "My Schedule",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(top = 24.dp, bottom = 8.dp)
                )
                Divider()
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(schedules) { item ->
                        ScheduleCard(
                            item,
                            onDone = { vm.markDone(item) },
                            onDelete = { vm.removeSchedule(item) }
                        )
                    }
                }
            }
            FloatingActionButton(
                onClick = { showDialog = true },
                modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp)
            ) {
                Text("+")
            }
            if (showDialog) {
                AlertDialog(
                    onDismissRequest = { showDialog = false },
                    title = { Text("Add Schedule") },
                    text = {
                        Column {
                            OutlinedTextField(
                                value = newTitle,
                                onValueChange = { newTitle = it },
                                label = { Text("Title") }
                            )
                            OutlinedTextField(
                                value = newTime,
                                onValueChange = { newTime = it },
                                label = { Text("Time (e.g. 08:00)") }
                            )
                        }
                    },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                if (newTitle.isNotBlank() && newTime.isNotBlank()) {
                                    vm.addSchedule(
                                        ScheduleEntity(
                                            id = 0, // 使用 0，因為有 autoGenerate = true
                                            title = newTitle,
                                            time = newTime
                                        )
                                    )
                                    newTitle = ""
                                    newTime = ""
                                    showDialog = false
                                }
                            }
                        ) { Text("Add") }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDialog = false }) { Text("Cancel") }
                    }
                )
            }
        }
    }
}

@Composable
fun ScheduleCard(item: ScheduleEntity, onDone: () -> Unit, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(Color(0xFF6DD5FA)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = item.icon, fontSize = 24.sp)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = item.title, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF222B45))
                Text(text = item.time, fontSize = 14.sp, color = Color(0xFF5A5A89))
                if (item.note.isNotBlank()) {
                    Text(text = item.note, fontSize = 13.sp, color = Color(0xFF8A8A8A))
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            if (item.status != "DONE") {
                IconButton(onClick = onDone) {
                    Icon(Icons.Default.CheckCircle, contentDescription = "Done", tint = Color(0xFF4CAF50))
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFF44336))
            }
        }
    }
}

@Composable
fun PlaceholderScreen(title: String) {
    Surface(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(
                text = "$title\n(Coming soon)",
                fontSize = 22.sp,
                color = Color(0xFF1A237E),
                fontWeight = FontWeight.Medium
            )
        }
    }
}