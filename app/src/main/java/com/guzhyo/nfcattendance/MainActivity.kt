package com.guzhyo.nfcattendance

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.guzhyo.nfcattendance.databinding.ActivityMainBinding
import com.guzhyo.nfcattendance.fragments.ClockInFragment
import com.guzhyo.nfcattendance.fragments.RecordsFragment
import com.guzhyo.nfcattendance.fragments.StatisticsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tabs = mutableListOf<TextView>()
    private val fragments = listOf(
        "打卡" to { ClockInFragment() },
        "记录" to { RecordsFragment() },
        "统计" to { StatisticsFragment() },
        "NFC设置" to { NfcSetupFragment() }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fragments.forEachIndexed { i, (name, _) ->
            val tv = TextView(this).apply {
                text = name
                textSize = 15f
                setPadding(24, 0, 24, 0)
                gravity = Gravity.CENTER
                setTextColor(Color.WHITE)
                setOnClickListener { selectTab(i) }
            }
            binding.tabBar.addView(tv)
            tabs.add(tv)
        }

        selectTab(0)
    }

    private fun selectTab(index: Int) {
        tabs.forEachIndexed { i, tv ->
            tv.isSelected = i == index
            tv.setTextColor(if (i == index) Color.parseColor("#4a90d9") else Color.WHITE)
            tv.setBackgroundColor(if (i == index) Color.WHITE else Color.TRANSPARENT)
        }
        val fragment = fragments[index].second()
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
