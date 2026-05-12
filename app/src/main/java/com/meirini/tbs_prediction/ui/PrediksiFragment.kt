package com.meirini.tbs_prediction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meirini.tbs_prediction.R
import java.text.SimpleDateFormat
import java.util.*

class PrediksiFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var hasilTerakhir: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_prediksi, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val tvHargaPasar = view.findViewById<TextView>(R.id.tvHargaPasar)
        val etUmur = view.findViewById<EditText>(R.id.etUmurTanaman)
        val etIndeksK = view.findViewById<EditText>(R.id.etIndeksK)
        val etCPO = view.findViewById<EditText>(R.id.etHargaCPO)
        val btnHitung = view.findViewById<Button>(R.id.btnHitung)
        val btnSimpan = view.findViewById<Button>(R.id.btnSimpan)
        val layoutHasil = view.findViewById<LinearLayout>(R.id.layoutHasil)
        val tvHasilPrediksi = view.findViewById<TextView>(R.id.tvHasilPrediksi)

        // 1. Ambil Harga Pasaran dari Admin (Firestore)
        firestore.collection("settings").document("harga_pasar")
            .get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    tvHargaPasar.text = "Rp ${doc.getLong("harga")} / Kg"
                }
            }

        // 2. Logika Hitung (Placeholder Machine Learning)
        btnHitung.setOnClickListener {
            val umur = etUmur.text.toString()
            val indeks = etIndeksK.text.toString()
            val cpo = etCPO.text.toString()

            if (umur.isEmpty() || indeks.isEmpty() || cpo.isEmpty()) {
                Toast.makeText(requireContext(), "Isi semua kolom dulu, Bang!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // SIMULASI MODEL ML (Nanti ganti dengan panggil API/TFLite)
            // Contoh rumus sederhana: (CPO * IndeksK) / 100 - (Umur * 10)
            hasilTerakhir = (cpo.toInt() * indeks.toDouble() / 100).toInt()

            tvHasilPrediksi.text = "Rp $hasilTerakhir"
            layoutHasil.visibility = View.VISIBLE
        }

        // 3. Simpan ke Riwayat
        btnSimpan.setOnClickListener {
            val userId = auth.currentUser?.uid
            val date = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault()).format(Date())

            val historyData = hashMapOf(
                "userId" to userId,
                "tanggal" to date,
                "umurTanaman" to etUmur.text.toString(),
                "indeksK" to etIndeksK.text.toString(),
                "hargaCPO" to etCPO.text.toString(),
                "hasilPrediksi" to hasilTerakhir
            )

            if (userId != null) {
                firestore.collection("riwayat_prediksi").add(historyData)
                    .addOnSuccessListener {
                        Toast.makeText(requireContext(), "Berhasil simpan ke Riwayat!", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Gagal simpan!", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}