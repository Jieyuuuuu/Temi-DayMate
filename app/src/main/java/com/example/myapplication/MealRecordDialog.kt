package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.window.Dialog
import java.util.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import android.provider.MediaStore
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.Image
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.core.content.FileProvider
import java.io.File
import coil.compose.rememberAsyncImagePainter
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

@Composable
fun MealRecordDialog(
    mealType: String,
    onDismiss: () -> Unit,
    onSave: (MealRecord) -> Unit
) {
    var description by remember { mutableStateOf("") }
    var selectedMood by remember { mutableStateOf<String?>(null) }
    var waterIntake by remember { mutableStateOf(0) }
    var photoUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    // Camera and picker launcher
    val cameraImageUri = remember { mutableStateOf<Uri?>(null) }
    val takePictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            photoUri = cameraImageUri.value
        }
    }
    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            photoUri = uri
        }
    }
    // Permission request launcher
    val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            val photoFile = File.createTempFile("meal_photo_", ".jpg", context.cacheDir)
            val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", photoFile)
            cameraImageUri.value = uri
            takePictureLauncher.launch(uri)
        } else {
            // Optional: show permission denied message
        }
    }
    
    val mealTypeText = when (mealType) {
        "breakfast" -> "Breakfast"
        "lunch" -> "Lunch"
        "dinner" -> "Dinner"
        "snack" -> "Snack"
        else -> mealType
    }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Title
                Text(
                    text = "Add $mealTypeText Record",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1A237E),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                
                // Description input
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description (optional)") },
                    placeholder = { Text("E.g. What did you eat today?") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF007AFF),
                        unfocusedBorderColor = Color(0xFFCCCCCC)
                    )
                )
                
                // Photo preview and actions
                Text(
                    text = "Photo",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A237E),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            val cameraPermission = Manifest.permission.CAMERA
                            if (ContextCompat.checkSelfPermission(context, cameraPermission) == PackageManager.PERMISSION_GRANTED) {
                                val photoFile = File.createTempFile("meal_photo_", ".jpg", context.cacheDir)
                                val uri = FileProvider.getUriForFile(context, context.packageName + ".provider", photoFile)
                                cameraImageUri.value = uri
                                takePictureLauncher.launch(uri)
                            } else {
                                cameraPermissionLauncher.launch(cameraPermission)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF007AFF))
                    ) {
                        Text("Take Photo")
                    }
                    Button(
                        onClick = { pickImageLauncher.launch("image/*") },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) {
                        Text("Pick from Gallery")
                    }
                }
                if (photoUri != null) {
                    Image(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(bottom = 8.dp),
                        painter = rememberAsyncImagePainter(photoUri),
                        contentDescription = "Selected photo",
                        contentScale = ContentScale.Crop
                    )
                }
                
                // Meal mood selection
                Text(
                    text = "Meal Mood",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A237E),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    MoodSelectionButton(
                        mood = "excited",
                        icon = Icons.Default.Star,
                        color = Color(0xFFFF9800),
                        isSelected = selectedMood == "excited",
                        onClick = { selectedMood = "excited" }
                    )
                    MoodSelectionButton(
                        mood = "happy",
                        icon = Icons.Default.ThumbUp,
                        color = Color(0xFF4CAF50),
                        isSelected = selectedMood == "happy",
                        onClick = { selectedMood = "happy" }
                    )
                    MoodSelectionButton(
                        mood = "neutral",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFF9E9E9E),
                        isSelected = selectedMood == "neutral",
                        onClick = { selectedMood = "neutral" }
                    )
                    MoodSelectionButton(
                        mood = "sad",
                        icon = Icons.Default.Favorite,
                        color = Color(0xFFE57373),
                        isSelected = selectedMood == "sad",
                        onClick = { selectedMood = "sad" }
                    )
                }
                
                // Water intake record
                Text(
                    text = "Water Intake",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF1A237E),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { if (waterIntake > 0) waterIntake -= 250 },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Decrease",
                            tint = Color(0xFF007AFF)
                        )
                    }
                    
                    Text(
                        text = "${waterIntake} ml",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF007AFF),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    
                    IconButton(
                        onClick = { waterIntake += 250 },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = Color(0xFFE3F2FD),
                                shape = RoundedCornerShape(8.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = Color(0xFF007AFF)
                        )
                    }
                }
                
                // Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color(0xFF666666)
                        )
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            val mealRecord = MealRecord(
                                timestamp = Date(),
                                mealType = mealType,
                                description = description.takeIf { it.isNotEmpty() },
                                mood = selectedMood,
                                waterIntake = waterIntake,
                                isCompleted = true,
                                photoPath = photoUri?.toString()
                            )
                            onSave(mealRecord)
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF007AFF)
                        )
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@Composable
fun MoodSelectionButton(
    mood: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) color else Color(0xFFF5F5F5)
    val iconColor = if (isSelected) Color.White else color
    
    IconButton(
        onClick = onClick,
        modifier = Modifier
            .size(48.dp)
            .background(
                color = backgroundColor,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = mood,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
} 