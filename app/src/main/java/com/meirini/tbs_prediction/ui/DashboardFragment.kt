package com.meirini.tbs_prediction.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.firestore.FirebaseFirestore
import com.meirini.tbs_prediction.R

class DashboardFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // 1. Inisialisasi Tombol dan Grafik
        val btnGoToPrediksi = view.findViewById<Button>(R.id.btnMulaiPrediksi)
        val lineChartTBS = view.findViewById<LineChart>(R.id.lineChart)

        // 2. Tarik Data Prediksi AI dari Firebase dan Gambar Grafiknya
        setupGrafikPrediksiAI(lineChartTBS)

        // 3. Aksi Tombol Pindah ke Kalkulator
        btnGoToPrediksi.setOnClickListener {
            findNavController().navigate(R.id.action_dashboardFragment_to_prediksiFragment)
        }
    }

    private fun setupGrafikPrediksiAI(chart: LineChart) {
        firestore.collection("Harga_Resmi").document("terkini").get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val daftarHarga = document.get("daftar_harga") as? List<Map<String, Any>>
                    if (daftarHarga.isNullOrEmpty()) return@addOnSuccessListener

                    val entries = ArrayList<Entry>()
                    val labelsUmur = ArrayList<String>()

                    // Urutkan data berdasarkan umur tanaman biar garisnya urut dari kiri ke kanan
                    val sortedList = daftarHarga.sortedBy { (it["umur"] as? Long) ?: 0L }

                    var index = 0f
                    for (item in sortedList) {
                        val umur = (item["umur"] as? Long) ?: 0L
                        val harga = item["harga_tbs"].toString().toFloatOrNull() ?: 0f

                        entries.add(Entry(index, harga))

                        // Bikin label teks di bawah grafik (Sumbu X)
                        labelsUmur.add(if (umur == 10L) "10-20 Thn" else "$umur Thn")
                        index++
                    }

                    // Desain Garis ala Aplikasi Modern
                    val dataSet = LineDataSet(entries, "Harga Prediksi (Rp/Kg)")
                    dataSet.color = Color.parseColor("#2E7D32")
                    dataSet.setCircleColor(Color.parseColor("#0288D1"))
                    dataSet.lineWidth = 3f
                    dataSet.circleRadius = 4f // Lingkaran diperkecil sedikit
                    dataSet.setDrawFilled(true)
                    dataSet.fillColor = Color.parseColor("#E8F5E9")
                    dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER

                    // ==================================================
                    // Matikan angka detail di atas titik
                    // ==================================================
                    dataSet.setDrawValues(false)

                    val lineData = LineData(dataSet)
                    chart.data = lineData

                    // Kustomisasi Sumbu X (Bawah - Umur Tanaman)
                    val xAxis = chart.xAxis
                    xAxis.position = XAxis.XAxisPosition.BOTTOM
                    xAxis.valueFormatter = IndexAxisValueFormatter(labelsUmur)
                    xAxis.setDrawGridLines(false)
                    xAxis.granularity = 1f
                    xAxis.labelRotationAngle = -45f
                    xAxis.textSize = 9f // Ukuran font dikecilkan
                    xAxis.setLabelCount(7, false) // Biarkan teks melompat (skip) agar tidak menumpuk

                    // Kustomisasi Sumbu Y Kiri (Harga)
                    val yAxis = chart.axisLeft
                    yAxis.textSize = 10f
                    yAxis.spaceTop = 20f // Beri ruang napas di atap grafik
                    yAxis.spaceBottom = 15f // Beri ruang napas di lantai grafik
                    yAxis.valueFormatter = object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String {
                            // Format jadi angka bulat aja di sumbu Y (tanpa desimal)
                            return value.toInt().toString()
                        }
                    }

                    // Kustomisasi Tampilan Umum
                    chart.axisRight.isEnabled = false // Matikan sumbu kanan
                    chart.description.isEnabled = false
                    chart.legend.isEnabled = true // Tetap tampilkan legenda warna

                    // Kasih jarak aman dari tepi layar
                    chart.setExtraOffsets(5f, 10f, 15f, 15f)

                    chart.animateX(1200)
                    chart.invalidate()
                }
            }
    }
}