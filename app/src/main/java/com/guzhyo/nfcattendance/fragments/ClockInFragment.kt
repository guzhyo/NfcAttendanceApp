package com.guzhyo.nfcattendance.fragments

import android.animation.ValueAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.guzhyo.nfcattendance.RecordStore
import com.guzhyo.nfcattendance.databinding.FragmentClockInBinding
import java.text.SimpleDateFormat
import java.util.*

class ClockInFragment : Fragment() {

    private var _binding: FragmentClockInBinding? = null
    private val binding get() = _binding!!
    private val handler = Handler(Looper.getMainLooper())
    private var timer: Runnable? = null
    private var lastRecord: String? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentClockInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startClock()

        binding.clockBtn.setOnClickListener {
            val r = RecordStore.add(requireContext(), "手动")
            lastRecord = "${r.date} ${r.time}"
            binding.recordTip.text = "打卡成功 $lastRecord"
            binding.recordTip.visibility = View.VISIBLE
            animateButton(binding.clockBtn)
            handler.postDelayed({ binding.recordTip.visibility = View.GONE }, 3000)
        }

        binding.nfcBtn.setOnClickListener {
            val r = RecordStore.add(requireContext(), "NFC")
            lastRecord = "${r.date} ${r.time}"
            binding.recordTip.text = "NFC打卡 $lastRecord"
            binding.recordTip.visibility = View.VISIBLE
            animateButton(binding.nfcBtn)
            handler.postDelayed({ binding.recordTip.visibility = View.GONE }, 3000)
        }
    }

    private fun startClock() {
        timer = object : Runnable {
            override fun run() {
                val now = Calendar.getInstance()
                binding.timeText.text = String.format("%02d:%02d:%02d",
                    now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), now.get(Calendar.SECOND))
                binding.dateText.text = String.format("%d年%02d月%02d日",
                    now.get(Calendar.YEAR), now.get(Calendar.MONTH) + 1, now.get(Calendar.DAY_OF_MONTH))
                binding.weekText.text = arrayOf("星期日","星期一","星期二","星期三","星期四","星期五","星期六")[now.get(Calendar.DAY_OF_WEEK) - 1]
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(timer!!)
    }

    private fun animateButton(view: View) {
        ValueAnimator.ofFloat(1f, 0.92f, 1f).apply {
            duration = 200
            addUpdateListener { view.scaleX = it.animatedValue as Float; view.scaleY = it.animatedValue as Float }
            start()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        timer?.let { handler.removeCallbacks(it) }
        _binding = null
    }
}
