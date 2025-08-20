package com.cantopac.habittracker.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.cantopac.habittracker.R
import com.cantopac.habittracker.data.entities.HabitEntry
import com.cantopac.habittracker.utils.DateUtils
import java.util.*

class CalendarAdapter(
    private val context: Context,
    private val onDateClick: (Date) -> Unit
) : BaseAdapter() {

    private var calendarDays: List<CalendarDay> = emptyList()
    private var habitEntries: List<HabitEntry> = emptyList()
    private var habitColor: Int = 0xFF2196F3.toInt()

    data class CalendarDay(
        val date: Date?,
        val dayNumber: Int,
        val isCurrentMonth: Boolean,
        val isToday: Boolean
    )

    fun updateData(days: List<CalendarDay>, entries: List<HabitEntry>, color: Int) {
        calendarDays = days
        habitEntries = entries
        habitColor = color
        notifyDataSetChanged()
    }

    override fun getCount(): Int = calendarDays.size

    override fun getItem(position: Int): CalendarDay = calendarDays[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: LayoutInflater.from(context)
            .inflate(R.layout.item_calendar_day, parent, false)

        val dayItem = calendarDays[position]
        val tvDay = view.findViewById<TextView>(R.id.tvDay)

        tvDay.text = if (dayItem.dayNumber > 0) dayItem.dayNumber.toString() else ""

        // Günün durumunu belirle
        val dayStatus = getDayStatus(dayItem)

        // Görünümü güncelle
        updateDayAppearance(tvDay, dayItem, dayStatus)

        // Click listener
        if (dayItem.date != null && dayItem.isCurrentMonth) {
            view.setOnClickListener { onDateClick(dayItem.date) }
        } else {
            view.setOnClickListener(null)
        }

        return view
    }

    private fun getDayStatus(dayItem: CalendarDay): DayStatus {
        if (dayItem.date == null || !dayItem.isCurrentMonth) {
            return DayStatus.EMPTY
        }

        val entry = habitEntries.find {
            DateUtils.isSameDay(it.date, dayItem.date)
        }

        return when {
            dayItem.isToday -> DayStatus.TODAY
            entry?.isCompleted == true -> DayStatus.COMPLETED
            entry != null -> DayStatus.MISSED
            else -> DayStatus.NEUTRAL
        }
    }

    private fun updateDayAppearance(textView: TextView, dayItem: CalendarDay, status: DayStatus) {
        when (status) {
            DayStatus.COMPLETED -> {
                textView.setBackgroundResource(R.drawable.calendar_day_completed)
                textView.setTextColor(context.getColor(android.R.color.white))
                // Habit rengini uygula
                textView.background.setTint(habitColor)
            }
            DayStatus.MISSED -> {
                textView.setBackgroundResource(R.drawable.calendar_day_missed)
                textView.setTextColor(context.getColor(android.R.color.white))
            }
            DayStatus.TODAY -> {
                textView.setBackgroundResource(R.drawable.calendar_day_today)
                textView.setTextColor(context.getColor(android.R.color.white))
            }
            DayStatus.NEUTRAL -> {
                textView.setBackgroundResource(R.drawable.calendar_day_neutral)
                textView.setTextColor(context.getColor(android.R.color.black))
            }
            DayStatus.EMPTY -> {
                textView.background = null
                textView.setTextColor(context.getColor(android.R.color.transparent))
            }
        }

        // Geçmiş ay/gelecek ay günleri için opacity azalt
        if (!dayItem.isCurrentMonth && dayItem.dayNumber > 0) {
            textView.alpha = 0.3f
        } else {
            textView.alpha = 1.0f
        }
    }

    enum class DayStatus {
        COMPLETED, MISSED, TODAY, NEUTRAL, EMPTY
    }
}