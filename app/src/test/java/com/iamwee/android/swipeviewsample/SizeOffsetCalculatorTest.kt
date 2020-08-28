package com.iamwee.android.swipeviewsample

import org.junit.Assert.*
import org.junit.Test

class SizeOffsetCalculatorTest {

    @Test
    fun test_1() {
        val inputWidth = 55
        val inputHeight = 40
        val surfaceWidth = 50
        val surfaceHeight = 50
        val margin = 4

        val (widthOutput, widthHeight) = SizeOffsetCalculator.getActualSize(
            width = inputWidth,
            height = inputHeight,
            surfaceWidth = surfaceWidth,
            surfaceHeight = surfaceHeight,
            margin = margin
        )

        assertEquals(46, widthOutput)
        assertEquals(33, widthHeight)
    }

    @Test
    fun test_2() {
        val inputWidth = 50
        val inputHeight = 40
        val surfaceWidth = 50
        val surfaceHeight = 50
        val margin = 4

        val (widthOutput, widthHeight) = SizeOffsetCalculator.getActualSize(
            width = inputWidth,
            height = inputHeight,
            surfaceWidth = surfaceWidth,
            surfaceHeight = surfaceHeight,
            margin = margin
        )

        assertEquals(46, widthOutput)
        assertEquals(36, widthHeight)
    }

    @Test
    fun test_3() {
        val inputHeight = 55
        val inputWidth = 40
        val surfaceWidth = 50
        val surfaceHeight = 50
        val margin = 4

        val (widthOutput, widthHeight) = SizeOffsetCalculator.getActualSize(
            width = inputWidth,
            height = inputHeight,
            surfaceWidth = surfaceWidth,
            surfaceHeight = surfaceHeight,
            margin = margin
        )

        assertEquals(46, widthHeight)
        assertEquals(33, widthOutput)
    }

    @Test
    fun test_4() {

        val inputHeight = 50
        val inputWidth = 40
        val surfaceWidth = 50
        val surfaceHeight = 50
        val margin = 4

        val (widthOutput, widthHeight) = SizeOffsetCalculator.getActualSize(
            width = inputWidth,
            height = inputHeight,
            surfaceWidth = surfaceWidth,
            surfaceHeight = surfaceHeight,
            margin = margin
        )

        assertEquals(46, widthHeight)
        assertEquals(36, widthOutput)
    }
}