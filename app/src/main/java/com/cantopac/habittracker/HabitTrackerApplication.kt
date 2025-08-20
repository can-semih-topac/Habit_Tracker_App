package com.cantopac.habittracker

import android.app.Application
import com.cantopac.habittracker.data.database.HabitDatabase

class HabitTrackerApplication : Application() {
    val database by lazy { HabitDatabase.getDatabase(this) }
}