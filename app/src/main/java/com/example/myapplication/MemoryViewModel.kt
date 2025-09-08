package com.example.myapplication

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MemoryViewModel(private val repository: MemoryRepository) : ViewModel() {
    val memories = repository.getAll().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun add(uri: String, title: String?, description: String?) {
        viewModelScope.launch { repository.add(uri, title, description) }
    }

    fun remove(memory: MemoryEntity) {
        viewModelScope.launch { repository.remove(memory) }
    }
}


