package com.guzhyo.nfcattendance

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.view.accessibility.AccessibilityEvent

class NfcAccessibilityService : AccessibilityService() {

    private var lastRecordTime = 0L

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event ?: return
        val pkg = event.packageName?.toString() ?: return
        if (pkg !in setOf("com.android.nfc", "com.android.systemui")) return

        val text = event.text?.joinToString(" ") ?: ""
        val combined = "$text ${event.contentDescription ?: ""}"
        val isNfc = listOf("NFC", "nfc", "card", "Card", "卡", "tap", "read", "读卡", "刷卡")
            .any { combined.contains(it) }

        if (isNfc) {
            val now = System.currentTimeMillis()
            if (now - lastRecordTime < 500) return
            lastRecordTime = now
            RecordStore.add(this, "NFC自动", "")
        }
    }

    override fun onServiceConnected() {
        super.onServiceConnected()
        serviceInfo = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED or
                        AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.DEFAULT
            notificationTimeout = 100
        }
    }

    override fun onInterrupt() {}
}
