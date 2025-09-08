package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
// replaced tone generator with custom beep via AudioTrack
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.data.MedicationEntity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.collect
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

sealed class InAppReminderEvent {
	data class MedicationEvent(val medication: MedicationEntity) : InAppReminderEvent()
	data class ScheduleEvent(val schedule: ScheduleEntity) : InAppReminderEvent()
}

object InAppReminderController {
	private val _events = MutableSharedFlow<InAppReminderEvent>(extraBufferCapacity = 1)
	val events = _events.asSharedFlow()

	fun trigger(event: InAppReminderEvent): Boolean {
		return _events.tryEmit(event)
	}
}

@Composable
fun InAppReminderHost(
	scheduleRepository: ScheduleRepository,
	medicationRepository: com.example.myapplication.data.MedicationRepository
) {
	val context = LocalContext.current
	val schedules by scheduleRepository.getAll().collectAsState(initial = emptyList())
	val medications by medicationRepository.getActiveMedications().collectAsState(initial = emptyList())
	var currentEvent by remember { mutableStateOf<InAppReminderEvent?>(null) }
	var isVisible by remember { mutableStateOf(false) }
	val scope = rememberCoroutineScope()

	LaunchedEffect(Unit) {
		InAppReminderController.events.collect { event ->
			if (!isVisible) {
				currentEvent = event
				isVisible = true
			}
		}
	}

	// Immediately check reminders when schedules/medications change
	LaunchedEffect(schedules, medications) {
		if (currentEvent == null) {
			val now = Calendar.getInstance()
			val hhmm = String.format(Locale.getDefault(), "%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))

			// Check medication reminders first
			val dueMedication = medications.firstOrNull { med ->
				med.isActive && normalizeHHmm(med.reminderTime) == hhmm &&
				!ReminderPrefs.wasShownToday(context, "med", med.id) &&
				ReminderPrefs.getSnoozeUntil(context, "med", med.id) <= System.currentTimeMillis()
			}
			if (dueMedication != null) {
				currentEvent = InAppReminderEvent.MedicationEvent(dueMedication)
				isVisible = true
			} else {
				// Then check schedules
				val dueSchedule = schedules.firstOrNull { sch ->
					sch.status != "DONE" && normalizeHHmm(sch.time) == hhmm &&
					!ReminderPrefs.wasShownToday(context, "sch", sch.id) &&
					ReminderPrefs.getSnoozeUntil(context, "sch", sch.id) <= System.currentTimeMillis()
				}
				if (dueSchedule != null) {
					currentEvent = InAppReminderEvent.ScheduleEvent(dueSchedule)
					isVisible = true
				}
			}
		}
	}

	LaunchedEffect(schedules, medications) {
		while (true) {
			if (currentEvent == null) {
				val now = Calendar.getInstance()
				val hhmm = String.format(Locale.getDefault(), "%02d:%02d", now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE))

				val dueMedication = medications.firstOrNull { med ->
					med.isActive && normalizeHHmm(med.reminderTime) == hhmm &&
					!ReminderPrefs.wasShownToday(context, "med", med.id) &&
					ReminderPrefs.getSnoozeUntil(context, "med", med.id) <= System.currentTimeMillis()
				}
				if (dueMedication != null) {
					currentEvent = InAppReminderEvent.MedicationEvent(dueMedication)
					isVisible = true
				} else {
					val dueSchedule = schedules.firstOrNull { sch ->
						sch.status != "DONE" && normalizeHHmm(sch.time) == hhmm &&
						!ReminderPrefs.wasShownToday(context, "sch", sch.id) &&
						ReminderPrefs.getSnoozeUntil(context, "sch", sch.id) <= System.currentTimeMillis()
					}
					if (dueSchedule != null) {
						currentEvent = InAppReminderEvent.ScheduleEvent(dueSchedule)
						isVisible = true
					}
				}
			}
			delay(5_000) // Check every 5 seconds for better responsiveness
		}
	}

    // play a short built-in beep (not system sound) when the overlay shows up
    LaunchedEffect(currentEvent, isVisible) {
        if (isVisible && currentEvent != null) {
            SoundUtil.playBeep()
        }
    }

	if (isVisible && currentEvent != null) {
		InAppReminderOverlay(
			event = currentEvent!!,
			onConfirm = {
				when (val e = currentEvent) {
					is InAppReminderEvent.MedicationEvent -> {
						ReminderPrefs.markShownToday(context, "med", e.medication.id)
					}
					is InAppReminderEvent.ScheduleEvent -> {
						scope.launch {
							scheduleRepository.update(
								e.schedule.copy(status = "DONE")
							)
						}
						ReminderPrefs.markShownToday(context, "sch", e.schedule.id)
					}
					else -> {}
				}
				isVisible = false
				currentEvent = null
			},
			onSnooze = { minutes ->
				when (val e = currentEvent) {
					is InAppReminderEvent.MedicationEvent -> {
						ReminderPrefs.setSnoozeUntilMinutesFromNow(context, "med", e.medication.id, minutes)
					}
					is InAppReminderEvent.ScheduleEvent -> {
						ReminderPrefs.setSnoozeUntilMinutesFromNow(context, "sch", e.schedule.id, minutes)
					}
					else -> {}
				}
				isVisible = false
				currentEvent = null
			},
			onDismiss = {
				when (val e = currentEvent) {
					is InAppReminderEvent.MedicationEvent -> ReminderPrefs.markShownToday(context, "med", e.medication.id)
					is InAppReminderEvent.ScheduleEvent -> ReminderPrefs.markShownToday(context, "sch", e.schedule.id)
					else -> {}
				}
				isVisible = false
				currentEvent = null
			}
		)
	}
}

@Composable
private fun InAppReminderOverlay(
	event: InAppReminderEvent,
	onConfirm: () -> Unit,
	onSnooze: (Int) -> Unit,
	onDismiss: () -> Unit
) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color(0xCC000000)),
		contentAlignment = Alignment.Center
	) {
		Card(
			shape = RoundedCornerShape(24.dp),
			colors = CardDefaults.cardColors(containerColor = Color.White),
			elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
			modifier = Modifier
				.fillMaxWidth(0.82f)
				.wrapContentHeight()
				.padding(16.dp)
		) {
			Column(
				modifier = Modifier.padding(24.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(
					text = when (event) {
						is InAppReminderEvent.MedicationEvent -> "Medication Reminder"
						is InAppReminderEvent.ScheduleEvent -> "Schedule Reminder"
					},
					style = MaterialTheme.typography.headlineSmall,
					fontWeight = FontWeight.Bold,
					color = MaterialTheme.colorScheme.primary
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = when (event) {
						is InAppReminderEvent.MedicationEvent -> "${event.medication.name} (${event.medication.dosage})"
						is InAppReminderEvent.ScheduleEvent -> event.schedule.title
					},
					fontSize = 20.sp,
					fontWeight = FontWeight.Medium,
					color = Color(0xFF222B45)
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = when (event) {
						is InAppReminderEvent.MedicationEvent -> "Time: ${event.medication.reminderTime}"
						is InAppReminderEvent.ScheduleEvent -> "Time: ${event.schedule.time}"
					},
					fontSize = 16.sp,
					color = Color(0xFF5A5A89)
				)
				Spacer(modifier = Modifier.height(16.dp))
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.SpaceBetween
				) {
					TextButton(onClick = onDismiss) { Text("Close") }
					Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
						TextButton(onClick = { onSnooze(5) }) { Text("Snooze 5m") }
						TextButton(onClick = { onSnooze(10) }) { Text("Snooze 10m") }
						Button(onClick = onConfirm) {
							Text(
								text = when (event) {
									is InAppReminderEvent.MedicationEvent -> "Taken"
									is InAppReminderEvent.ScheduleEvent -> "Completed"
								}
							)
						}
					}
				}
			}
		}
	}
}

private object ReminderPrefs {
	private const val PREFS_NAME = "in_app_reminders"

	private fun prefs(context: Context): SharedPreferences =
		context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

	private fun todayKey(): String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(System.currentTimeMillis())

	fun wasShownToday(context: Context, type: String, id: Int): Boolean {
		val key = "shown_${type}_${id}_${todayKey()}"
		return prefs(context).getBoolean(key, false)
	}

	fun markShownToday(context: Context, type: String, id: Int) {
		val key = "shown_${type}_${id}_${todayKey()}"
		prefs(context).edit().putBoolean(key, true).apply()
	}

	fun getSnoozeUntil(context: Context, type: String, id: Int): Long {
		val key = "snooze_${type}_${id}"
		return prefs(context).getLong(key, 0L)
	}

	fun setSnoozeUntilMinutesFromNow(context: Context, type: String, id: Int, minutes: Int) {
		val until = System.currentTimeMillis() + minutes * 60_000L
		val key = "snooze_${type}_${id}"
		prefs(context).edit().putLong(key, until).apply()
	}
}

private fun normalizeHHmm(input: String): String {
    if (input.isBlank() || input == "--:--") return "00:00"
    
    val parts = input.trim().split(":")
    if (parts.size != 2) return "00:00"
    
    val h = parts[0].toIntOrNull() ?: 0
    val m = parts[1].toIntOrNull() ?: 0
    
    // Ensure hour is within 0-23 and minute within 0-59
    val normalizedH = (h % 24).coerceAtLeast(0)
    val normalizedM = (m % 60).coerceAtLeast(0)
    
    return String.format(Locale.getDefault(), "%02d:%02d", normalizedH, normalizedM)
}

// removed old tone-based beeper; using SoundUtil


