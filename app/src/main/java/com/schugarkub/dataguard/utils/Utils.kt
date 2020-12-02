package com.schugarkub.dataguard.utils

fun floatToPercent(float: Float): Int {
    return when {
        float < 0.0F -> 0
        float > 1.0F -> 100
        else -> (float * 100).toInt()
    }
}

fun percentToFloat(percent: Int): Float {
    return when {
        percent < 0 -> 0.0F
        percent > 100 -> 1.0F
        else -> percent.toFloat() / 100
    }
}