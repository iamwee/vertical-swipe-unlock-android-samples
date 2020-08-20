package com.iamwee.android.swipeviewsample

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View

class SwipeUnlockVerticalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "SwipeUnlockVerticalView"
    }

    private var desiredWidthDp = 65f
    private var desiredHeightDp = 140f
    private var desiredWidth: Int = 0
    private var desiredHeight: Int = 0

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    private val paint2 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.FILL
    }

    private val paint3 = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        shader = LinearGradient(
            0f,
            0f,
            0f,
            500f,
            intArrayOf(Color.TRANSPARENT, Color.GRAY, Color.BLACK),
            floatArrayOf(0.0f, 0.55f, 0.75f),
            Shader.TileMode.CLAMP
        )
    }

    private var surfaceWidth = 0
    private var surfaceHeight = 0

    private var circleMargin = 0
    private var circleSize = 0
    private var _currentCircleY = 0f

    private var isPressing: Boolean = false

    init {
        desiredWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            desiredWidthDp,
            resources.displayMetrics
        ).toInt()

        desiredHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            desiredHeightDp,
            resources.displayMetrics
        ).toInt()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = surfaceWidth
        val height = surfaceHeight

        if (circleSize == 0) {
            circleSize = if (width < height) width else height
        }
        if (_currentCircleY == 0f) {
            _currentCircleY = height - (circleSize.toFloat() / 2)
        }

        if (isPressing) {
            canvas.drawRoundRect(
                0f,
                _currentCircleY - (circleSize.toFloat() / 2),
                width.toFloat(),
                height.toFloat(),
                circleSize.toFloat(),
                circleSize.toFloat(),
                paint2
            )
        } else {
            canvas.drawRoundRect(
                0f,
                0f,
                width.toFloat(),
                height.toFloat(),
                circleSize.toFloat(),
                circleSize.toFloat(),
                paint3
            )
        }

        if (!isPressing) {
            canvas.drawRect(
                ((width / 2) - 20).toFloat(),
                _currentCircleY - (circleSize.toFloat() / 2) - 50,
                ((width / 2) + 20).toFloat(), (_currentCircleY - (circleSize.toFloat() / 2)) - 30, paint
            )
        }

        canvas.drawCircle(
            circleSize.toFloat() / 2, _currentCircleY,
            (circleSize.toFloat() / 2) - circleMargin,
            paint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                Log.d(TAG, "ACTION_DOWN[x=${event.x}, y=${event.y}]")
                isPressing = true
                //setup position value
                invalidate()
            }
            MotionEvent.ACTION_MOVE -> {
                val height = surfaceHeight
                Log.d(TAG, "ACTION_MOVE[x=${event.x}, y=${event.y}]")
                //Get diff of y between currentY and eventY
                val diffY = event.y - _currentCircleY

                //Check if diff value is negative or positive, if positive then increment value, else decrement value

                //coerce the position value

                //invalidate view
                _currentCircleY = event.y
                _currentCircleY = event.y.coerceIn(
                    circleSize.toFloat() / 2,
                    (height - (circleSize / 2)).toFloat()
                )
                invalidate()
            }
            MotionEvent.ACTION_UP -> {
                val height = surfaceHeight
                Log.d(TAG, "ACTION_UP[x=${event.x}, y=${event.y}]")
                val eventY = event.y.coerceAtLeast(0f)
                val percent = ((height - eventY) * 100 / height)
                _currentCircleY = if (percent <= 50) {
                    height - (circleSize / 2).toFloat()
                } else {
                    circleSize.toFloat() / 2
                }
                isPressing = false
                invalidate()
            }
            else -> return false
        }
        return true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val height: Int

        height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightSize)
            MeasureSpec.UNSPECIFIED -> desiredHeight
            else -> desiredHeight
        }
        setMeasuredDimension(desiredWidth, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        surfaceWidth = w
        surfaceHeight = h
    }


}