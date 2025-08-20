package com.cantopac.habittracker.data.dao

import androidx.room.*

import androidx.lifecycle.LiveData // getAllActiveHabits için
import com.cantopac.habittracker.data.entities.Habit // Entity importu

@Dao
interface HabitDao {
    @Query("SELECT * FROM habits_table WHERE isActive = 1 ORDER BY createdAt DESC")
    fun getAllActiveHabits(): LiveData<List<Habit>> // Doğru

    @Query("SELECT * FROM habits_table WHERE id = :id")
    suspend fun getHabitById(id: Long): Habit? // DOĞRU: Entity tipi veya null

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHabit(habit: Habit): Long // DOĞRU: Eklenen ID'yi döndürür (veya void/Unit için sadece `suspend fun insertHabit(habit: Habit)`)

    @Update
    suspend fun updateHabit(habit: Habit) // DOĞRU: Geri dönüş tipi Unit (void)

    @Delete
    suspend fun deleteHabit(habit: Habit) // DOĞRU: Geri dönüş tipi Unit (void)

    @Query("UPDATE habits_table SET isActive = 0 WHERE id = :id")
    suspend fun deactivateHabit(id: Long) // DOĞRU: Geri dönüş tipi Unit (void) (veya Int dönebilir)
}