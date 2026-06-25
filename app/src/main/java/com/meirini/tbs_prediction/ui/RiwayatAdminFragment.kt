package com.meirini.tbs_prediction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.meirini.tbs_prediction.R

class RiwayatAdminFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapter: RiwayatUpdateAdapter
    private val riwayatList = ArrayList<DataRiwayatUpdate>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Asumsi kamu masih pakai fragment_riwayat_admin.xml yang kita buat sebelumnya
        return inflater.inflate(R.layout.fragment_riwayat_admin, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        val rvRiwayat = view.findViewById<RecyclerView>(R.id.rvRiwayatAdminFull)

        rvRiwayat.layoutManager = LinearLayoutManager(requireContext())
        adapter = RiwayatUpdateAdapter(riwayatList)
        rvRiwayat.adapter = adapter

        loadRiwayatUpdate()
    }

    private fun loadRiwayatUpdate() {
        // Ambil data dari koleksi baru yang kita bikin di AdminFragment
        firestore.collection("Riwayat_Update")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshots, e ->

                if (!isAdded || context == null) return@addSnapshotListener

                if (e != null) {
                    Toast.makeText(requireContext(), "Gagal memuat: ${e.message}", Toast.LENGTH_SHORT).show()
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    riwayatList.clear()
                    for (doc in snapshots) {
                        // Tarik data dengan aman, kasih default value kalau null
                        val cpo = doc.getLong("cpo_global")?.toInt() ?: 0
                        val kernel = doc.getLong("kernel_global")?.toInt() ?: 0
                        val indeks = doc.getDouble("indeks_k") ?: 0.0
                        val tanggal = doc.getString("tanggal_update") ?: "Waktu tidak diketahui"
                        val timestamp = doc.getLong("timestamp") ?: 0L

                        riwayatList.add(DataRiwayatUpdate(tanggal, cpo, kernel, indeks, timestamp))
                    }
                    adapter.notifyDataSetChanged()
                }
            }
    }
}