package com.unibo.android.ui.customs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class StatusCircleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLUE
        style = Paint.Style.FILL
    }

    //viene chiamato ogni volta che lo schermo deve ridisegnarsi
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Calcoliamo il centro della View
        val centerX = width / 2f
        val centerY = height / 2f
        val radius = Math.min(width, height) / 4f

        // Disegniamo fisicamente un cerchio sul "Canvas"
        canvas.drawCircle(centerX, centerY, radius, paint)
    }

    // Metodo per cambiare il colore a runtime (es. da Verde a Rosso)
    fun setStatusColor(newColor: Int) {
        paint.color = newColor
        invalidate() // ridisegna
    }
}