package com.meirini.tbs_prediction.ui

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
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

        // Bind UI
        val spinnerUmur = view.findViewById<Spinner>(R.id.spinnerUmur)
        val etHargaJualTBS = view.findViewById<EditText>(R.id.etHargaJualTBS)
        val etTonase = view.findViewById<EditText>(R.id.etTonase)
        val etBiayaOps = view.findViewById<EditText>(R.id.etBiayaOps)
        val btnHitungPendapatan = view.findViewById<Button>(R.id.btnHitungPendapatan)

        // Bind UI Nota Hasil
        val cardNotaHasil = view.findViewById<View>(R.id.cardNotaHasil)
        val tvNotaOmzet = view.findViewById<TextView>(R.id.tvNotaOmzet)
        val tvNotaBiaya = view.findViewById<TextView>(R.id.tvNotaBiaya)
        val boxStatusHasil = view.findViewById<LinearLayout>(R.id.boxStatusHasil)
        val tvLabelStatus = view.findViewById<TextView>(R.id.tvLabelStatus)
        val tvNominalBersih = view.findViewById<TextView>(R.id.tvNominalBersih)

        cardNotaHasil.visibility = View.GONE

        // 1. SETUP DROPDOWN UMUR
        val daftarUmur = arrayOf("Pilih Umur...",
            "Umur 3 Tahun", "Umur 4 Tahun", "Umur 5 Tahun", "Umur 6 Tahun",
            "Umur 7 Tahun", "Umur 8 Tahun", "Umur 9 Tahun", "Umur 10-20 Tahun",
            "Umur 21 Tahun", "Umur 22 Tahun", "Umur 23 Tahun", "Umur 24 Tahun", "Umur 25 Tahun"
        )
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, daftarUmur)
        spinnerUmur.adapter = adapter

        // 2. SEDOT DATA DARI FIREBASE DIAM-DIAM
        firestore.collection("Harga_Resmi").document("terkini").get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    // Ambil array daftar_harga dari Firebase
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
        spinnerUmur.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val pilihan = daftarUmur[position]
                var targetUmur = 0

                // Konversi teks dropdown jadi angka (Lengkap 13 umur)
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
                    // Cari harga yang umurnya cocok sama pilihan Petani
                    val hargaItem = daftarHargaResmi.find { (it["umur"] as Long).toInt() == targetUmur }
                    if (hargaItem != null) {
                        hargaJualSaatIni = (hargaItem["harga_tbs"] as Number).toDouble()
                        etHargaJualTBS.setText(hargaJualSaatIni.toString())
                    }
                } else {
                    etHargaJualTBS.setText("")
                    hargaJualSaatIni = 0.0
                }

                // Sembunyikan nota kalau umurnya diganti
                cardNotaHasil.visibility = View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 4. LOGIKA TOMBOL HITUNG
        val formatRupiah = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        formatRupiah.maximumFractionDigits = 0

        btnHitungPendapatan.setOnClickListener {
            if (hargaJualSaatIni == 0.0) {
                Toast.makeText(requireContext(), "Pilih umur tanaman dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tonaseStr = etTonase.text.toString()
            val biayaStr = etBiayaOps.text.toString()

            if (tonaseStr.isEmpty() || biayaStr.isEmpty()) {
                Toast.makeText(requireContext(), "Tonase dan Biaya wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val tonase = tonaseStr.toDouble()
            val biayaOps = biayaStr.toDouble()

            val pendapatanKotor = tonase * hargaJualSaatIni
            val pendapatanBersih = pendapatanKotor - biayaOps

            tvNotaOmzet.text = formatRupiah.format(pendapatanKotor)
            tvNotaBiaya.text = formatRupiah.format(biayaOps)

            if (pendapatanBersih >= 0) {
                boxStatusHasil.setBackgroundColor(Color.parseColor("#2E7D32"))
                tvLabelStatus.text = "ESTIMASI UNTUNG BERSIH"
                tvNominalBersih.text = formatRupiah.format(pendapatanBersih)
            } else {
                boxStatusHasil.setBackgroundColor(Color.parseColor("#D32F2F"))
                tvLabelStatus.text = "ESTIMASI RUGI FINANSIAL"
                tvNominalBersih.text = formatRupiah.format(pendapatanBersih)
            }

            cardNotaHasil.visibility = View.VISIBLE

            // === BAGIAN BARU: KIRIM LOG KE ADMIN ===
            val logId = firestore.collection("Riwayat_Panen").document().id
            val dataLog = hashMapOf(
                "id" to logId,
                "namaPetani" to "Kelompok Tani Swadaya", // Bisa dinamis jika ada data login user
                "bruto" to tonase,
                "biaya" to biayaOps,
                "netto" to pendapatanBersih,
                "timestamp" to System.currentTimeMillis()
            )

            firestore.collection("Riwayat_Panen").document(logId).set(dataLog)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Nota terkirim ke Log Validasi Admin!", Toast.LENGTH_SHORT).show()
                }
        }
    }
}