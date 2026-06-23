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
            val indeksK = etIndeksK.text.toString().trim()
            val hargaCPO = etHargaCPO.text.toString().trim()
            val hargaKernel = etHargaKernel.text.toString().trim()

            if (indeksK.isEmpty() || hargaCPO.isEmpty() || hargaKernel.isEmpty()) {
                Toast.makeText(requireContext(), "Isi 3 parameter pasar dulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Setup Retrofit
            val retrofit = retrofit2.Retrofit.Builder()
                .baseUrl("http://172.20.10.2:5000/") // Gunakan IP dari Terminal Flask-mu
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()

            val apiService = retrofit.create(com.meirini.tbs_prediction.network.ApiService::class.java)

            // 2. Bungkus 3 inputan Admin
            val requestData = com.meirini.tbs_prediction.network.TbsRequest(
                cpo = hargaCPO.toInt(),
                kernel = hargaKernel.toInt(),
                indeksK = indeksK.toDouble()
            )

            // 3. Tembak ke API Flask Mei!
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
                                "cpo_global" to hargaCPO.toInt(),
                                "kernel_global" to hargaKernel.toInt(),
                                "indeks_k" to indeksK.toDouble(),
                                "daftar_harga" to daftarHargaAsli
                            )

                            // Dorong hasil murni AI ke Firebase!
                            firestore.collection("Harga_Resmi").document("terkini")
                                .set(paketDataResmi)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "SUKSES! AI Selesai Menghitung & Data Di-Update.", Toast.LENGTH_LONG).show()
                                    etIndeksK.text.clear()
                                    etHargaCPO.text.clear()
                                    etHargaKernel.text.clear()
                                }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal membaca AI. Server membalas error.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<com.meirini.tbs_prediction.network.TbsResponse>, t: Throwable) {
                    Toast.makeText(requireContext(), "Server AI Mati / HP tidak satu WiFi: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }
    }

    // FUNGSI MENARIK LOG DATA PANEN PETANI DARI FIREBASE
    private fun loadLogTimbanganPetani() {
        firestore.collection("Riwayat_Panen")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->
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