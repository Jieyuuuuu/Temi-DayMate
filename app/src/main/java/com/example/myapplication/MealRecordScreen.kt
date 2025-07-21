package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.MIGRATION_2_3
import com.example.myapplication.data.MIGRATION_3_4
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun MealRecordScreen(
    navController: NavController,
    viewModel: MealRecordViewModel = viewModel(
        factory = MealRecordViewModelFactory(
            MealRepository(
                androidx.room.Room.databaseBuilder(
                    LocalContext.current.applicationContext,
                    AppDatabase::class.java,
                    "app-db"
                ).addMigrations(MIGRATION_2_3, MIGRATION_3_4).fallbackToDestructiveMigration().build().mealDao()
            )
        )
    )
) {
    val mealRecords by viewModel.todayMealRecords.collectAsState(initial = emptyList())
    val dailyWaterIntake by viewModel.dailyWaterIntake.collectAsState(initial = 0)
    var showAddDialog by remember { mutableStateOf(false) }
    var selectedMealType by remember { mutableStateOf("") }
    
    LaunchedEffect(Unit) {
        viewModel.loadTodayRecords()
        viewModel.loadDailyWaterIntake()
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFe0eafc),
                        Color(0xFFcfdef3),
                        Color(0xFFa1c4fd)
                    )
                )
            )
            .padding(16.dp)
    ) {
        // Title
        Text(
            text = "Meal Record",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        
        // Water intake tracking card
        WaterIntakeCard(
            currentIntake = dailyWaterIntake,
            onAddWater = { viewModel.addWaterIntake(250) }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick add buttons
        QuickAddButtons(
            onMealTypeSelected = { mealType ->
                selectedMealType = mealType
                showAddDialog = true
            }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Today's records
        Text(
            text = "Today's Records",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1A237E),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(mealRecords) { record ->
                MealRecordCard(
                    record = record,
                    onDelete = { viewModel.deleteMealRecord(record) }
                )
            }
        }
    }
    
    // 新增記錄對話框
    if (showAddDialog) {
        MealRecordDialog(
            mealType = selectedMealType,
            onDismiss = { showAddDialog = false },
            onSave = { mealRecord ->
                viewModel.addMealRecord(mealRecord)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun WaterIntakeCard(
    currentIntake: Int,
    onAddWater: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Water Intake Today",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A237E)
                )
                Text(
                    text = "${currentIntake} ml",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF007AFF)
                )
            }
            
            Button(
                onClick = onAddWater,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF007AFF)
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add Water",
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("+250ml")
            }
        }
    }
}

@Composable
fun QuickAddButtons(
    onMealTypeSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickAddButton(
                text = "Breakfast",
                icon = Icons.Default.Star,
                color = Color(0xFFFF9800),
                modifier = Modifier.weight(1f),
                onClick = { onMealTypeSelected("breakfast") }
            )
            QuickAddButton(
                text = "Lunch",
                icon = Icons.Default.Favorite,
                color = Color(0xFF4CAF50),
                modifier = Modifier.weight(1f),
                onClick = { onMealTypeSelected("lunch") }
            )
        }
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            QuickAddButton(
                text = "Dinner",
                icon = Icons.Default.ThumbUp,
                color = Color(0xFF9C27B0),
                modifier = Modifier.weight(1f),
                onClick = { onMealTypeSelected("dinner") }
            )
            QuickAddButton(
                text = "Snack",
                icon = Icons.Default.Favorite,
                color = Color(0xFFE91E63),
                modifier = Modifier.weight(1f),
                onClick = { onMealTypeSelected("snack") }
            )
        }
    }
}

@Composable
fun QuickAddButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        colors = ButtonDefaults.buttonColors(
            containerColor = color
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = text,
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = text,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@Composable
fun MealRecordCard(
    record: MealRecord,
    onDelete: () -> Unit
) {
    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val mealTypeText = when (record.mealType) {
        "breakfast" -> "Breakfast"
        "lunch" -> "Lunch"
        "dinner" -> "Dinner"
        "snack" -> "Snack"
        else -> record.mealType
    }
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color.White.copy(alpha = 0.9f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = mealTypeText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E)
                )
                Text(
                    text = timeFormat.format(record.timestamp),
                    fontSize = 14.sp,
                    color = Color(0xFF666666)
                )
                record.description?.let { desc ->
                    if (desc.isNotEmpty()) {
                        Text(
                            text = desc,
                            fontSize = 12.sp,
                            color = Color(0xFF888888),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                record.mood?.let { mood ->
                    MoodIcon(mood = mood)
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete",
                        tint = Color(0xFFE57373)
                    )
                }
            }
        }
    }
}

@Composable
fun MoodIcon(mood: String) {
    val icon = when (mood) {
        "happy" -> Icons.Default.ThumbUp
        "sad" -> Icons.Default.Favorite
        "excited" -> Icons.Default.Star
        else -> Icons.Default.Favorite
    }
    
    val color = when (mood) {
        "happy" -> Color(0xFF4CAF50)
        "sad" -> Color(0xFFE57373)
        "excited" -> Color(0xFFFF9800)
        else -> Color(0xFF9E9E9E)
    }
    
    Icon(
        imageVector = icon,
        contentDescription = mood,
        tint = color,
        modifier = Modifier.size(24.dp)
    )
} 