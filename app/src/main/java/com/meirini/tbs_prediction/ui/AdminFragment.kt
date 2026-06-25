package com.meirini.tbs_prediction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.meirini.tbs_prediction.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AdminFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: LogPanenAdapter
    private val logList = ArrayList<LogPanen>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()

        val etHargaCPO = view.findViewById<EditText>(R.id.etHargaCPO)
        val etHargaKernel = view.findViewById<EditText>(R.id.etHargaKernel)
        val etIndeksK = view.findViewById<EditText>(R.id.etIndeksK)
        val btnSimpanAdmin = view.findViewById<Button>(R.id.btnSimpanAdmin)
        val rvRiwayatAdmin = view.findViewById<RecyclerView>(R.id.rvRiwayatAdmin)

        // --- 1. SETUP TABEL LOG VALIDASI MASUK ---
        rvRiwayatAdmin.layoutManager = LinearLayoutManager(requireContext())
        adapter = LogPanenAdapter(logList)
        rvRiwayatAdmin.adapter = adapter

        // Jalankan fungsi sedot data log timbangan milik petani
        loadLogTimbanganPetani()

        // --- 2. AKSI TOMBOL SIMPAN UPDATE PARAMETER ---
        btnSimpanAdmin.setOnClickListener {
            // FILTER OTOMATIS: Ganti koma jadi titik biar mesin ML ngga crash
            val cpoStr = etHargaCPO.text.toString().trim().replace(",", ".")
            val kernelStr = etHargaKernel.text.toString().trim().replace(",", ".")
            val indeksKStr = etIndeksK.text.toString().trim().replace(",", ".")

            if (cpoStr.isEmpty() || kernelStr.isEmpty() || indeksKStr.isEmpty()) {
                Toast.makeText(requireContext(), "Isi 3 parameter pasar dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Ubah string yang udah bersih tadi ke desimal (Double)
            val cpoVal = cpoStr.toDoubleOrNull() ?: 0.0
            val kernelVal = kernelStr.toDoubleOrNull() ?: 0.0
            val indeksKVal = indeksKStr.toDoubleOrNull() ?: 0.0

            // 1. Setup Retrofit
            val retrofit = retrofit2.Retrofit.Builder()
                .baseUrl("https://mantra-wipe-living.ngrok-free.dev/")
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(com.meirini.tbs_prediction.network.ApiService::class.java)

            // 2. Bungkus inputan Admin yang udah jadi desimal murni
            val requestData = com.meirini.tbs_prediction.network.TbsRequest(
                cpo = cpoVal,
                kernel = kernelVal,
                indeksK = indeksKVal
            )

            // 3. Tembak ke API Flask!
            apiService.getPrediksiTbs(requestData).enqueue(object : retrofit2.Callback<com.meirini.tbs_prediction.network.TbsResponse> {
                override fun onResponse(call: retrofit2.Call<com.meirini.tbs_prediction.network.TbsResponse>, response: retrofit2.Response<com.meirini.tbs_prediction.network.TbsResponse>) {
                    if (response.isSuccessful && response.body() != null) {
                        val balasan = response.body()!!

                        if (balasan.status == "success") {
                            // API Sukses! Ubah format jawaban API biar cocok sama bentuk array Firebase
                            val daftarHargaAsli = balasan.dataPrediksi.map {
                                hashMapOf(
                                    "umur" to it.umurTanaman,
                                    "harga_tbs" to it.hargaTbs
                                )
                            }

                            val tanggalUpdate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

                            val paketDataResmi = hashMapOf(
                                "tanggal_update" to tanggalUpdate,
                                "cpo_global" to cpoVal,
                                "kernel_global" to kernelVal,
                                "indeks_k" to indeksKVal,
                                "daftar_harga" to daftarHargaAsli
                            )

                            // Dorong hasil murni AI ke Firebase!
                            firestore.collection("Harga_Resmi").document("terkini")
                                .set(paketDataResmi)
                                .addOnSuccessListener {

                                    // ==========================================
                                    // TAMBAHAN: Catat juga ke dalam "Buku Sejarah" (Riwayat_Update)
                                    // ==========================================
                                    val dataRiwayat = paketDataResmi.toMutableMap()
                                    dataRiwayat["timestamp"] = System.currentTimeMillis()

                                    firestore.collection("Riwayat_Update").add(dataRiwayat)
                                    // ==========================================

                                    Toast.makeText(requireContext(), "SUKSES! AI Selesai Menghitung & Data Di-Update.", Toast.LENGTH_LONG).show()
                                    etIndeksK.text.clear()
                                    etHargaCPO.text.clear()
                                    etHargaKernel.text.clear()
                                }
                        }
                    } else {
                        // Minta Android membongkar isi pesan error dari server
                        val isiError = response.errorBody()?.string() ?: "Error tak terbaca"
                        Toast.makeText(requireContext(), "Laporan Server: $isiError", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.meirini.tbs_prediction.network.TbsResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Server AI Mati / HP tidak satu WiFi: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    // FUNGSI MENARIK LOG DATA PANEN PETANI DARI FIREBASE (TETAP SAMA UNTUK DASHBOARD)
    private fun loadLogTimbanganPetani() {
        firestore.collection("Riwayat_Panen")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->

                // SABUK PENGAMAN: Kalau fragment udah mati/pindah halaman (misal karena logout), hentikan proses ini!
                if (!isAdded || context == null) {
                    return@addSnapshotListener
                }

                if (e != null) {
                    Toast.makeText(requireContext(), "Gagal memuat log: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    logList.clear()
                    for (doc in snapshots) {
                        val log = LogPanen(
                            id = doc.getString("id") ?: "",
                            namaPetani = doc.getString("namaPetani") ?: "",
                            bruto = doc.getDouble("bruto") ?: 0.0,
                            biaya = doc.getDouble("biaya") ?: 0.0,
                            netto = doc.getDouble("netto") ?: 0.0,
                            timestamp = doc.getLong("timestamp") ?: 0L
                        )
                        logList.add(log)
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}