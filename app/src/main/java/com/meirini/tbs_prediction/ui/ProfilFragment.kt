package com.meirini.tbs_prediction.ui

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.meirini.tbs_prediction.R

class ProfilFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profil, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvRole = view.findViewById<TextView>(R.id.tvProfilRole)
        val tvEmail = view.findViewById<TextView>(R.id.tvProfilEmail)
        val tvInisial = view.findViewById<TextView>(R.id.tvInisial)

        // Bind ID baru, buang ID tombol yang udah dihapus di XML
        val btnPusatBantuan = view.findViewById<LinearLayout>(R.id.btnPusatBantuan)
        val btnPanduan = view.findViewById<LinearLayout>(R.id.btnPanduan)
        val btnLogout = view.findViewById<LinearLayout>(R.id.btnLogout)

        // 1. Ambil Email dari Firebase
        val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "email@domain.com"
        tvEmail.text = userEmail

        // Bikin Inisial Otomatis dari 2 huruf pertama Email
        if (userEmail.length >= 2) {
            tvInisial.text = userEmail.substring(0, 2).uppercase()
        }

        // 2. Ambil Role dari SharedPreferences
        val sharedPref = requireActivity().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
        val role = sharedPref.getString("role", "petani")

        if (role == "admin") {
            tvRole.text = "Administrator Pusat"
        } else {
            tvRole.text = "Petani Kelapa Sawit"
        }

        // 3. AKSI TOMBOL-TOMBOL MENU

        // Aksi Pop-up Pusat Bantuan
        btnPusatBantuan.setOnClickListener {
            val pesanBantuan = """
                Jika Anda mengalami kendala saat menggunakan aplikasi atau menemukan ketidaksesuaian data harga TBS, silakan hubungi Layanan Admin kami melalui:
                
                WhatsApp : 0812-3456-7890
                Email : admin.tbs@disbun.riau.go.id
                
                Jam Operasional: Senin - Jumat (08.00 - 16.00 WIB)
            """.trimIndent()

            AlertDialog.Builder(requireContext())
                .setTitle("Pusat Bantuan")
                .setMessage(pesanBantuan)
                .setIcon(android.R.drawable.ic_dialog_info)
                .setPositiveButton("TUTUP") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        btnPanduan.setOnClickListener {
            Toast.makeText(requireContext(), "Portal Prediksi TBS v1.0.0", Toast.LENGTH_SHORT).show()
        }

        // FUNGSI LOGOUT (Pindahan dari MainActivity)
        btnLogout.setOnClickListener {
            // Bersihkan memori HP
            sharedPref.edit().clear().apply()

            // Logout Firebase
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(requireContext(), "Berhasil Keluar", Toast.LENGTH_SHORT).show()

            // Buang semua riwayat halaman dan kembali ke Login
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true) // Pastikan ID ini sesuai dengan ID awal graf navigasimu
                .build()

            findNavController().navigate(R.id.loginFragment, null, navOptions)
        }
    }
}