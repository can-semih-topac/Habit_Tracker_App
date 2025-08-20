package com.cantopac.habittracker.ui

import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.cantopac.habittracker.R // Kendi R dosyanızı import edin
import com.cantopac.habittracker.data.entities.Habit
import com.cantopac.habittracker.data.entities.HabitEntry
import com.cantopac.habittracker.databinding.ActivityHabitDetailBinding
import com.cantopac.habittracker.utils.DateUtils
import com.cantopac.habittracker.utils.ScoreCalculator
import com.cantopac.habittracker.viewmodel.HabitViewModel
import com.prolificinteractive.materialcalendarview.CalendarDay
import com.prolificinteractive.materialcalendarview.MaterialCalendarView
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat // SimpleDateFormat import edildi
import java.util.*

class HabitDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHabitDetailBinding
    private val viewModel: HabitViewModel by viewModels()
    private var habitId: Long = -1
    private var currentHabit: Habit? = null
    private var habitEntries: List<HabitEntry> = emptyList()

    // CalendarAdapter ve ilgili değişkenler kaldırıldı, MaterialCalendarView kendi adaptörünü kullanır.
    // private lateinit var calendarAdapter: CalendarAdapter
    // private var currentCalendarMonth = Calendar.getInstance().get(Calendar.MONTH)
    // private var currentCalendarYear = Calendar.getInstance().get(Calendar.YEAR)

    private lateinit var calendarView: MaterialCalendarView
    private lateinit var tvMonthYear: TextView
    private lateinit var btnPrevMonth: ImageButton
    private lateinit var btnNextMonth: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHabitDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // İkinci setContentView çağrısı gereksizdir ve kaldırılmalıdır. ViewBinding zaten root view'ı ayarlar.
        // setContentView(R.layout.activity_habit_detail)

        // View'ları binding üzerinden initialize edin
        calendarView = binding.calendarView // XML'deki id ile eşleşmeli
        tvMonthYear = binding.tvMonthYear     // XML'deki id ile eşleşmeli
        btnPrevMonth = binding.btnPrevMonth   // XML'deki id ile eşleşmeli
        btnNextMonth = binding.btnNextMonth   // XML'deki id ile eşleşmeli

        habitId = intent.getLongExtra("HABIT_ID", -1)
        if (habitId == -1L) {
            finish()
            return
        }

        setupToolbar()
        loadHabitDetails()
        observeHabitEntries()
        setupCalendar() // setupCalendar çağrısı burada olmalı

        // Buton tıklama olayları
        btnPrevMonth.setOnClickListener {
            calendarView.goToPrevious()
        }

        btnNextMonth.setOnClickListener {
            calendarView.goToNext()
        }

        // Ay değiştiğinde başlığı güncelle
        calendarView.setOnMonthChangedListener { widget, date ->
            updateMonthYearText(date)
        }
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun loadHabitDetails() {
        lifecycleScope.launch {
            currentHabit = viewModel.getHabitById(habitId)
            currentHabit?.let { habit ->
                supportActionBar?.title = habit.name
                updateHabitStats() // Alışkanlık detayları yüklendikten sonra istatistikleri güncelle
            }
        }
    }

    private fun observeHabitEntries() {
        viewModel.getEntriesForHabit(habitId).observe(this) { entries ->
            habitEntries = entries
            updateHabitStats() // Girişler güncellendiğinde istatistikleri ve takvimi güncelle
            // MaterialCalendarView'da günleri işaretlemek için burada bir metod çağırabilirsiniz.
            // Örneğin: markCalendarDays(entries)
        }
    }

    private fun setupCalendar() {
        // Başlangıç ayını ve yılını ayarla
        val today = CalendarDay.today()
        calendarView.setCurrentDate(today)
        calendarView.setSelectedDate(today) // İsteğe bağlı: bugünü seçili yap

        updateMonthYearText(today)

        // Takvim için diğer özelleştirmeler (isteğe bağlı)
        calendarView.setTopbarVisible(false) // Kendi başlığınızı kullanıyorsanız
        // calendarView.state().edit()
        //     .setFirstDayOfWeek(Calendar.MONDAY) // Haftanın ilk gününü Pazartesi yap
        //     .commit()

        // Eski calendarGrid ve adapter ile ilgili kodlar kaldırıldı.
        // binding.calendarGrid.adapter = calendarAdapter
        // updateCalendarView() // Artık MaterialCalendarView kendi görünümünü yönetiyor.
    }

    private fun updateMonthYearText(date: CalendarDay) {
        val monthFormat = SimpleDateFormat("MMMM", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        val monthName = monthFormat.format(date.calendar.time)
        val year = yearFormat.format(date.calendar.time)
        tvMonthYear.text = "$monthName $year"
    }

    // navigateMonth ve updateCalendarView fonksiyonları MaterialCalendarView ile gereksiz hale geldi.
    // MaterialCalendarView kendi navigasyonunu ve güncellemesini yönetir.
    /*
    private fun navigateMonth(direction: Int) {
        // ... (Bu fonksiyon artık kullanılmıyor)
    }

    private fun updateCalendarView() {
        // ... (Bu fonksiyon artık kullanılmıyor, MaterialCalendarView güncellemeleri kendi içinde yapar)
        // Ancak, alışkanlık girişlerine göre günleri işaretlemek/renklendirmek için
        // burada MaterialCalendarView'a özel decorator'lar ekleyebilirsiniz.
        // Örneğin:
        // val decorators = mutableListOf<DayViewDecorator>()
        // habitEntries.forEach { entry ->
        //    if (entry.isCompleted) {
        //        val day = CalendarDay.from(entry.date.year, entry.date.monthValue -1, entry.date.dayOfMonth) // entry.date LocalDate ise
        //        decorators.add(EventDecorator(Color.GREEN, setOf(day)))
        //    }
        // }
        // calendarView.addDecorators(decorators)
    }
    */

    private fun updateHabitStats() {
        val habit = currentHabit ?: return

        lifecycleScope.launch {
            // Temel istatistikler
            val currentScore = ScoreCalculator.calculateCurrentScore(habit, habitEntries)
            val completionRate = ScoreCalculator.calculateCompletionRate(habit, habitEntries)
            val totalDays = getTotalDaysSinceStart(habit)
            // val completedDays = getCompletedDaysCount() // Bu değişken kullanılmıyor gibi görünüyor.
            val currentStreak = viewModel.getHabitStreak(habitId) // viewModel'den alınmalı

            // Değişim oranları
            val monthlyChange = ScoreCalculator.calculateScoreChange(habit, habitEntries, 30)
            val yearlyChange = ScoreCalculator.calculateScoreChange(habit, habitEntries, 365)

            // Grafik verilerini güncelle
            val scoreHistory = ScoreCalculator.calculateScoreHistory(habit, habitEntries, 30)

            runOnUiThread {
                binding.apply {
                    // Ana istatistikler
                    tvCurrentScore.text = "$currentScore%"
                    tvCompletionRate.text = "$completionRate%"
                    tvTotalDays.text = "$totalDays"
                    tvCurrentStreak.text = "$currentStreak gün"

                    // Progress bar
                    progressBar.progress = currentScore

                    // Değişim oranları
                    tvMonthlyChange.text = formatChangeValue(monthlyChange)
                    tvYearlyChange.text = formatChangeValue(yearlyChange)

                    // Renk ayarları
                    updateChangeColors(monthlyChange, tvMonthlyChange)
                    updateChangeColors(yearlyChange, tvYearlyChange)

                    // Grafik güncelle
                    // chartView.setData(scoreHistory) // chartView binding'de tanımlı olmalı
                }

                // Takvimi güncelleme (günleri işaretleme vb.) MaterialCalendarView decorator'ları ile yapılabilir.
                // updateCalendarView() // Bu fonksiyonun içeriği değişmeli veya kaldırılmalı.
            }
        }
    }

    private fun getTotalDaysSinceStart(habit: Habit): Int {
        val startDate = habit.createdAt // createdAt Date tipinde olmalı
        val today = DateUtils.getToday() // DateUtils.getToday() Date döndürmeli
        val diffInMillies = today.time - startDate.time
        return (diffInMillies / (1000 * 60 * 60 * 24)).toInt() + 1
    }

    private fun getCompletedDaysCount(): Int {
        return habitEntries.count { it.isCompleted }
    }

    private fun formatChangeValue(change: Int): String {
        return if (change > 0) "+$change%" else "$change%"
    }

    private fun updateChangeColors(change: Int, textView: TextView) { // Parametre tipi TextView olarak düzeltildi
        val color = when {
            change > 0 -> getColor(com.cantopac.habittracker.R.color.stat_positive)
            change < 0 -> getColor(com.cantopac.habittracker.R.color.stat_negative)
            else -> getColor(com.cantopac.habittracker.R.color.stat_primary)
        }
        textView.setTextColor(color)
    }

    // MaterialCalendarView için günleri işaretlemek üzere örnek bir decorator sınıfı
    // (İhtiyaca göre özelleştirilebilir)
    /*
    import android.graphics.Color
    import com.prolificinteractive.materialcalendarview.DayViewDecorator
    import com.prolificinteractive.materialcalendarview.DayViewFacade
    import com.prolificinteractive.materialcalendarview.spans.DotSpan

    class EventDecorator(private val color: Int, private val dates: Collection<CalendarDay>) : DayViewDecorator {
        override fun shouldDecorate(day: CalendarDay): Boolean {
            return dates.contains(day)
        }

        override fun decorate(view: DayViewFacade) {
            view.addSpan(DotSpan(5f, color)) // Veya farklı bir işaretleme
        }
    }
    */
}
