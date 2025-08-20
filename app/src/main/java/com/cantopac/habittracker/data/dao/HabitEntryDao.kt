package com.cantopac.habittracker.data.dao

import androidx.room.*
import androidx.lifecycle.LiveData
import com.cantopac.habittracker.data.entities.HabitEntry
import java.util.Date

@Dao
interface HabitEntryDao {
    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date DESC")
    fun getEntriesForHabit(habitId: Long): LiveData<List<HabitEntry>>

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date = :date")
    suspend fun getEntryForDate(habitId: Long, date: Date): HabitEntry?

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId AND date BETWEEN :startDate AND :endDate")
    suspend fun getEntriesInDateRange(habitId: Long, startDate: Date, endDate: Date): List<HabitEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: HabitEntry)

    @Update
    suspend fun updateEntry(entry: HabitEntry)

    @Delete
    suspend fun deleteEntry(entry: HabitEntry)

    @Query("DELETE FROM habit_entries WHERE habitId = :habitId")
    suspend fun deleteAllEntriesForHabit(habitId: Long)

    @Query("SELECT * FROM habit_entries WHERE habitId = :habitId ORDER BY date DESC")
    suspend fun getEntriesForHabitSync(habitId: Long): List<HabitEntry>
}