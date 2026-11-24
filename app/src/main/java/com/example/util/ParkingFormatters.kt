package com.example.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object ParkingFormatters {

    fun formatDateTime(millis: Long): String {
        val sdf = SimpleDateFormat("yyyy.MM.dd (E) a h:mm", Locale.KOREAN)
        return sdf.format(Date(millis))
    }

    fun formatShortTime(millis: Long): String {
        val sdf = SimpleDateFormat("M/d a h:mm", Locale.KOREAN)
        return sdf.format(Date(millis))
    }

    fun formatElapsedDuration(parkedAtMillis: Long, nowMillis: Long = System.currentTimeMillis()): String {
        val diffMinutes = TimeUnit.MILLISECONDS.toMinutes((nowMillis - parkedAtMillis).coerceAtLeast(0L))
        return when {
            diffMinutes < 1 -> "방금 주차"
            diffMinutes < 60 -> "${diffMinutes}분 경과"
            diffMinutes < 24 * 60 -> {
                val hours = diffMinutes / 60
                val mins = diffMinutes % 60
                if (mins == 0L) "${hours}시간 경과" else "${hours}시간 ${mins}분 경과"
            }
            else -> {
                val days = diffMinutes / (24 * 60)
                "${days}일 경과"
            }
        }
    }

    fun formatCoordinates(latitude: Double?, longitude: Double?): String {
        if (latitude == null || longitude == null) return "좌표 없음 (메모·사진 전용)"
        return String.format(Locale.US, "%.6f, %.6f", latitude, longitude)
    }

    fun formatAccuracy(accuracyMeters: Float?): String? {
        return accuracyMeters?.let { "±${it.roundToInt()}m" }
    }
}
