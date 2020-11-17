package com.schugarkub.dataguard.constants

object NetworkUsageConstants {
    // TODO Calibrate constants
    const val MAX_BYTES_RATE_DEVIATION = 0.5
    const val MIN_CALIBRATION_TIMES = 100
    const val MAX_CALIBRATION_TIMES = 10_000
    const val TX_BYTES_THRESHOLD = 10_000L
}