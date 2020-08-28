package com.iamwee.android.swipeviewsample

object SizeOffsetCalculator {

    fun getActualSize(width: Int, height: Int, surfaceWidth: Int, surfaceHeight: Int, margin: Int = 0): Pair<Int, Int> {
        val (widthOffset, heightOffset) = when {
            width in (0..surfaceWidth) && height in (0..surfaceHeight) -> width to height
            width > surfaceWidth -> {
                surfaceWidth to getOffsetSize(
                    actualSize1 = height,
                    actualSize2 = width,
                    offsetSize2 = surfaceWidth
                )
            }
            height > surfaceHeight -> {
                getOffsetSize(
                    actualSize1 = width,
                    actualSize2 = height,
                    offsetSize2 = surfaceHeight
                ) to surfaceHeight
            }
            else -> 0 to 0
        }

        val hasIncludedMarginHorizontal = widthOffset > heightOffset
        return if (hasIncludedMarginHorizontal) {
            widthOffset - margin to getOffsetSize(heightOffset, widthOffset, widthOffset - margin)
        } else {
            getOffsetSize(widthOffset, heightOffset, heightOffset - margin) to heightOffset - margin
        }
    }


    private fun getOffsetSize(actualSize1: Int, actualSize2: Int, offsetSize2: Int): Int {
        val ratio = (offsetSize2 * 100 / actualSize2.toFloat()) / 100.toFloat()
        return (actualSize1 * ratio).toInt()
    }
}