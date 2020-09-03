package com.iamwee.android.swipeviewsample

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieCompositionFactory
import com.airbnb.lottie.LottieDrawable
import kotlin.math.roundToInt

class LottieInsiderView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
): View(context, attrs, defStyleAttr) {

    private var surfaceWidth = 0
    private var surfaceHeight = 0

    private var desiredWidthDp = 50f
    private var desiredHeightDp = 50f
    private val desiredWidth: Int
    private val desiredHeight: Int

    private val lottieDrawable by lazy { LottieDrawable() }
    private val lottieView by lazy { LottieAnimationView(context) }

    init {
        desiredWidth = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            desiredWidthDp,
            resources.displayMetrics
        ).roundToInt()

        desiredHeight = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            desiredHeightDp,
            resources.displayMetrics
        ).roundToInt()
    }

    private val paint by lazy {
        Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#cdcdcd")
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas ?: return
        canvas.drawRect(0f, 0f, surfaceWidth.toFloat(), surfaceHeight.toFloat(), paint)
        canvas.save()
        lottieDrawable.draw(canvas)
        canvas.restore()

    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val width = when (widthMode) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> desiredWidth.coerceAtMost(widthSize)
            else -> desiredWidth
        }

        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        val height = when (heightMode) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> desiredHeight.coerceAtMost(heightSize)
            else -> desiredHeight
        }
        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        surfaceWidth = w
        surfaceHeight = h

        LottieCompositionFactory.fromRawRes(context, R.raw.unlock).addListener {
            lottieDrawable.composition = it
            lottieDrawable.repeatCount = LottieDrawable.INFINITE
            lottieDrawable.scale = ((surfaceWidth / 2) * 100f / it.bounds.right.toFloat()) / 100f
            lottieDrawable.addAnimatorUpdateListener { invalidate() }
            lottieDrawable.playAnimation()
            invalidate()
        }
    }
}