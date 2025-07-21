package com.example.myapplication

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapplication.data.MedicationEntity
import com.example.myapplication.data.MedicationRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.platform.LocalContext

@Composable
fun MedicationReminderScreen(
    medicationRepository: MedicationRepository,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val medications by medicationRepository.getAllMedications().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var selectedMedication by remember { mutableStateOf<MedicationEntity?>(null) }
    val scope = rememberCoroutineScope()
    
    // 初始化通知頻道
    LaunchedEffect(Unit) {
        MedicationNotificationService.createNotificationChannel(context)
    }
    
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Medication Reminder",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medication")
            }
        }
        
        // Statistics Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Medications",
                value = medications.size.toString(),
                icon = Icons.Default.Add,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            
            StatCard(
                title = "Active Reminders",
                value = medications.count { it.isActive }.toString(),
                icon = Icons.Default.Notifications,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
        }
        
        // Medications List
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(medications) { medication ->
                MedicationCard(
                    medication = medication,
                    onEdit = {
                        selectedMedication = medication
                        showEditDialog = true
                    },
                    onDelete = {
                        scope.launch {
                            // 取消系統提醒
                            MedicationNotificationService.cancelMedicationReminder(context, medication.id)
                            medicationRepository.deleteMedication(medication)
                        }
                    },
                    onToggleActive = {
                        scope.launch {
                            val updatedMedication = medication.copy(isActive = !medication.isActive)
                            medicationRepository.updateMedication(updatedMedication)
                            
                            // 管理系統提醒
                            if (updatedMedication.isActive) {
                                MedicationNotificationService.scheduleMedicationReminder(context, updatedMedication)
                            } else {
                                MedicationNotificationService.cancelMedicationReminder(context, medication.id)
                            }
                        }
                    }
                )
            }
        }
    }
    
    // Add Medication Dialog
    if (showAddDialog) {
        MedicationDialog(
            medication = null,
            onDismiss = { showAddDialog = false },
            onSave = { name, dosage, reminderTime, note ->
                scope.launch {
                    val newMedication = MedicationEntity(
                        id = 0,
                        name = name,
                        dosage = dosage,
                        reminderTime = reminderTime,
                        note = note,
                        isActive = true,
                        createdAt = System.currentTimeMillis()
                    )
                    val insertedId = medicationRepository.insertMedication(newMedication)
                    
                    // 設定系統提醒
                    val medicationWithId = newMedication.copy(id = insertedId.toInt())
                    MedicationNotificationService.scheduleMedicationReminder(context, medicationWithId)
                    
                    showAddDialog = false
                }
            }
        )
    }
    
    // Edit Medication Dialog
    if (showEditDialog && selectedMedication != null) {
        MedicationDialog(
            medication = selectedMedication,
            onDismiss = { 
                showEditDialog = false
                selectedMedication = null
            },
            onSave = { name, dosage, reminderTime, note ->
                scope.launch {
                    selectedMedication?.let { medication ->
                        val updatedMedication = medication.copy(
                            name = name,
                            dosage = dosage,
                            reminderTime = reminderTime,
                            note = note
                        )
                        medicationRepository.updateMedication(updatedMedication)
                        
                        // 重新設定系統提醒
                        if (updatedMedication.isActive) {
                            MedicationNotificationService.cancelMedicationReminder(context, medication.id)
                            MedicationNotificationService.scheduleMedicationReminder(context, updatedMedication)
                        } else {
                            MedicationNotificationService.cancelMedicationReminder(context, medication.id)
                        }
                    }
                    showEditDialog = false
                    selectedMedication = null
                }
            }
        )
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun MedicationCard(
    medication: MedicationEntity,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = medication.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "Dosage: ${medication.dosage}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Text(
                        text = "Reminder: ${medication.reminderTime}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (!medication.note.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = medication.note ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // Active/Inactive Toggle
                    Switch(
                        checked = medication.isActive,
                        onCheckedChange = { onToggleActive() },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Action Buttons
                    Row {
                        IconButton(onClick = onEdit) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                        
                        IconButton(onClick = onDelete) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Delete",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            
            // Status Indicator
            Row(
                modifier = Modifier.padding(top = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (medication.isActive) Color.Green else Color.Gray,
                            shape = CircleShape
                        )
                )
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = if (medication.isActive) "Active" else "Inactive",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (medication.isActive) Color.Green else Color.Gray
                )
            }
        }
    }
} 