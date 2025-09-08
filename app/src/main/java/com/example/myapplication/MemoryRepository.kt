package com.example.myapplication

class MemoryRepository(private val dao: MemoryDao) {
    fun getAll() = dao.getAll()
    suspend fun add(uri: String, title: String?, description: String?) {
        dao.insert(MemoryEntity(uri = uri, title = title, description = description))
    }
    suspend fun remove(memory: MemoryEntity) = dao.delete(memory)
    suspend fun removeById(id: Int) = dao.deleteById(id)
}


