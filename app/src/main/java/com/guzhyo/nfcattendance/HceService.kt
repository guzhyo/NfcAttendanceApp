package com.guzhyo.nfcattendance

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import android.util.Log
import java.text.SimpleDateFormat
import java.util.*

class HceService : HostApduService() {

    companion object {
        const val TAG = "NfcHce"
        const val ACTION_RECORD = "com.guzhyo.nfcattendance.RECORD"
        const val EXTRA_TIMESTAMP = "timestamp"
        const val EXTRA_APDU = "apdu"
    }

    override fun onDeactivated(reason: Int) {
        Log.d(TAG, "HCE deactivated, reason=$reason")
    }

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        // 记录刷卡事件
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date(now))
        val apduHex = commandApdu.joinToString("") { "%02X".format(it) }

        Log.i(TAG, "NFC tap detected at $timestamp")

        // 保存到 SharedPreferences
        val prefs = getSharedPreferences("records", MODE_PRIVATE)
        val records = prefs.getStringSet("list", emptySet())?.toMutableSet() ?: mutableSetOf()
        records.add("$timestamp|APDU:${apduHex.take(20)}")
        prefs.edit().putStringSet("list", records).apply()

        // 发送广播通知 MainActivity 更新
        val intent = android.content.Intent(ACTION_RECORD).apply {
            putExtra(EXTRA_TIMESTAMP, timestamp)
        }
        sendBroadcast(intent)

        // 返回成功响应
        return byteArrayOf(0x90.toByte(), 0x00.toByte())
    }
}
