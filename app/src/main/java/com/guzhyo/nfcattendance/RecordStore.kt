package com.guzhyo.nfcattendance

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.*

data class AttendanceRecord(
    val date: String,      // 2026-06-17
    val time: String,      // 08:30:15
    val source: String,    // 手动 / NFC / HCE
    val detail: String = "" // 卡号/APDU
)

object RecordStore {
    private const val KEY = "attendance_records"
    private const val SEP = "\u001F" // unit separator

    fun add(context: Context, source: String, detail: String = ""): AttendanceRecord {
        val now = Calendar.getInstance()
        val date = String.format("%04d-%02d-%02d",
            now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH))
        val time = String.format("%02d:%02d:%02d",
            now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND))
        val record = AttendanceRecord(date, time, source, detail)

        val prefs = context.getSharedPreferences("store", Context.MODE_PRIVATE)
        val list = load(context).toMutableList()
        list.add(0, record)
        // 只保留最近 500 条
        val trimmed = if (list.size > 500) list.take(500) else list
        val serialized = trimmed.joinToString("\n") { "${it.date}$SEP${it.time}$SEP${it.source}$SEP${it.detail}" }
        prefs.edit().putString(KEY, serialized).apply()
        return record
    }

    fun load(context: Context): List<AttendanceRecord> {
        val prefs = context.getSharedPreferences("store", Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split("\n").mapNotNull { line ->
            val parts = line.split(SEP)
            if (parts.size >= 3) AttendanceRecord(parts[0], parts[1], parts[2], parts.getOrElse(3) { "" })
            else null
        }
    }

    fun clear(context: Context) {
        context.getSharedPreferences("store", Context.MODE_PRIVATE).edit().remove(KEY).apply()
    }
}
