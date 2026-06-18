package com.guzhyo.nfcattendance

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.guzhyo.nfcattendance.databinding.FragmentNfcSetupBinding

class NfcSetupFragment : Fragment() {

    private var _binding: FragmentNfcSetupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentNfcSetupBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.accessibilityBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        binding.nfcPaymentBtn.setOnClickListener {
            try {
                startActivity(Intent(Settings.ACTION_NFC_PAYMENT_SETTINGS))
            } catch (_: Exception) {
                startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
            }
        }

        binding.nfcSettingsBtn.setOnClickListener {
            startActivity(Intent(Settings.ACTION_NFC_SETTINGS))
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
