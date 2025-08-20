package com.cantopac.habittracker.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.cantopac.habittracker.R
import com.cantopac.habittracker.utils.ScoreCalculator

class SimpleChartView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var dataPoints: List<ScoreCalculator.ScorePoint> = emptyList()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    init {
        paint.color = context.getColor(R.color.stat_primary)
        paint.strokeWidth = 4f
        paint.style = Paint.Style.STROKE

        gridPaint.color = Color.LTGRAY
        gridPaint.strokeWidth = 1f
        gridPaint.style = Paint.Style.STROKE

        textPaint.color = Color.GRAY
        textPaint.textSize = 24f
        textPaint.textAlign = Paint.Align.CENTER
    }

    fun setData(points: List<ScoreCalculator.ScorePoint>) {
        dataPoints = points
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataPoints.isEmpty()) return

        val padding = 50f
        val chartWidth = width - 2 * padding
        val chartHeight = height - 2 * padding

        // Grid çizgileri
        drawGrid(canvas, padding, chartWidth, chartHeight)

        // Veri noktalarını çiz
        drawDataPoints(canvas, padding, chartWidth, chartHeight)
    }

    private fun drawGrid(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        // Yatay grid çizgileri (0, 25, 50, 75, 100 için)
        for (i in 0..4) {
            val y = padding + (chartHeight * i / 4)
            canvas.drawLine(padding, y, padding + chartWidth, y, gridPaint)

            // Y ekseni etiketleri
            val value = (100 - i * 25).toString()
            canvas.drawText(value, padding / 2, y + 8, textPaint)
        }
    }

    private fun drawDataPoints(canvas: Canvas, padding: Float, chartWidth: Float, chartHeight: Float) {
        if (dataPoints.size < 2) return

        val path = Path()
        val maxScore = 100f
        val minScore = 0f
        val labelPaint = Paint(textPaint).apply {
            textSize = 20f
            color = Color.DKGRAY
        }

        for (i in dataPoints.indices) {
            val point = dataPoints[i]
            val x = padding + (chartWidth * i / (dataPoints.size - 1))
            val y = padding + chartHeight - (chartHeight * (point.score - minScore) / (maxScore - minScore))

            if (i == 0) {
                path.moveTo(x, y)
            } else {
                path.lineTo(x, y)
            }

            // Dış daire (vurgu için)
            val circlePaint = Paint(paint).apply {
                style = Paint.Style.FILL
                color = Color.WHITE
            }
            canvas.drawCircle(x, y, 8f, circlePaint)

            // İç daire
            canvas.drawCircle(x, y, 5f, paint)

            // Etiket (örnek: "Hafta 1", "Hafta 2" vs.)
            val label = "H${i + 1}"
            canvas.drawText(label, x, height - 10f, labelPaint)
        }

        // Çizgiyi çiz
        canvas.drawPath(path, paint)
    }
}
