package com.example.facebook.util

import android.annotation.SuppressLint
import java.text.SimpleDateFormat
import java.util.Locale

@SuppressLint("DefaultLocale")
fun Long.toTime(): String {
    val second = this % 60
    val minute = this / 60 % 60
    val hour = this / 3600
    return if (hour > 0) {
        String.format("%02d:%02d:%02d", hour, minute, second)
    } else {
        String.format("%02d:%02d", minute, second)
    }
}

@SuppressLint("ConstantLocale")
val mongoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())

fun parseDate(date: String, format: String):String {
    return try {
        val d = mongoDateFormat.parse(date)
        SimpleDateFormat(format, Locale.getDefault()).format(d)
    } catch (e: Exception) {
        ""
    }
}