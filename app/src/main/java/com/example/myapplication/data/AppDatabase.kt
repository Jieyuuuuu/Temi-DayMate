package com.example.myapplication.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.myapplication.ScheduleEntity
import com.example.myapplication.ScheduleDao
import com.example.myapplication.data.MedicationEntity
import com.example.myapplication.data.MedicationDao
import com.example.myapplication.MealRecord
import com.example.myapplication.MealDao
import java.util.Date

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Recreate schedules table
        database.execSQL("DROP TABLE IF EXISTS schedules")
        database.execSQL("""
            CREATE TABLE schedules (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                title TEXT NOT NULL,
                time TEXT NOT NULL,
                status TEXT NOT NULL DEFAULT 'PENDING',
                icon TEXT NOT NULL DEFAULT '\u23f0',
                note TEXT NOT NULL DEFAULT '',
                isDone INTEGER NOT NULL DEFAULT 0
            )
        """)
        // Recreate medications table
        database.execSQL("DROP TABLE IF EXISTS medications")
        database.execSQL("""
            CREATE TABLE medications (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                name TEXT NOT NULL,
                dosage TEXT NOT NULL,
                reminderTime TEXT NOT NULL,
                note TEXT,
                isActive INTEGER NOT NULL DEFAULT 1,
                createdAt INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(database: SupportSQLiteDatabase) {
        // Create meal_records table
        database.execSQL("""
            CREATE TABLE meal_records (
                id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                timestamp INTEGER NOT NULL,
                mealType TEXT NOT NULL,
                photoPath TEXT,
                mood TEXT,
                description TEXT,
                waterIntake INTEGER NOT NULL DEFAULT 0,
                isCompleted INTEGER NOT NULL DEFAULT 0
            )
        """)
    }
}

@Database(
    entities = [ScheduleEntity::class, MedicationEntity::class, MealRecord::class],
    version = 4,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scheduleDao(): ScheduleDao
    abstract fun medicationDao(): MedicationDao
    abstract fun mealDao(): MealDao
}

class Converters {
    @androidx.room.TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @androidx.room.TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
} 