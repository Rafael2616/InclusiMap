package com.rafael.inclusimap.core.util.map

import androidx.compose.ui.graphics.Color

fun String.extractUserEmail(): String? = try {
    this.split("_")[1].split("-")[0]
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun String.extractPlaceID(): String? = try {
    this.split("_")[0]
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun String.extractPlaceUserEmail(): String? = try {
    this.split("_")[1].removeSuffix(".json")
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun String.extractImageDate(): String? = try {
    this
        .split("_")[1]
        .split("-")
        .drop(1)
        .joinToString("-")
        .split("T")[0]
        .formatDate()
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun String.formatDate(): String? = try {
    val dateRegex = """(\d{4}-\d{2}-\d{2})""".toRegex()
    val date = dateRegex.find(this)?.value

    date?.let {
        val (year, month, day) = it.split("-")
        "$day/$month/$year"
    }
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun String.removeTime(): String? = try {
    this.split("T")[0]
} catch (e: Exception) {
    e.printStackTrace()
    null
}

fun Float.toColor(): Color = when (this) {
    in 1f..1.666f -> Color.Red
    in 1.666f..2.333f -> Color.Yellow
    in 2.333f..3f -> Color.Green
    else -> Color.Cyan
}
