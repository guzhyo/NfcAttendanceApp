package com.guzhyo.nfcattendance.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.guzhyo.nfcattendance.RecordStore
import com.guzhyo.nfcattendance.databinding.FragmentStatisticsBinding

class StatisticsFragment : Fragment() {

    private var _binding: FragmentStatisticsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentStatisticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        val records = RecordStore.load(requireContext())
        val total = records.size
        val today = records.count { it.date == java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()) }
        val thisMonth = records.count { it.date.take(7) == java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.getDefault()).format(java.util.Date()) }
        val sources = records.groupBy { it.source }.mapValues { it.value.size }

        binding.totalText.text = "$total"
        binding.todayText.text = "$today"
        binding.monthText.text = "$thisMonth"
        binding.manualText.text = "${sources["手动"] ?: 0}"
        binding.nfcText.text = "${(sources["NFC"] ?: 0) + (sources["HCE"] ?: 0)}"
        binding.avgText.text = if (thisMonth > 0) String.format("%.1f", thisMonth / java.util.Calendar.getInstance().get(java.util.Calendar.DAY_OF_MONTH).toFloat()) else "0"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
