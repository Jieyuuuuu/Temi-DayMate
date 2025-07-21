package com.example.myapplication.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MedicationDao {
    @Query("SELECT * FROM medications ORDER BY createdAt DESC")
    fun getAllMedications(): Flow<List<MedicationEntity>>
    
    @Query("SELECT * FROM medications WHERE isActive = 1 ORDER BY reminderTime ASC")
    fun getActiveMedications(): Flow<List<MedicationEntity>>
    
    @Query("SELECT * FROM medications WHERE id = :id")
    fun getMedicationById(id: Int): Flow<MedicationEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedication(medication: MedicationEntity): Long
    
    @Update
    suspend fun updateMedication(medication: MedicationEntity)
    
    @Delete
    suspend fun deleteMedication(medication: MedicationEntity)
    
    @Query("DELETE FROM medications WHERE id = :id")
    suspend fun deleteMedicationById(id: Int)
    
    @Query("UPDATE medications SET isActive = :isActive WHERE id = :id")
    suspend fun updateMedicationActive(id: Int, isActive: Boolean)
} 