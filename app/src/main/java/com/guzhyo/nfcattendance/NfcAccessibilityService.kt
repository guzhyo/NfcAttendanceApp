package com.guzhyo.nfcattendance

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent
import java.text.SimpleDateFormat
import java.util.*

class NfcAccessibilityService : AccessibilityService() {

    companion object {
        // 匹配 NFC 相关的包名和类名
        private val NFC_PACKAGES = setOf(
            "com.android.nfc",
            "com.android.systemui",
            "com.google.android.gms"
        )
        private val NFC_TEXT_PATTERNS = listOf(
            "NFC", "nfc", "card", "Card", "卡", "tap", "read",
            "transaction", "payment", "付", "读卡", "刷卡"
        )
    }

    private var lastRecordTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return

        val packageName = event.packageName?.toString() ?: return
        if (packageName !in NFC_PACKAGES) return

        // 检测 NFC 相关事件
        val text = event.text?.joinToString(" ") ?: ""
        val contentDesc = event.contentDescription?.toString() ?: ""

        val combined = "$text $contentDesc"
        val isNfcEvent = NFC_TEXT_PATTERNS.any { combined.contains(it) }

        if (isNfcEvent) {
            val now = System.currentTimeMillis()
            // 防抖：500ms 内不重复记录
            if (now - lastRecordTime < 500) return
            lastRecordTime = now

            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val timestamp = sdf.format(Date(now))

            val prefs = getSharedPreferences("records", MODE_PRIVATE)
            val saved = prefs.getStringSet("list", emptySet())?.toMutableSet() ?: mutableSetOf()
            saved.add("$timestamp|Auto:$text")
            prefs.edit().putStringSet("list", saved).apply()

            android.util.Log.d("NfcAS", "NFC event: $text")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS or
                    AccessibilityServiceInfo.FLAG_RETRIEVE_INTERACTIVE_WINDOWS
            notificationTimeout = 100
        }
        serviceInfo = info
        android.util.Log.d("NfcAS", "Accessibility service connected")
    }

    override fun onInterrupt() {}
}
