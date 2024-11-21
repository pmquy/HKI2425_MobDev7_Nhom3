package com.example.facebook.util

import android.annotation.SuppressLint

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