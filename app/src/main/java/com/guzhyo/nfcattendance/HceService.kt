package com.guzhyo.nfcattendance

import android.nfc.cardemulation.HostApduService
import android.os.Bundle

class HceService : HostApduService() {

    private var lastTap = 0L

    override fun onDeactivated(reason: Int) {}

    override fun processCommandApdu(commandApdu: ByteArray, extras: Bundle?): ByteArray {
        val now = System.currentTimeMillis()
        if (now - lastTap > 2000) {
            lastTap = now
            val apduHex = commandApdu.joinToString("") { "%02X".format(it) }.take(16)
            RecordStore.add(this, "HCE", apduHex)
        }
        return byteArrayOf(0x90.toByte(), 0x00.toByte())
    }
}
