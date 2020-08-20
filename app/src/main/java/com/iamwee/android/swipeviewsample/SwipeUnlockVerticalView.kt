package com.iamwee.android.swipeviewsample

import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

class SwipeUnlockVerticalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "SwipeUnlockVerticalView"
    }

    // surface declaration
    private var surfaceWidth = 0
    private var surfaceHeight = 0

    // desired size declaration
    private var desiredWidthDp = 65f
    private var desiredHeightDp = 140f
    private var desiredWidth: Int = 0
    private var desiredHeight: Int = 0

    // locker button margin declaration
    private var circleMarginDp = 0f
    private var circleMargin = 0

    private var circleSize = 0

    // drawable in normal state inside locker button
    private lateinit var unlockIconDrawable: Drawable
    private var unlockIcon: Int = R.drawable.ic_baseline_arrow_upward_24
        set(value) {
            field = value
            if (field != 0) {
                ResourcesCompat.getDrawable(context.resources, value, context.theme)?.let {
                    unlockIconDrawable = it
                    DrawableCompat.setTint(
                        it,
                        resources.getColor(android.R.color.white, context?.theme)
                    )
                }
            }
        }

    // drawable in pressed state inside locker button
    private lateinit var lockIconDrawable: Drawable
    private var lockIcon: Int = R.drawable.ic_baseline_expand_less_24
        set(value) {
            field = value
            if (field != 0) {
                ResourcesCompat.getDrawable(context.resources, value, context.theme)?.let {
                    lockIconDrawable = it
                    DrawableCompat.setTint(
                        it,
                        resources.getColor(android.R.color.white, context?.theme)
                    )
                }
            }
        }

    private lateinit var lockerButtonUpperDrawable: Drawable
    private var lockerButtonUpperRes: Int = R.drawable.ic_baseline_expand_less_24
        set(value) {
            field = value
            if (field != 0) {
                ResourcesCompat.getDrawable(context.resources, value, context.theme)?.let {
                    lockerButtonUpperDrawable = it
                    DrawableCompat.setTint(it, Color.RED)
                }
            }
        }

    // locker button current position
    private var currentCircleY = 0f

    // the last position of locker button, used to calculate with the latest y position for finding diff of y
    private var currentY = 0f

    // state declaration
    private var isPressing: Boolean = false
    private var isUnlocked: Boolean = false

    //paint that used to draw locker button
    private val lockerButtonPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    //paint that used to draw background when state are in pressed and dragging
    private val pressedOrDraggingBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    //print that used to draw background in normal state
    private val normalBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG)

    private val lockerButtonRectF: RectF = RectF(Rect())

    private val normalBackgroundRectF = RectF(Rect())

    private val pressedOrDraggingBackgroundRectF = RectF(Rect())

    init {

        //desired size initialization
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

        circleMargin = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            circleMarginDp,
            resources.displayMetrics
        ).toInt()

        // paint initialization
        lockerButtonPaint.color = Color.RED
        pressedOrDraggingBackgroundPaint.color = Color.parseColor("#fff1f1f1")

        // attr initialization
        unlockIcon = R.drawable.ic_baseline_arrow_upward_24
        lockIcon = R.drawable.ic_baseline_expand_less_24
        lockerButtonUpperRes = R.drawable.ic_baseline_expand_less_24
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        normalBackgroundRectF.set(
            0f,
            currentCircleY - (circleSize.toFloat() / 2),
            surfaceWidth.toFloat(),
            surfaceHeight.toFloat()
        )
        canvas.drawRoundRect(
            normalBackgroundRectF,
            circleSize.toFloat(),
            circleSize.toFloat(),
            pressedOrDraggingBackgroundPaint
        )

        pressedOrDraggingBackgroundRectF.set(
            0f,
            0f,
            surfaceWidth.toFloat(),
            surfaceHeight.toFloat()
        )
        canvas.drawRoundRect(
            pressedOrDraggingBackgroundRectF,
            circleSize.toFloat(),
            circleSize.toFloat(),
            normalBackgroundPaint
        )

        lockerButtonRectF.set(
            0f,
            currentCircleY - (circleSize / 2),
            circleSize.toFloat(),
            currentCircleY + (circleSize.toFloat() / 2)
        )

        canvas.drawRoundRect(
            lockerButtonRectF,
            circleSize.toFloat(),
            circleSize.toFloat(),
            lockerButtonPaint
        )

        if (!isPressing) {
            canvas.save()
            val drawableWidth = lockerButtonUpperDrawable.intrinsicWidth
            val drawableHeight = lockerButtonUpperDrawable.intrinsicHeight
            val drawableGap = 4.dpToPx
            lockerButtonUpperDrawable.setBounds(
                ((surfaceWidth / 2) - (drawableWidth / 2)),
                (currentCircleY - (circleSize.toFloat() / 2) - drawableGap - drawableHeight).toInt(),
                ((surfaceWidth / 2) + (drawableWidth / 2)),
                ((currentCircleY - (circleSize.toFloat() / 2)) - drawableGap).toInt()
            )
            lockerButtonUpperDrawable.draw(canvas)

            canvas.restore()
        }

        canvas.save()
        if (!isPressing) {
            unlockIconDrawable.setBounds(
                lockerButtonRectF.left.toInt() + 20.dpToPx,
                lockerButtonRectF.top.toInt() + 20.dpToPx,
                lockerButtonRectF.right.toInt() - 20.dpToPx,
                lockerButtonRectF.bottom.toInt() - 20.dpToPx
            )
            unlockIconDrawable.draw(canvas)
        } else {
            lockIconDrawable.setBounds(
                lockerButtonRectF.left.toInt() + 20.dpToPx,
                lockerButtonRectF.top.toInt() + 20.dpToPx,
                lockerButtonRectF.right.toInt() - 20.dpToPx,
                lockerButtonRectF.bottom.toInt() - 20.dpToPx
            )
            lockIconDrawable.draw(canvas)
        }
        canvas.restore()
    }

    @Suppress("RedundantOverride")
    override fun performClick(): Boolean {
        return super.performClick()
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null && isEnabled) {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    Log.d(TAG, "ACTION_DOWN[x=${event.x}, y=${event.y}]")
                    if (event.x in lockerButtonRectF.left..lockerButtonRectF.right
                        && event.y in lockerButtonRectF.top..lockerButtonRectF.bottom
                    ) {
                        isPressing = true
                        //setup position value
                        currentY = event.y.coerceIn(
                            circleSize.toFloat() / 2,
                            (height - (circleSize / 2)).toFloat()
                        )
                        parent.requestDisallowInterceptTouchEvent(true)
                        invalidate()
                    }
                    performClick()
                }
                MotionEvent.ACTION_MOVE -> {
                    if (!isPressing) return true
                    Log.d(TAG, "ACTION_MOVE[x=${event.x}, y=${event.y}]")
                    //Get diff of y between currentY and eventY
                    val diffY = event.y - currentY
                    currentY = event.y
                    //Check if diff value is negative or positive, if positive then increment value, else decrement value
                    currentCircleY += diffY
                    //coerce the position value
                    currentCircleY = currentCircleY.coerceIn(
                        circleSize.toFloat() / 2,
                        (surfaceHeight - (circleSize / 2)).toFloat()
                    )
                    //invalidate view
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    if (!isPressing) return true
                    parent.requestDisallowInterceptTouchEvent(false)
                    val height = surfaceHeight
                    Log.d(TAG, "ACTION_UP[x=${event.x}, y=${event.y}]")
                    val eventY = event.y.coerceAtLeast(0f)
                    val percent = ((height - eventY) * 100 / height)
                    currentCircleY = if (percent <= 50) {
                        isUnlocked = false
                        height - (circleSize / 2).toFloat()
                    } else {
                        isUnlocked = true
                        circleSize.toFloat() / 2
                    }
                    isPressing = false
                    invalidate()
                }
            }
            return true
        }
        return super.onTouchEvent(event)
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

        normalBackgroundPaint.shader = LinearGradient(
            0f, 0f, 0f, surfaceHeight.toFloat(),
            intArrayOf(Color.TRANSPARENT, Color.BLACK),
            floatArrayOf(0.0f, 0.65f),
            Shader.TileMode.CLAMP
        )
        normalBackgroundPaint.alpha = 10

        circleSize = if (surfaceWidth < surfaceHeight) surfaceWidth else surfaceHeight
        if (currentCircleY == 0f) {
            currentCircleY = surfaceHeight - (circleSize.toFloat() / 2)
        }
    }

    private val Int.dpToPx: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()

}