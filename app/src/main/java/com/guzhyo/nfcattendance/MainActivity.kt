package com.guzhyo.nfcattendance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.guzhyo.nfcattendance.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val records = mutableListOf<String>()
    private lateinit var adapter: RecordAdapter

    private val recordReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val ts = intent?.getStringExtra("timestamp") ?: return
            runOnUiThread { refreshRecords() }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = RecordAdapter(records)
        binding.recordList.layoutManager = LinearLayoutManager(this)
        binding.recordList.adapter = adapter
        refreshRecords()
        updateNfcStatus()

        binding.setupBtn.setOnClickListener {
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

        try {
            registerReceiver(recordReceiver,
                IntentFilter("com.guzhyo.nfcattendance.RECORD"),
                Context.RECEIVER_NOT_EXPORTED)
        } catch (_: Exception) {}
    }

    override fun onResume() {
        super.onResume()
        updateNfcStatus()
    }

    private fun updateNfcStatus() {
        try {
            val adapter = NfcAdapter.getDefaultAdapter(this)
            when {
                adapter == null -> {
                    binding.statusText.text = "设备不支持 NFC"
                    binding.setupBtn.visibility = android.view.View.GONE
                }
                !adapter.isEnabled -> {
                    binding.statusText.text = "NFC 未开启"
                    binding.setupBtn.visibility = android.view.View.VISIBLE
                    binding.setupBtn.text = "打开 NFC 设置"
                }
                else -> {
                    binding.statusText.text = "NFC 已开启 · 点击下方按钮设置默认服务"
                    binding.setupBtn.visibility = android.view.View.VISIBLE
                    binding.setupBtn.text = "设为默认 NFC 服务"
                    binding.hintText.text = "在设置页面 → 点右上角或「其他」→ 选「NFC考勤打卡」"
                }
            }
        } catch (e: Exception) {
            binding.statusText.text = "错误: ${e.message}"
        }
    }

    private fun refreshRecords() {
        val prefs = getSharedPreferences("records", MODE_PRIVATE)
        val saved = prefs.getStringSet("list", emptySet()) ?: emptySet()
        records.clear()
        records.addAll(saved.sortedDescending())
        adapter.notifyDataSetChanged()
        binding.countText.text = "刷卡次数: ${records.size}"
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(recordReceiver) } catch (_: Exception) {}
    }
}
