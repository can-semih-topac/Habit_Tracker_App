package com.cantopac.habittracker.utils

import com.cantopac.habittracker.data.entities.Habit
import com.cantopac.habittracker.data.entities.HabitEntry
import java.util.*

object ScoreCalculator {

    private const val INITIAL_SCORE = 10 // İlk gün %10
    private const val DAILY_INCREASE = 5 // Her gün %5 artış
    private const val DAILY_DECREASE = 6 // Kaçırılan gün %6 azalış
    private const val MIN_SCORE = 0
    private const val MAX_SCORE = 100

    /**
     * Habit için güncel skoru hesaplar
     */
    fun calculateCurrentScore(habit: Habit, entries: List<HabitEntry>): Int {
        if (entries.isEmpty()) return INITIAL_SCORE

        // Tarihe göre sıralı entries al
        val sortedEntries = entries.sortedBy { it.date }
        val habitStartDate = habit.createdAt

        var currentScore = INITIAL_SCORE
        val today = DateUtils.getToday()

        // Habit başlangıcından bugüne kadar her güne bak
        var currentDate = habitStartDate
        val calendar = Calendar.getInstance()

        while (currentDate.time <= today.time) {
            val entryForDate = sortedEntries.find {
                DateUtils.isSameDay(it.date, currentDate)
            }

            if (entryForDate?.isCompleted == true) {
                // Tamamlandıysa %5 artır
                currentScore += DAILY_INCREASE
            } else {
                // Kaçırıldıysa %6 azalt
                currentScore -= DAILY_DECREASE
            }

            // Sınırları kontrol et
            currentScore = currentScore.coerceIn(MIN_SCORE, MAX_SCORE)

            // Bir sonraki güne geç
            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            currentDate = calendar.time
        }

        return currentScore
    }

    /**
     * Belirli bir tarih aralığındaki skor değişimini hesaplar
     */
    fun calculateScoreChange(habit: Habit, entries: List<HabitEntry>, daysAgo: Int): Int {
        val today = DateUtils.getToday()
        val pastDate = DateUtils.getDateDaysAgo(daysAgo)

        val currentScore = calculateCurrentScore(habit, entries)
        val pastScore = calculateScoreAtDate(habit, entries, pastDate)

        return currentScore - pastScore
    }

    /**
     * Belirli bir tarihteki skoru hesaplar
     */
    private fun calculateScoreAtDate(habit: Habit, entries: List<HabitEntry>, targetDate: Date): Int {
        val sortedEntries = entries.sortedBy { it.date }
        val habitStartDate = habit.createdAt

        var currentScore = INITIAL_SCORE
        val calendar = Calendar.getInstance()

        // Habit başlangıcından hedef tarihe kadar hesapla
        var currentDate = habitStartDate

        while (currentDate.time <= targetDate.time) {
            val entryForDate = sortedEntries.find {
                DateUtils.isSameDay(it.date, currentDate)
            }

            if (entryForDate?.isCompleted == true) {
                currentScore += DAILY_INCREASE
            } else {
                currentScore -= DAILY_DECREASE
            }

            currentScore = currentScore.coerceIn(MIN_SCORE, MAX_SCORE)

            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            currentDate = calendar.time
        }

        return currentScore
    }

    /**
     * Günlük skor geçmişini hesaplar (grafik için)
     */
    fun calculateScoreHistory(habit: Habit, entries: List<HabitEntry>, days: Int): List<ScorePoint> {
        val scoreHistory = mutableListOf<ScorePoint>()
        val sortedEntries = entries.sortedBy { it.date }
        val habitStartDate = habit.createdAt

        var currentScore = INITIAL_SCORE
        val calendar = Calendar.getInstance()
        val today = DateUtils.getToday()
        val startDate = DateUtils.getDateDaysAgo(days)

        // Habit başlangıcından başlayarak skorları hesapla
        var currentDate = habitStartDate

        while (currentDate.time <= today.time) {
            val entryForDate = sortedEntries.find {
                DateUtils.isSameDay(it.date, currentDate)
            }

            if (entryForDate?.isCompleted == true) {
                currentScore += DAILY_INCREASE
            } else {
                currentScore -= DAILY_DECREASE
            }

            currentScore = currentScore.coerceIn(MIN_SCORE, MAX_SCORE)

            // Eğer bu tarih görüntülenecek aralıktaysa kaydet
            if (currentDate.time >= startDate.time) {
                scoreHistory.add(ScorePoint(Date(currentDate.time), currentScore))
            }

            calendar.time = currentDate
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            currentDate = calendar.time
        }

        return scoreHistory
    }

    /**
     * Tamamlama oranını hesaplar
     */
    fun calculateCompletionRate(habit: Habit, entries: List<HabitEntry>): Int {
        val habitStartDate = habit.createdAt
        val today = DateUtils.getToday()

        val totalDays = getDaysBetween(habitStartDate, today)
        if (totalDays == 0) return 0

        val completedDays = entries.count { it.isCompleted }

        return (completedDays * 100) / totalDays
    }

    /**
     * İki tarih arasındaki gün sayısını hesaplar
     */
    private fun getDaysBetween(startDate: Date, endDate: Date): Int {
        val diffInMillies = endDate.time - startDate.time
        return (diffInMillies / (1000 * 60 * 60 * 24)).toInt() + 1
    }

    fun getWeeklyScoreData(habit: Habit, entries: List<HabitEntry>): List<ScorePoint> {
        return calculateScoreHistory(habit, entries, 7)
    }

    fun getMonthlyScoreData(habit: Habit, entries: List<HabitEntry>): List<ScorePoint> {
        return calculateScoreHistory(habit, entries, 30)
    }

    data class ScorePoint(val date: Date, val score: Int)
}