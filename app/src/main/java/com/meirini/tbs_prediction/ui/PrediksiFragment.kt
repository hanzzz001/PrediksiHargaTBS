package com.meirini.tbs_prediction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.firestore.FirebaseFirestore
import com.meirini.tbs_prediction.R
import java.text.NumberFormat
import java.util.Locale

class PrediksiFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore

    // Variabel untuk nyimpan daftar harga dari Firebase
    private var daftarHargaResmi: List<HashMap<String, Any>> = emptyList()
    // Variabel untuk nyimpan harga yang lagi aktif dipilih
    private var hargaJualSaatIni: Double = 0.0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_prediksi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        // Bind UI FORM INPUT
        val spinnerUmur = view.findViewById<AutoCompleteTextView>(R.id.spinnerUmur)
        val etHargaOtomatis = view.findViewById<TextInputEditText>(R.id.etHargaOtomatis)
        val etTonase = view.findViewById<TextInputEditText>(R.id.etTonase)
        val etBiaya = view.findViewById<TextInputEditText>(R.id.etBiaya)
        val btnHitung = view.findViewById<Button>(R.id.btnHitungPendapatan)

        // Bind UI CARD HASIL NOTA
        val cardNotaHasil = view.findViewById<View>(R.id.cardNotaHasil)
        val tvNotaOmzet = view.findViewById<TextView>(R.id.tvNotaOmzet)
        val tvNotaBiaya = view.findViewById<TextView>(R.id.tvNotaBiaya)
        val boxStatusHasil = view.findViewById<LinearLayout>(R.id.boxStatusHasil)
        val tvLabelStatus = view.findViewById<TextView>(R.id.tvLabelStatus)
        val tvNominalBersih = view.findViewById<TextView>(R.id.tvNominalBersih)

        // 1. SETUP DROPDOWN UMUR
        val daftarUmur = arrayOf(
            "Umur 3 Tahun", "Umur 4 Tahun", "Umur 5 Tahun", "Umur 6 Tahun",
            "Umur 7 Tahun", "Umur 8 Tahun", "Umur 9 Tahun", "Umur 10-20 Tahun",
            "Umur 21 Tahun", "Umur 22 Tahun", "Umur 23 Tahun", "Umur 24 Tahun", "Umur 25 Tahun"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, daftarUmur)
        spinnerUmur.setAdapter(adapter)

        // 2. SEDOT DATA DARI FIREBASE DIAM-DIAM
        firestore.collection("Harga_Resmi").document("terkini").get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    daftarHargaResmi = doc.get("daftar_harga") as List<HashMap<String, Any>>

                    Toast.makeText(requireContext(), "Data Harga Resmi Berhasil Disinkronisasi!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Belum ada data harga dari Admin.", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Gagal mengambil data harga pasar.", Toast.LENGTH_SHORT).show()
            }

        // 3. LOGIKA KETIKA PETANI MILIH UMUR DI DROPDOWN
        spinnerUmur.setOnItemClickListener { parent, _, position, _ ->
            val pilihan = parent.getItemAtPosition(position).toString()
            var targetUmur = 0

            when (pilihan) {
                "Umur 3 Tahun" -> targetUmur = 3
                "Umur 4 Tahun" -> targetUmur = 4
                "Umur 5 Tahun" -> targetUmur = 5
                "Umur 6 Tahun" -> targetUmur = 6
                "Umur 7 Tahun" -> targetUmur = 7
                "Umur 8 Tahun" -> targetUmur = 8
                "Umur 9 Tahun" -> targetUmur = 9
                "Umur 10-20 Tahun" -> targetUmur = 10
                "Umur 21 Tahun" -> targetUmur = 21
                "Umur 22 Tahun" -> targetUmur = 22
                "Umur 23 Tahun" -> targetUmur = 23
                "Umur 24 Tahun" -> targetUmur = 24
                "Umur 25 Tahun" -> targetUmur = 25
            }

            if (targetUmur != 0 && daftarHargaResmi.isNotEmpty()) {
                val hargaItem = daftarHargaResmi.find { (it["umur"] as Long).toInt() == targetUmur }
                if (hargaItem != null) {
                    hargaJualSaatIni = (hargaItem["harga_tbs"] as Number).toDouble()
                    val formatRp = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
                    formatRp.maximumFractionDigits = 0
                    etHargaOtomatis.setText(formatRp.format(hargaJualSaatIni))
                }
            } else {
                etHargaOtomatis.setText("")
                hargaJualSaatIni = 0.0
            }

            // Sembunyikan hasil perhitungan kalau umurnya diganti
            cardNotaHasil.visibility = View.GONE
        }

        // 4. LOGIKA TOMBOL HITUNG
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        formatRupiah.maximumFractionDigits = 0

        btnHitung.setOnClickListener {
            if (hargaJualSaatIni == 0.0) {
                // Snackbar Pengganti Toast Biasa
                com.google.android.material.snackbar.Snackbar.make(view, "Pilih umur tanaman dulu ya!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tonaseStr = etTonase.text.toString().trim().replace(",", ".")
            val biayaStr = etBiaya.text.toString().trim().replace(",", ".")

            if (tonaseStr.isEmpty() || biayaStr.isEmpty()) {
                com.google.android.material.snackbar.Snackbar.make(view, "Tonase dan Biaya wajib diisi!", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tonase = tonaseStr.toDoubleOrNull() ?: 0.0
            val biayaOps = biayaStr.toDoubleOrNull() ?: 0.0

            // Rumus
            val pendapatanKotor = tonase * hargaJualSaatIni
            val pendapatanBersih = pendapatanKotor - biayaOps

            // TAMPILKAN KE CARD NOTA HASIL
            tvNotaOmzet.text = formatRupiah.format(pendapatanKotor)
            tvNotaBiaya.text = "- " + formatRupiah.format(biayaOps)

            if (pendapatanBersih >= 0) {
                boxStatusHasil.setBackgroundColor(android.graphics.Color.parseColor("#2E7D32"))
                tvLabelStatus.text = "ESTIMASI UNTUNG BERSIH"
                tvNominalBersih.text = formatRupiah.format(pendapatanBersih)
                tvNominalBersih.setTextColor(android.graphics.Color.parseColor("#2E7D32"))
            } else {
                boxStatusHasil.setBackgroundColor(android.graphics.Color.parseColor("#D32F2F"))
                tvLabelStatus.text = "ESTIMASI RUGI FINANSIAL"
                tvNominalBersih.text = formatRupiah.format(pendapatanBersih)
                tvNominalBersih.setTextColor(android.graphics.Color.parseColor("#D32F2F"))
            }

            // Munculkan Card-nya
            cardNotaHasil.visibility = View.VISIBLE

            // Otomatis gulir layar ke bawah biar Card hasilnya langsung kelihatan
            (view as? android.widget.ScrollView)?.post {
                view.fullScroll(View.FOCUS_DOWN)
            }

            // === KIRIM LOG KE ADMIN ===
            val logId = firestore.collection("Riwayat_Panen").document().id
            val dataLog = hashMapOf(
                "id" to logId,
                "namaPetani" to "Kelompok Tani Swadaya",
                "bruto" to tonase,
                "biaya" to biayaOps,
                "netto" to pendapatanBersih,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("Riwayat_Panen").document(logId).set(dataLog)
                .addOnSuccessListener {
                    val snackbar = com.google.android.material.snackbar.Snackbar.make(
                        view,
                        "Perhitungan Selesai & Dicatat!",
                        com.google.android.material.snackbar.Snackbar.LENGTH_LONG
                    )
                    // Ganti warna jadi hijau
                    snackbar.setBackgroundTint(android.graphics.Color.parseColor("#2E7D32"))

                    // Supaya pop-up nya ngambang di ATAS navbar bawah
                    val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavigationView)
                    if (bottomNav != null) {
                        snackbar.anchorView = bottomNav
                    }

                    snackbar.show()
                }
        }
    }
}