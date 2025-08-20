package com.cantopac.habittracker.data.repository

import androidx.lifecycle.LiveData
import com.cantopac.habittracker.data.dao.HabitDao
import com.cantopac.habittracker.data.dao.HabitEntryDao
import com.cantopac.habittracker.data.entities.Habit
import com.cantopac.habittracker.data.entities.HabitEntry
import java.util.Date

class HabitRepository(
    private val habitDao: HabitDao,
    private val habitEntryDao: HabitEntryDao
) {
    // Habit operations
    fun getAllHabits(): LiveData<List<Habit>> = habitDao.getAllActiveHabits()

    suspend fun insertHabit(habit: Habit): Long = habitDao.insertHabit(habit)

    suspend fun updateHabit(habit: Habit) = habitDao.updateHabit(habit)

    suspend fun deleteHabit(habit: Habit) = habitDao.deleteHabit(habit)

    suspend fun getHabitById(id: Long): Habit? = habitDao.getHabitById(id)

    // HabitEntry operations
    suspend fun getEntriesForHabitSync(habitId: Long): List<HabitEntry> =
        habitEntryDao.getEntriesForHabitSync(habitId)

    suspend fun getEntryForDate(habitId: Long, date: Date): HabitEntry? =
        habitEntryDao.getEntryForDate(habitId, date)

    suspend fun insertOrUpdateEntry(entry: HabitEntry) =
        habitEntryDao.insertEntry(entry)

    suspend fun toggleHabitForDate(habitId: Long, date: Date) {
        val existingEntry = habitEntryDao.getEntryForDate(habitId, date)
        if (existingEntry != null) {
            val updatedEntry = existingEntry.copy(
                isCompleted = !existingEntry.isCompleted,
                completedAt = if (!existingEntry.isCompleted) Date() else null
            )
            habitEntryDao.updateEntry(updatedEntry)
        } else {
            val newEntry = HabitEntry(
                habitId = habitId,
                date = date,
                isCompleted = true,
                completedAt = Date()
            )
            habitEntryDao.insertEntry(newEntry)
        }
    }

    fun getEntriesForHabit(habitId: Long): LiveData<List<HabitEntry>> =
        habitEntryDao.getEntriesForHabit(habitId)


    suspend fun getEntriesInDateRange(habitId: Long, startDate: Date, endDate: Date): List<HabitEntry> =
        habitEntryDao.getEntriesInDateRange(habitId, startDate, endDate)
}
