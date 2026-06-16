package com.guzhyo.nfcattendance

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.nfc.NfcAdapter
import android.nfc.cardemulation.CardEmulation
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
            refreshRecords()
            if (!isFinishing) {
                Toast.makeText(this@MainActivity, "刷卡成功 $ts", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        refreshRecords()
        adapter = RecordAdapter(records)
        binding.recordList.layoutManager = LinearLayoutManager(this)
        binding.recordList.adapter = adapter

        checkNfcState()

        binding.setupBtn.setOnClickListener {
            // 直接跳到 NFC 付款设置
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
            Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show()
        }

        registerReceiver(recordReceiver,
            IntentFilter("com.guzhyo.nfcattendance.RECORD"),
            Context.RECEIVER_NOT_EXPORTED)
    }

    override fun onResume() {
        super.onResume()
        checkNfcState()
    }

    private fun checkNfcState() {
        val adapter = NfcAdapter.getDefaultAdapter(this)
        if (adapter == null) {
            binding.statusText.text = "设备不支持 NFC"
            return
        }
        if (!adapter.isEnabled) {
            binding.statusText.text = "NFC 未开启"
            binding.setupBtn.visibility = android.view.View.VISIBLE
            binding.setupBtn.text = "打开 NFC 设置"
            binding.hintText.text = ""
            return
        }

        val cardEmulation = CardEmulation.getInstance(adapter)
        val isDefault = cardEmulation.isDefaultServiceForCategory(
            android.content.ComponentName(this, HceService::class.java),
            CardEmulation.CATEGORY_OTHER
        )

        if (isDefault) {
            binding.statusText.text = "已就绪，靠近读卡器自动记录"
            binding.setupBtn.visibility = android.view.View.GONE
            binding.hintText.text = ""
        } else {
            binding.statusText.text = "需要设为默认 NFC 服务"
            binding.setupBtn.visibility = android.view.View.VISIBLE
            binding.setupBtn.text = "去设置默认 NFC 服务"
            binding.hintText.text = "在付款设置页面 →「其他」→ 选「NFC考勤打卡」"
        }
    }

    private fun refreshRecords() {
        val prefs = getSharedPreferences("records", MODE_PRIVATE)
        val saved = prefs.getStringSet("list", emptySet()) ?: emptySet()
        records.clear()
        records.addAll(saved.sortedDescending())
        adapter?.notifyDataSetChanged()
        binding.countText.text = "刷卡次数: ${records.size}"
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(recordReceiver) } catch (_: Exception) {}
    }
}
