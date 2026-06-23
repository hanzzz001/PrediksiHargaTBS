package com.meirini.tbs_prediction.ui // Pastikan package ini sesuai dengan milikmu

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
import com.meirini.tbs_prediction.R

class DashboardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Inisialisasi Tombol dan Grafik
        val btnGoToPrediksi = view.findViewById<Button>(R.id.btnGoToPrediksi)
        val lineChartTBS = view.findViewById<LineChart>(R.id.lineChartTBS)

        // 2. Setup Grafik MPAndroidChart
        setupGrafik(lineChartTBS)

        // 3. Aksi Tombol Mulai Prediksi
        btnGoToPrediksi.setOnClickListener {
            // Pastikan ID 'action_dashboardFragment_to_prediksiFragment' ini SAMA PERSIS
            // dengan yang ada di garis panah nav_graph.xml milikmu ya!
            findNavController().navigate(R.id.action_dashboardFragment_to_prediksiFragment)
        }
    }

    private fun setupGrafik(chart: LineChart) {
        // Data Dummy Harga TBS Seminggu Terakhir (Nanti diganti data asli dari Firebase/API)
        val entries = ArrayList<Entry>()
        entries.add(Entry(0f, 2500f)) // Hari 1
        entries.add(Entry(1f, 2550f)) // Hari 2
        entries.add(Entry(2f, 2600f)) // Hari 3
        entries.add(Entry(3f, 2580f)) // Hari 4
        entries.add(Entry(4f, 2650f)) // Hari 5
        entries.add(Entry(5f, 2700f)) // Hari 6
        entries.add(Entry(6f, 2750f)) // Hari 7

        // Desain Garisnya (Warna Hijau Sawit)
        val dataSet = LineDataSet(entries, "Harga TBS (Rp)")
        dataSet.color = Color.parseColor("#2E7D32")
        dataSet.valueTextColor = Color.BLACK
        dataSet.lineWidth = 3f
        dataSet.circleRadius = 5f
        dataSet.setCircleColor(Color.parseColor("#009688"))
        dataSet.setDrawFilled(true)
        dataSet.fillColor = Color.parseColor("#C8E6C9")
        dataSet.mode = LineDataSet.Mode.CUBIC_BEZIER // Biar garisnya melengkung mulus, ngga kaku

        val lineData = LineData(dataSet)
        chart.data = lineData

        // Kustomisasi Sumbu X (Bawah) jadi nama Hari
        val hari = arrayOf("Sen", "Sel", "Rab", "Kam", "Jum", "Sab", "Min")
        val xAxis = chart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(hari)
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f

        // Kustomisasi Tampilan Umum
        chart.axisRight.isEnabled = false // Matikan angka di sebelah kanan
        chart.description.isEnabled = false // Matikan teks deskripsi kecil di pojok
        chart.animateX(1000) // Animasi garis bergerak pas halaman dibuka
        chart.invalidate() // Refresh grafik
    }
}