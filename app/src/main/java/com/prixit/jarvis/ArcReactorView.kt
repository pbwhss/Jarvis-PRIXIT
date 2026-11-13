package com.prixit.jarvis

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import kotlin.math.min

class ArcReactorView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private var angleOuter = 0f
    private var angleInner = 360f
    private var pulseScale = 1.0f
    var isListeningMode = false
        set(value) {
            field = value
            invalidate()
        }

    private val outerRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
        color = Color.parseColor("#00F3FF")
        pathEffect = DashPathEffect(floatArrayOf(40f, 18f, 12f, 18f), 0f)
    }

    private val middleRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.parseColor("#0066FF")
        pathEffect = DashPathEffect(floatArrayOf(70f, 35f), 0f)
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val coreRingPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 4f
        color = Color.WHITE
    }

    init {
        val spinAnimator = ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 4000
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                val v = it.animatedValue as Float
                angleOuter = if (isListeningMode) v * 2.5f else v
                angleInner = 360f - (if (isListeningMode) v * 3f else v * 1.5f)
                invalidate()
            }
        }
        spinAnimator.start()

        val pulseAnimator = ValueAnimator.ofFloat(0.88f, 1.08f).apply {
            duration = 900
            repeatCount = ValueAnimator.INFINITE
            repeatMode = ValueAnimator.REVERSE
            addUpdateListener {
                pulseScale = it.animatedValue as Float
                invalidate()
            }
        }
        pulseAnimator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cx = width / 2f
        val cy = height / 2f
        val maxRadius = min(cx, cy) - 16f

        val activeColor = if (isListeningMode) Color.parseColor("#00FF9D") else Color.parseColor("#00F3FF")
        outerRingPaint.color = activeColor

        glowPaint.shader = RadialGradient(
            cx, cy, maxRadius * pulseScale,
            intArrayOf(
                if (isListeningMode) Color.argb(140, 0, 255, 157) else Color.argb(130, 0, 243, 255),
                Color.argb(40, 0, 102, 255),
                Color.TRANSPARENT
            ),
            floatArrayOf(0.2f, 0.65f, 1.0f),
            Shader.TileMode.CLAMP
        )
        canvas.drawCircle(cx, cy, maxRadius * pulseScale, glowPaint)

        canvas.save()
        canvas.rotate(angleOuter, cx, cy)
        canvas.drawCircle(cx, cy, maxRadius * 0.92f, outerRingPaint)
        canvas.restore()

        canvas.save()
        canvas.rotate(angleInner, cx, cy)
        canvas.drawCircle(cx, cy, maxRadius * 0.72f, middleRingPaint)
        canvas.restore()

        canvas.drawCircle(cx, cy, maxRadius * 0.42f * pulseScale, coreRingPaint)
    }
}
