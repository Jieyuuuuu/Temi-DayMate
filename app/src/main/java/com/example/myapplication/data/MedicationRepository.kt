package com.example.myapplication.data

import kotlinx.coroutines.flow.Flow

class MedicationRepository(private val medicationDao: MedicationDao) {
    
    fun getAllMedications(): Flow<List<MedicationEntity>> {
        return medicationDao.getAllMedications()
    }
    
    fun getActiveMedications(): Flow<List<MedicationEntity>> {
        return medicationDao.getActiveMedications()
    }
    
    fun getMedicationById(id: Int): Flow<MedicationEntity?> {
        return medicationDao.getMedicationById(id)
    }
    
    suspend fun insertMedication(medication: MedicationEntity): Long {
        return medicationDao.insertMedication(medication)
    }
    
    suspend fun updateMedication(medication: MedicationEntity) {
        medicationDao.updateMedication(medication)
    }
    
    suspend fun deleteMedication(medication: MedicationEntity) {
        medicationDao.deleteMedication(medication)
    }
    
    suspend fun deleteMedicationById(id: Int) {
        medicationDao.deleteMedicationById(id)
    }
    
    suspend fun toggleMedicationActive(id: Int, isActive: Boolean) {
        medicationDao.updateMedicationActive(id, isActive)
    }
} 