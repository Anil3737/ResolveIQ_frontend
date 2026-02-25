package com.simats.resolveiq_frontend.utils

import android.os.Build
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/**
 * Converts an ISO 8601 UTC timestamp string (e.g. "2026-02-17T07:37:53+00:00")
 * to the device's local timezone, formatted as "dd MMM yyyy, hh:mm a".
 *
 * Falls back gracefully on older API levels or parse errors.
 */
fun convertUtcToLocal(utcString: String?): String {
    if (utcString.isNullOrBlank()) return "N/A"
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val utcDateTime = OffsetDateTime.parse(utcString)
            val localDateTime = utcDateTime.atZoneSameInstant(ZoneId.systemDefault())
            localDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", Locale.getDefault()))
        } else {
            // Fallback for API < 26
            val formats = listOf(
                "yyyy-MM-dd'T'HH:mm:ssXXX",
                "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
                "yyyy-MM-dd'T'HH:mm:ssZ"
            )
            var parsed: Date? = null
            for (format in formats) {
                try {
                    val sdf = SimpleDateFormat(format, Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC")
                    parsed = sdf.parse(utcString)
                    break
                } catch (_: Exception) {}
            }
            if (parsed != null) {
                val displaySdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())
                displaySdf.timeZone = TimeZone.getDefault()
                displaySdf.format(parsed)
            } else {
                utcString
            }
        }
    } catch (e: Exception) {
        utcString
    }
}

/**
 * Short format: "dd MMM, hh:mm a" — for compact list views.
 */
fun convertUtcToLocalShort(utcString: String?): String {
    if (utcString.isNullOrBlank()) return "N/A"
    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val utcDateTime = OffsetDateTime.parse(utcString)
            val localDateTime = utcDateTime.atZoneSameInstant(ZoneId.systemDefault())
            localDateTime.format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a", Locale.getDefault()))
        } else {
            convertUtcToLocal(utcString)
        }
    } catch (e: Exception) {
        utcString
    }
}
