package com.cantopac.habittracker.ui.adapters

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.cantopac.habittracker.R
import com.cantopac.habittracker.data.entities.Habit
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class HabitAdapter(
    private val onHabitClick: (Habit) -> Unit,
    private val onToggleClick: (Habit) -> Unit,
    private val onLongClick: (Habit) -> Unit
) : ListAdapter<HabitAdapter.HabitWithStatus, HabitAdapter.HabitViewHolder>(HabitDiffCallback()) {

    data class HabitWithStatus(
        val habit: Habit,
        val isCompletedToday: Boolean,
        val currentStreak: Int
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        // XML dosyanızın adı item_habit.xml ise
        val view = inflater.inflate(R.layout.item_habit, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val cardView: MaterialCardView = itemView.findViewById(R.id.cardView)
        private val tvHabitName: TextView = itemView.findViewById(R.id.tvHabitName)
        private val tvHabitStreak: TextView = itemView.findViewById(R.id.tvHabitStreak)
        private val btnToggle: MaterialButton = itemView.findViewById(R.id.btnToggle)

        fun bind(habitWithStatus: HabitWithStatus) {
            val habit = habitWithStatus.habit
            val context = itemView.context // Context'i almak için

            tvHabitName.text = habit.name
            tvHabitStreak.text = itemView.context.getString(R.string.habit_streak_format, habitWithStatus.currentStreak)
            // Yukarıdaki satır için strings.xml içinde tanımlama yapmanız gerekir:
            // <string name="habit_streak_format">Seri: %d gün</string>

            // Toggle button durumunu ayarla
            if (habitWithStatus.isCompletedToday) {
                btnToggle.text = "✓" // Veya bir ikon kullanabilirsiniz
                // MaterialButton için backgroundTint kullanmak daha iyidir
                btnToggle.backgroundTintList = ColorStateList.valueOf(habit.color)
                btnToggle.icon = null // Eğer ikon kullanıyorsanız ve metinle değişiyorsa
            } else {
                btnToggle.text = "○" // Veya bir ikon kullanabilirsiniz
                // Renkleri resources'tan almak daha iyi bir pratiktir
                val defaultColor = ContextCompat.getColor(context, R.color.default_toggle_button_color) // R.color.default_toggle_button_color'ı colors.xml'de tanımlayın (örn: #9E9E9E)
                btnToggle.backgroundTintList = ColorStateList.valueOf(defaultColor)
                btnToggle.icon = null
            }

            // Card rengi
            // Renk şeffaflığı için (habit.color and 0x00FFFFFF) or 0x20000000 gibi bir yapı yerine
            // Color.argb kullanarak veya resources'tan yarı şeffaf bir renk alarak yapabilirsiniz.
            // Bu örnekte orijinal mantığınızı koruyorum ama daha okunabilir bir hale getirebiliriz.
            val cardBackgroundColor = if (habitWithStatus.isCompletedToday) {
                // habit.color'ın %20 alpha ile versiyonu (0x33 hexadecimal'de ~%20 alpha)
                // Orijinal mantık: (habit.color and 0x00FFFFFF) or 0x20000000 (bu yaklaşık %12.5 alpha - 32/255)
                // Daha anlaşılır bir alpha uygulaması:
                val baseColor = habit.color
                Color.argb(50, Color.red(baseColor), Color.green(baseColor), Color.blue(baseColor)) // Alpha değeri 0-255 arası (50 ~ %20)
            } else {
                ContextCompat.getColor(context, R.color.default_card_background_color) // R.color.default_card_background_color'ı colors.xml'de tanımlayın (örn: #FFFFFF)
            }
            cardView.setCardBackgroundColor(cardBackgroundColor)


            // Click listeners - Bunlar zaten doğru ayarlanmış
            cardView.setOnClickListener { onHabitClick(habit) }
            btnToggle.setOnClickListener {
                // ÖNEMLİ: "Tekleme" sorununu çözmek için onToggleClick lambda'sının
                // ViewModel'de UI'ı bloke etmeyen bir şekilde çalıştığından emin olun
                // ve habitWithStatus.isCompletedToday değerinin doğru ve zamanında güncellendiğinden
                // emin olun ki bir sonraki bind işleminde buton doğru görünsün.
                onToggleClick(habit)
            }
            cardView.setOnLongClickListener {
                onLongClick(habit)
                true // Olayın tüketildiğini belirtmek için true döndürmek önemlidir
            }
        }
    }

    class HabitDiffCallback : DiffUtil.ItemCallback<HabitWithStatus>() {
        override fun areItemsTheSame(oldItem: HabitWithStatus, newItem: HabitWithStatus): Boolean {
            // Sadece ID'ye bakmak genellikle yeterlidir
            return oldItem.habit.id == newItem.habit.id
        }

        override fun areContentsTheSame(oldItem: HabitWithStatus, newItem: HabitWithStatus): Boolean {
            // Tüm alanları karşılaştırır. HabitWithStatus bir data class olduğu için
            // bu zaten doğru çalışacaktır.
            return oldItem == newItem
        }
    }
}
