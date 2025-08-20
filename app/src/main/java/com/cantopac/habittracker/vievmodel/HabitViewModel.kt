package com.cantopac.habittracker.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData // zaten olduğu için eklemedim
import androidx.lifecycle.viewModelScope
import com.cantopac.habittracker.data.database.HabitDatabase
import com.cantopac.habittracker.data.entities.Habit
import com.cantopac.habittracker.data.entities.HabitEntry
import com.cantopac.habittracker.data.repository.HabitRepository
import com.cantopac.habittracker.utils.DateUtils
import com.cantopac.habittracker.utils.ScoreCalculator
import kotlinx.coroutines.launch
import java.util.*
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import com.cantopac.habittracker.ui.adapters.HabitAdapter

class HabitViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: HabitRepository
    val allHabits: LiveData<List<Habit>>

    private val _habitsWithStatus = MediatorLiveData<List<HabitAdapter.HabitWithStatus>>()
    val habitsWithStatus: LiveData<List<HabitAdapter.HabitWithStatus>> = _habitsWithStatus

    init {
        val database = HabitDatabase.getDatabase(application)
        repository = HabitRepository(database.habitDao(), database.habitEntryDao())
        allHabits = repository.getAllHabits()
        _habitsWithStatus.addSource(allHabits) {
            refreshHabitsWithStatus()
        }
    }

    fun insertHabit(habit: Habit) {
        viewModelScope.launch {
            repository.insertHabit(habit)
        }
    }

    fun updateHabit(habit: Habit) {
        viewModelScope.launch {
            repository.updateHabit(habit)
        }
    }

    fun deleteHabit(habit: Habit) {
        viewModelScope.launch {
            repository.deleteHabit(habit)
        }
    }

    fun toggleHabitForToday(habitId: Long) { // anasayfa daki tuş ile günlük işaretleme yapan fonksiyon
        viewModelScope.launch {
            repository.toggleHabitForDate(habitId, DateUtils.getToday())
            refreshHabitsWithStatus() // listeyi güncelle
        }
    }

    fun toggleHabitForDate(habitId: Long, date: Date) {
        viewModelScope.launch {
            repository.toggleHabitForDate(habitId, date)
            refreshHabitsWithStatus() // listeyi güncelle (opsiyonel) (şüpheli)
        }
    }

    fun getEntriesForHabit(habitId: Long): LiveData<List<HabitEntry>> {
        return repository.getEntriesForHabit(habitId)
    }

    suspend fun isHabitCompletedForDate(habitId: Long, date: Date): Boolean {
        return repository.getEntryForDate(habitId, date)?.isCompleted ?: false
    }

    suspend fun getHabitStreak(habitId: Long): Int {
        val today = DateUtils.getToday()
        var streak = 0
        var currentDate = today

        for (i in 0 until 365) {
            val entry = repository.getEntryForDate(habitId, currentDate)
            if (entry?.isCompleted == true) {
                streak++
                currentDate = DateUtils.getDateDaysAgo(i + 1)
            } else {
                break
            }
        }
        return streak
    }

    suspend fun getHabitById(habitId: Long): Habit? {
        return repository.getHabitById(habitId)
    }

    suspend fun getHabitScore(habitId: Long): Double {
        val habit = repository.getHabitById(habitId) ?: return 0.0
        val entries = repository.getEntriesForHabitSync(habitId)
        return ScoreCalculator.calculateCurrentScore(habit, entries).toDouble()


    }

    suspend fun getHabitCompletionRate(habitId: Long): Int {
        val habit = repository.getHabitById(habitId) ?: return 0
        val entries = repository.getEntriesForHabitSync(habitId)
        return ScoreCalculator.calculateCompletionRate(habit, entries)

    }

    suspend fun getHabitMonthlyChange(habitId: Long): Double {
        val habit = repository.getHabitById(habitId) ?: return 0.0
        val entries = repository.getEntriesForHabitSync(habitId)
        return ScoreCalculator.calculateScoreChange(habit, entries, 30).toDouble()
    }

    suspend fun getHabitYearlyChange(habitId: Long): Double {
        val habit = repository.getHabitById(habitId) ?: return 0.0
        val entries = repository.getEntriesForHabitSync(habitId)
        return ScoreCalculator.calculateScoreChange(habit, entries, 365).toDouble()
    }

    suspend fun getTotalDaysSinceStart(habitId: Long): Int {
        val habit = repository.getHabitById(habitId) ?: return 0
        val today = DateUtils.getToday()
        val diffInMillis = today.time - habit.createdAt.time
        return (diffInMillis / (1000 * 60 * 60 * 24)).toInt() + 1
    }

    suspend fun getCompletedDaysCount(habitId: Long): Int {
        val entries = repository.getEntriesForHabitSync(habitId)
        return entries.count { it.isCompleted }
    }

    suspend fun getWeeklyScoreData(habitId: Long): List<ScoreCalculator.ScorePoint> {
        val habit = repository.getHabitById(habitId) ?: return emptyList()
        val entries = repository.getEntriesForHabitSync(habitId)
        return ScoreCalculator.getWeeklyScoreData(habit, entries)
    }

    suspend fun getMonthlyScoreData(habitId: Long): List<ScoreCalculator.ScorePoint> {
        val habit = repository.getHabitById(habitId) ?: return emptyList()
        val entries = repository.getEntriesForHabitSync(habitId)
        return ScoreCalculator.getMonthlyScoreData(habit, entries)
    }

    suspend fun getEntriesInDateRange(habitId: Long, startDate: Date, endDate: Date): List<HabitEntry> {
        return repository.getEntriesInDateRange(habitId, startDate, endDate)
    }

    suspend fun getScoreHistory(habitId: Long, days: Int): List<ScoreCalculator.ScorePoint> {
        val habit = repository.getHabitById(habitId) ?: return emptyList()
        val entries = repository.getEntriesInDateRange(
            habitId,
            DateUtils.getDateDaysAgo(days),
            DateUtils.getToday()
        )
        return ScoreCalculator.calculateScoreHistory(habit, entries, days)
    }

    suspend fun getCurrentScore(habitId: Long): Int {
        val habit = repository.getHabitById(habitId) ?: return 0
        val entries = repository.getEntriesForHabitSync(habitId)
        return ScoreCalculator.calculateCurrentScore(habit, entries)
    }
    private fun refreshHabitsWithStatus() {
        viewModelScope.launch {
            val habits = allHabits.value ?: return@launch
            val updatedList = habits.map { habit ->
                val isCompletedToday = isHabitCompletedForDate(habit.id, DateUtils.getToday())
                val currentStreak = getHabitStreak(habit.id)

                HabitAdapter.HabitWithStatus(
                    habit = habit,
                    isCompletedToday = isCompletedToday,
                    currentStreak = currentStreak
                )
            }
            _habitsWithStatus.postValue(updatedList)
        }
    }

}
