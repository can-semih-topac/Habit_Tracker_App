package com.cantopac.habittracker.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "habits_table")
data class Habit(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val color: Int = 0xFF2196F3.toInt(), // Varsayılan mavi renk
    val createdAt: Date = Date(),
    val isActive: Boolean = true,
    val targetDays: Int = 7, // Haftada kaç gün hedefleniyor
    val reminderTime: String? = null // "09:00" formatında
)