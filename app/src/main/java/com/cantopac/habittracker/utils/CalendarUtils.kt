package com.cantopac.habittracker.utils

import com.cantopac.habittracker.ui.adapters.CalendarAdapter
import java.util.*

object CalendarUtils {

    fun generateCalendarDays(year: Int, month: Int): List<CalendarAdapter.CalendarDay> {
        val calendar = Calendar.getInstance()
        val today = DateUtils.getToday()
        val days = mutableListOf<CalendarAdapter.CalendarDay>()

        // Ayın ilk günü
        calendar.set(year, month, 1)
        val firstDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK) - 1 // 0-6 arası
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Önceki ayın son günleri (boşlukları doldurmak için)
        val prevMonth = if (month == 0) 11 else month - 1
        val prevYear = if (month == 0) year - 1 else year
        calendar.set(prevYear, prevMonth, 1)
        val daysInPrevMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        // Önceki ayın görünen günleri
        for (i in firstDayOfMonth - 1 downTo 0) {
            val day = daysInPrevMonth - i
            calendar.set(prevYear, prevMonth, day)
            days.add(
                CalendarAdapter.CalendarDay(
                    date = Date(calendar.timeInMillis),
                    dayNumber = day,
                    isCurrentMonth = false,
                    isToday = DateUtils.isSameDay(Date(calendar.timeInMillis), today)
                )
            )
        }

        // Bu ayın günleri
        for (day in 1..daysInMonth) {
            calendar.set(year, month, day)
            days.add(
                CalendarAdapter.CalendarDay(
                    date = Date(calendar.timeInMillis),
                    dayNumber = day,
                    isCurrentMonth = true,
                    isToday = DateUtils.isSameDay(Date(calendar.timeInMillis), today)
                )
            )
        }

        // Gelecek ayın ilk günleri (42 günü tamamlamak için - 6x7 grid)
        val remainingCells = 42 - days.size
        val nextMonth = if (month == 11) 0 else month + 1
        val nextYear = if (month == 11) year + 1 else year

        for (day in 1..remainingCells) {
            calendar.set(nextYear, nextMonth, day)
            days.add(
                CalendarAdapter.CalendarDay(
                    date = Date(calendar.timeInMillis),
                    dayNumber = day,
                    isCurrentMonth = false,
                    isToday = DateUtils.isSameDay(Date(calendar.timeInMillis), today)
                )
            )
        }

        return days
    }

    fun getMonthName(month: Int): String {
        val months = arrayOf(
            "Ocak", "Şubat", "Mart", "Nisan", "Mayıs", "Haziran",
            "Temmuz", "Ağustos", "Eylül", "Ekim", "Kasım", "Aralık"
        )
        return months[month]
    }

    fun getDayNames(): Array<String> {
        return arrayOf("Pzt", "Sal", "Çar", "Per", "Cum", "Cmt", "Paz")
    }
}