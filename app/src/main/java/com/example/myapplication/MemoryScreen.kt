package com.example.myapplication

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.data.AppDatabase
import com.example.myapplication.data.MIGRATION_2_3
import com.example.myapplication.data.MIGRATION_3_4
import com.example.myapplication.data.MIGRATION_4_5

@Composable
fun MyMemoriesScreen() {
    val context = LocalContext.current
    val db = remember {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "app-db"
        ).addMigrations(MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5).fallbackToDestructiveMigration().build()
    }
    val repo = remember { MemoryRepository(db.memoryDao()) }
    val vm = remember { MemoryViewModel(repo) }
    val memories by vm.memories.collectAsState()

    var showAdd by remember { mutableStateOf(false) }
    var photoUri by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAdd = true }) { Text("+") }
        }
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            Text("My Memories", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))
            LazyVerticalGrid(columns = GridCells.Fixed(2), contentPadding = PaddingValues(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(memories) { m ->
                    Card {
                        Column(Modifier.fillMaxWidth()) {
                            Image(
                                painter = rememberAsyncImagePainter(model = m.uri),
                                contentDescription = m.title ?: "memory",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxWidth().height(140.dp)
                            )
                            Text(m.title ?: "Untitled", modifier = Modifier.padding(8.dp))
                            Text(m.description ?: "", modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                            TextButton(onClick = { vm.remove(m) }, modifier = Modifier.align(Alignment.End)) { Text("Delete") }
                        }
                    }
                }
            }
        }
    }

    if (showAdd) {
        AlertDialog(
            onDismissRequest = { showAdd = false },
            confirmButton = {
                TextButton(onClick = {
                    if (photoUri.isNotBlank()) {
                        vm.add(photoUri, title.ifBlank { null }, desc.ifBlank { null })
                        photoUri = ""; title = ""; desc = ""; showAdd = false
                    }
                }) { Text("Add") }
            },
            dismissButton = { TextButton(onClick = { showAdd = false }) { Text("Cancel") } },
            title = { Text("Add Memory") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(value = photoUri, onValueChange = { photoUri = it }, label = { Text("Photo URI (content:// or file://)") })
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (optional)") })
                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description (optional)") })
                    Text("Note: For now we use URI input; later we can add picker if needed.")
                }
            }
        )
    }
}


