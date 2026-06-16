package com.guzhyo.nfcattendance

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.guzhyo.nfcattendance.databinding.ActivityMainBinding
import com.guzhyo.nfcattendance.fragments.ClockInFragment
import com.guzhyo.nfcattendance.fragments.HelpFragment
import com.guzhyo.nfcattendance.fragments.RecordsFragment
import com.guzhyo.nfcattendance.fragments.StatisticsFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (savedInstanceState == null) {
            switchFragment(ClockInFragment())
        }

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_clockin -> switchFragment(ClockInFragment())
                R.id.nav_records -> switchFragment(RecordsFragment())
                R.id.nav_stats -> switchFragment(StatisticsFragment())
                R.id.nav_help -> switchFragment(HelpFragment())
            }
            true
        }
    }

    private fun switchFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.container, fragment)
            .commit()
    }
}
