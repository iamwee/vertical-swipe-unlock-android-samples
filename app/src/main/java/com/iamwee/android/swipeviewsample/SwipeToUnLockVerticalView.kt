package com.iamwee.android.swipeviewsample

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat
import kotlin.math.roundToInt
import kotlin.properties.Delegates

typealias OnSwipeProgressChangeListener = (progress: Int) -> Unit

class SwipeToUnLockVerticalView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        private const val TAG = "SwipeUnlockVerticalView"
        private const val SNAP_TO_UNLOCK_PROGRESS = 60
    }

    // surface declaration
    private var surfaceWidth = 0
    private var surfaceHeight = 0

    // desired size declaration
    private var desiredWidthDp = 60f
    private var desiredHeightDp = 140f
    private val desiredWidth: Int
    private val desiredHeight: Int

    // locker button size which declared with smallest desired size
    private var circleSize = 0

    //resources declaration
    private val normalBackgroundColor: Int
    private val unlockBackgroundColor: Int
    private val unLockerButtonColor: Int
    private val tintColor: Int
    private val tintIndicatorColor: Int
    private val srcMarginPx: Int
    private val normalBackgroundAlpha: Int

    // drawable in normal state inside locker button
    private lateinit var unlockIconDrawable: Drawable
    private var unlockIcon: Int = R.drawable.ic_baseline_arrow_upward_24
        set(value) {
            field = value
            if (field != 0) {
                ResourcesCompat.getDrawable(context.resources, value, context.theme)?.let {
                    unlockIconDrawable = it
                    DrawableCompat.setTint(it, tintColor)
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
                    DrawableCompat.setTint(it, tintColor)
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
                    DrawableCompat.setTint(it, tintIndicatorColor)
                }
            }
        }

    // locker button current position
    private var currentTopY = 0f
        set(value) {
            field = value
            //calculate progress included button offset

            //Data given: currentTopY=300, height=350, buttonSize=50, progress=100

            //currentTopY=300 => 100%
            //currentTopY=250 => 250 * 100 / 300 => 84%
            val currentTopProgress = (value * 100 / (surfaceHeight - circleSize)).roundToInt()

            // 100% of button size => 50
            // 84% of button size => 84 * 50 / 100 => 42
            val buttonSizeOffset = (currentTopProgress * circleSize / 100)

            // height=350 => 100%
            // 250+42 = 292 => 292 * 100 / 350 => 84%
            val actualProgress = ((value + buttonSizeOffset) * 100 / surfaceHeight).roundToInt().coerceIn(0..100)
            progress = 100 - actualProgress
            //16 = 100 - 84
        }

    // progress position
    private var progress: Int by Delegates.observable(0) { _, old, new ->
        if (old != new || old == 0 || new == 0) {
            Log.d(TAG, "currentProgress:$new")
            onProgressChangeListener?.invoke(new)
        }
    }

    // the last position of locker button, used to calculate with the latest y position for finding diff of y
    private var currentY = 0f

    // state declaration
    private var isPressing: Boolean = false
    private var isUnlocked: Boolean = false
        set(value) {
            field = value
            if (value) {
                onSwipeActionListener?.onSwipeSucceeded()
            } else {
                onSwipeActionListener?.onSwipeFailed()
            }
        }

    // listener declaration
    private var onProgressChangeListener: OnSwipeProgressChangeListener? = null
    private var onSwipeActionListener: OnSwipeActionListener? = null

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
        val accentColor = ContextCompat.getColor(context, R.color.colorAccent)
        val attr = context.theme.obtainStyledAttributes(attrs, R.styleable.SwipeToUnLockVerticalView, defStyleAttr, 0)

        try {
            normalBackgroundColor = attr.getColor(R.styleable.SwipeToUnLockVerticalView_normalBackgroundColor, Color.parseColor("#fd968d"))
            unlockBackgroundColor = attr.getColor(R.styleable.SwipeToUnLockVerticalView_unlockBackgroundColor, Color.parseColor("#f1f1f1"))
            tintColor = attr.getColor(R.styleable.SwipeToUnLockVerticalView_tint, Color.WHITE)
            unLockerButtonColor = attr.getColor(R.styleable.SwipeToUnLockVerticalView_unLockerButtonColor, accentColor)
            tintIndicatorColor = attr.getColor(R.styleable.SwipeToUnLockVerticalView_tintIndicator, accentColor)
            srcMarginPx = attr.getDimensionPixelSize(R.styleable.SwipeToUnLockVerticalView_srcMargin, 20.dpToPx)
            normalBackgroundAlpha = attr.getInt(R.styleable.SwipeToUnLockVerticalView_normalBackgroundAlpha, 50)

            unlockIcon = attr.getResourceId(R.styleable.SwipeToUnLockVerticalView_unlockSrc, R.drawable.ic_baseline_arrow_upward_24)
            lockIcon = attr.getResourceId(R.styleable.SwipeToUnLockVerticalView_lockSrc, unlockIcon)
            lockerButtonUpperRes = attr.getResourceId(R.styleable.SwipeToUnLockVerticalView_indicatorSrc, R.drawable.ic_baseline_expand_less_24)

            // paint initialization
            lockerButtonPaint.color = unLockerButtonColor
            pressedOrDraggingBackgroundPaint.color = unlockBackgroundColor
        } finally {
            attr.recycle()
        }

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
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        lockerButtonRectF.set(
            0f,
            currentTopY,
            circleSize.toFloat(),
            currentTopY + circleSize
        )

        normalBackgroundRectF.set(
            0f,
            0f,
            lockerButtonRectF.right,
            lockerButtonRectF.bottom
        )
        canvas.drawRoundRect(
            normalBackgroundRectF,
            circleSize.toFloat(),
            circleSize.toFloat(),
            normalBackgroundPaint
        )

        pressedOrDraggingBackgroundRectF.set(
            lockerButtonRectF.left,
            lockerButtonRectF.top,
            surfaceWidth.toFloat(),
            surfaceHeight.toFloat()
        )
        canvas.drawRoundRect(
            pressedOrDraggingBackgroundRectF,
            circleSize.toFloat(),
            circleSize.toFloat(),
            pressedOrDraggingBackgroundPaint
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
                (currentTopY - drawableGap - drawableHeight).toInt(),
                ((surfaceWidth / 2) + (drawableWidth / 2)),
                (currentTopY - drawableGap).toInt()
            )
            lockerButtonUpperDrawable.draw(canvas)

            canvas.restore()
        }

        canvas.save()
        if (!isPressing) {
            unlockIconDrawable.setBounds(
                lockerButtonRectF.left.toInt() + srcMarginPx,
                lockerButtonRectF.top.toInt() + srcMarginPx,
                lockerButtonRectF.right.toInt() - srcMarginPx,
                lockerButtonRectF.bottom.toInt() - srcMarginPx
            )
            unlockIconDrawable.draw(canvas)
        } else {
            lockIconDrawable.setBounds(
                lockerButtonRectF.left.toInt() + srcMarginPx,
                lockerButtonRectF.top.toInt() + srcMarginPx,
                lockerButtonRectF.right.toInt() - srcMarginPx,
                lockerButtonRectF.bottom.toInt() - srcMarginPx
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
                    //Get diff of y between currentY and eventY
                    val diffY = event.y - currentY
                    currentY = event.y
                    //Check if diff value is negative or positive, if positive then increment value, else decrement value
                    currentTopY += diffY
                    //coerce the position value
                    currentTopY = currentTopY.coerceIn(0f, surfaceHeight.toFloat() - circleSize)
                    //invalidate view
                    invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    if (!isPressing) return true
                    parent.requestDisallowInterceptTouchEvent(false)

                    val destCurrentTopY = if (progress > SNAP_TO_UNLOCK_PROGRESS) {
                        isUnlocked = true
                        0f
                    } else {
                        isUnlocked = false
                        surfaceHeight.toFloat() - circleSize
                    }
                    isPressing = false

                    val postAnimator = ValueAnimator.ofFloat(currentTopY, destCurrentTopY)
                    postAnimator.addUpdateListener {
                        currentTopY = it.animatedValue as Float
                        invalidate()
                    }
                    postAnimator.duration = 250L
                    postAnimator.start()
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
            intArrayOf(Color.TRANSPARENT, normalBackgroundColor),
            floatArrayOf(0.0f, 0.55f),
            Shader.TileMode.CLAMP
        )
        normalBackgroundPaint.alpha = normalBackgroundAlpha

        circleSize = if (surfaceWidth < surfaceHeight) surfaceWidth else surfaceHeight
        //force unLock button to bottom
        if (currentTopY == 0f) {
            currentTopY = surfaceHeight.toFloat() - circleSize
        }
    }

    fun setOnProgressChangeListener(listener: OnSwipeProgressChangeListener) {
        onProgressChangeListener = listener
    }

    fun setOnSwipeActionListener(listener: OnSwipeActionListener) {
        onSwipeActionListener = listener
    }

    private val Int.dpToPx: Int
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            this.toFloat(),
            resources.displayMetrics
        ).toInt()

    interface OnSwipeActionListener {
        fun onSwipeSucceeded()
        fun onSwipeFailed()
    }

}