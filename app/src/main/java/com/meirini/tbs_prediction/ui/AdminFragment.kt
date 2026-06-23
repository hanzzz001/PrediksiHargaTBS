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

            // 13 DATA MOCK (Sesuai output ML Mei)
            val listHargaMock = listOf(
                hashMapOf("umur" to 3, "harga_tbs" to 2450.0),
                hashMapOf("umur" to 4, "harga_tbs" to 2480.0),
                hashMapOf("umur" to 5, "harga_tbs" to 2510.0),
                hashMapOf("umur" to 6, "harga_tbs" to 2550.0),
                hashMapOf("umur" to 7, "harga_tbs" to 2600.0),
                hashMapOf("umur" to 8, "harga_tbs" to 2650.0),
                hashMapOf("umur" to 9, "harga_tbs" to 2700.0),
                hashMapOf("umur" to 10, "harga_tbs" to 2750.0), // Mewakili 10-20 Tahun
                hashMapOf("umur" to 21, "harga_tbs" to 2710.0), // Mulai turun harganya
                hashMapOf("umur" to 22, "harga_tbs" to 2680.0),
                hashMapOf("umur" to 23, "harga_tbs" to 2640.0),
                hashMapOf("umur" to 24, "harga_tbs" to 2590.0),
                hashMapOf("umur" to 25, "harga_tbs" to 2500.0)
            )

            val tanggalUpdate = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

            val paketDataResmi = hashMapOf(
                "tanggal_update" to tanggalUpdate,
                "cpo_global" to hargaCPO.toInt(),
                "kernel_global" to hargaKernel.toInt(),
                "indeks_k" to indeksK.toDouble(),
                "daftar_harga" to listHargaMock
            )

            firestore.collection("Harga_Resmi").document("terkini")
                .set(paketDataResmi)
                .addOnSuccessListener {
                    Toast.makeText(requireContext(), "Model AI Diupdate! Harga Resmi Tersimpan.", Toast.LENGTH_LONG).show()
                    etIndeksK.text.clear()
                    etHargaCPO.text.clear()
                    etHargaKernel.text.clear()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Gagal menyimpan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
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