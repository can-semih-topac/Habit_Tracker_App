package com.cantopac.habittracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.cantopac.habittracker.databinding.ActivityMainBinding
import com.cantopac.habittracker.ui.HabitDetailActivity
import com.cantopac.habittracker.ui.adapters.HabitAdapter
import com.cantopac.habittracker.viewmodel.HabitViewModel
import com.cantopac.habittracker.utils.DateUtils
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var habitAdapter: HabitAdapter
    private val viewModel: HabitViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupFab()
        observeHabits()
    }

    private fun setupRecyclerView() {
        habitAdapter = HabitAdapter(
            onHabitClick = { habit ->
                // Habit detayları sayfasına git
                val intent = Intent(this, HabitDetailActivity::class.java)
                intent.putExtra("HABIT_ID", habit.id)
                startActivity(intent)
            },
            onToggleClick = { habit ->
                viewModel.toggleHabitForToday(habit.id)
            },
            onLongClick = { habit ->
                // Habit düzenleme/silme menüsü (sonraki adımda ekleyeceğiz)
            }
        )

        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = habitAdapter
        }
    }

    private fun setupFab() {
        binding.fabAddHabit.setOnClickListener {
            showAddHabitDialog()
        }
    }

    private fun observeHabits() {
        viewModel.habitsWithStatus.observe(this) { habitsWithStatus ->
            habitAdapter.submitList(habitsWithStatus)
        }
    }

    private fun showAddHabitDialog() {
        // Basit bir dialog ile yeni habit ekleme
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        val input = android.widget.EditText(this)
        input.hint = "Alışkanlık adı"

        builder.setTitle("Yeni Alışkanlık")
            .setView(input)
            .setPositiveButton("Ekle") { _, _ ->
                val habitName = input.text.toString().trim()
                if (habitName.isNotEmpty()) {
                    val newHabit = com.cantopac.habittracker.data.entities.Habit(
                        name = habitName,
                        color = getRandomColor()
                    )
                    viewModel.insertHabit(newHabit)
                }
            }
            .setNegativeButton("İptal", null)
            .show()
    }

    private fun getRandomColor(): Int {
        val colors = arrayOf(
            0xFF2196F3.toInt(), // Mavi
            0xFF4CAF50.toInt(), // Yeşil
            0xFFFF9800.toInt(), // Turuncu
            0xFFE91E63.toInt(), // Pembe
            0xFF9C27B0.toInt(), // Mor
            0xFF00BCD4.toInt(), // Cyan
            0xFFFF5722.toInt()  // Kırmızı
        )
        return colors.random()
    }
}