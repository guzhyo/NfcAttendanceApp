package com.guzhyo.nfcattendance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.guzhyo.nfcattendance.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val records = mutableListOf<String>()
    private lateinit var adapter: RecordAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RecordAdapter(records)
        binding.recordList.layoutManager = LinearLayoutManager(this)
        binding.recordList.adapter = adapter
        refreshRecords()
        updateStatus()

        binding.method1Btn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.method2Btn.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_NFC_PAYMENT_SETTINGS))
            } catch (_: Exception) {
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
        }

        binding.clearBtn.setOnClickListener {
            records.clear()
            getSharedPreferences("records", MODE_PRIVATE).edit().putStringSet("list", emptySet()).apply()
            adapter.notifyDataSetChanged()
            binding.countText.text = "刷卡次数: 0"
        }

        // 每3秒刷新列表
        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        val refresher = object : Runnable {
            override fun run() {
                refreshRecords()
                handler.postDelayed(this, 3000)
            }
        }
        handler.postDelayed(refresher, 3000)
    }

    private fun updateStatus() {
        try {
            val adapter = NfcAdapter.getDefaultAdapter(this)
            when {
                adapter == null -> binding.statusText.text = "设备不支持 NFC"
                !adapter.isEnabled -> binding.statusText.text = "NFC 未开启"
                else -> binding.statusText.text = "NFC 已开启 · 选一种方式启用监控"
            }
        } catch (e: Exception) {
            binding.statusText.text = "状态检测失败"
        }
    }

    private fun refreshRecords() {
        val prefs = getSharedPreferences("records", MODE_PRIVATE)
        val saved = prefs.getStringSet("list", emptySet()) ?: emptySet()
        val newRecords = saved.sortedDescending()
        if (newRecords != records) {
            records.clear()
            records.addAll(newRecords)
            adapter.notifyDataSetChanged()
            binding.countText.text = "刷卡次数: ${records.size}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
