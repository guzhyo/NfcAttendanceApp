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

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val records = mutableListOf<String>()
    private lateinit var adapter: RecordAdapter

    private val recordReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val ts = intent?.getStringExtra(HceService.EXTRA_TIMESTAMP) ?: return
            records.add(0, ts)
            adapter.notifyItemInserted(0)
            binding.countText.text = "刷卡次数: ${records.size}"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 加载历史记录
        val prefs = getSharedPreferences("records", MODE_PRIVATE)
        val saved = prefs.getStringSet("list", emptySet()) ?: emptySet()
        records.addAll(saved.sortedDescending())
        binding.countText.text = "刷卡次数: ${records.size}"

        adapter = RecordAdapter(records)
        binding.recordList.layoutManager = LinearLayoutManager(this)
        binding.recordList.adapter = adapter

        // 检查 NFC / HCE 状态
        val nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        if (nfcAdapter == null) {
            binding.statusText.text = "设备不支持 NFC"
        } else if (!nfcAdapter.isEnabled) {
            binding.statusText.text = "NFC 未开启"
            binding.setupBtn.setOnClickListener {
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
        } else {
            binding.statusText.text = "NFC 已就绪，靠近读卡器即可记录"
        }

        // 注册广播
        registerReceiver(recordReceiver,
            IntentFilter(HceService.ACTION_RECORD),
            Context.RECEIVER_NOT_EXPORTED)

        // 清空按钮
        binding.clearBtn.setOnClickListener {
            records.clear()
            prefs.edit().putStringSet("list", emptySet()).apply()
            adapter.notifyDataSetChanged()
            binding.countText.text = "刷卡次数: 0"
            Toast.makeText(this, "已清空", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try { unregisterReceiver(recordReceiver) } catch (_: Exception) {}
    }
}
