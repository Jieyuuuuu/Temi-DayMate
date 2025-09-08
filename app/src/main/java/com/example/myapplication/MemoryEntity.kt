package com.example.myapplication

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "memories")
data class MemoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val uri: String,
    val title: String? = null,
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)


