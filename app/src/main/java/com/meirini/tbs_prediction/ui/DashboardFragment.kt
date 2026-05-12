package com.meirini.tbs_prediction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.meirini.tbs_prediction.R

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Tarik desain XML fragment_dashboard
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Kenalkan tombol MULAI PREDIKSI dari XML
        val btnGoToPrediksi = view.findViewById<Button>(R.id.btnGoToPrediksi)

        // Kasih perintah pindah ke halaman Prediksi pas diklik
        btnGoToPrediksi.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_prediksiFragment)
        }
    }
}