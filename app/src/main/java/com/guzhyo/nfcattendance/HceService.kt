package com.guzhyo.nfcattendance

import android.nfc.cardemulation.HostApduService
import android.os.Bundle
import java.text.SimpleDateFormat
import java.util.*

class HceService : HostApduService() {

    override fun onDeactivated(reason: Int) {}

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        val now = System.currentTimeMillis()
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date(now))
        val apduHex = commandApdu.joinToString("") { "%02X".format(it) }.take(20)

        val prefs = getSharedPreferences("records", MODE_PRIVATE)
        val saved = prefs.getStringSet("list", emptySet())?.toMutableSet() ?: mutableSetOf()
        saved.add("$timestamp|APDU:$apduHex")
        prefs.edit().putStringSet("list", saved).apply()

        android.content.Intent("com.guzhyo.nfcattendance.RECORD").apply {
            putExtra("timestamp", timestamp)
            sendBroadcast(this)
        }

        return byteArrayOf(0x90.toByte(), 0x00.toByte())
    }
}
