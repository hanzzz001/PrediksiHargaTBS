package com.meirini.tbs_prediction.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.meirini.tbs_prediction.R

class LoginFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val etLoginEmail = view.findViewById<EditText>(R.id.etUsername)
        val etLoginPassword = view.findViewById<EditText>(R.id.etPassword)
        val btnLogin = view.findViewById<Button>(R.id.btnLogin)
        val tvRegister = view.findViewById<TextView>(R.id.tvRegister)

        // Pindah ke halaman Daftar
        tvRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }

        // Aksi Tombol Login
        btnLogin.setOnClickListener {
            val email = etLoginEmail.text.toString().trim()
            val password = etLoginPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(requireContext(), "Email dan Password jangan dikosongkan, Bang!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Proses Login ke Firebase Auth (Cek Email & Password)
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {

                            // 2. Kalau berhasil masuk, baru kita baca Database Firestore buat ngecek Role
                            firestore.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    // Ambil role dari database, kalau kosong default ke "petani"
                                    val role = document.getString("role") ?: "petani"

                                    // =========================================================
                                    // Simpan role ke memori HP (SharedPreferences)
                                    // Semua dilowercase biar aman pas dicek di MainActivity
                                    // =========================================================
                                    val sharedPref = requireActivity().getSharedPreferences("UserPrefs", android.content.Context.MODE_PRIVATE)
                                    sharedPref.edit().putString("role", role.lowercase()).apply()
                                    // =========================================================

                                    // Siapkan opsi untuk menghapus halaman Login dari tumpukan memori
                                    val navOptions = androidx.navigation.NavOptions.Builder()
                                        .setPopUpTo(R.id.loginFragment, true)
                                        .build()

                                    // 3. Arahkan rute sesuai Role (KEBAL HURUF BESAR/KECIL)
                                    if (role.equals("admin", ignoreCase = true)) {
                                        Toast.makeText(requireContext(), "Selamat Datang Admin!", Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.action_loginFragment_to_adminFragment, null, navOptions)
                                    } else {
                                        Toast.makeText(requireContext(), "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                        findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment, null, navOptions)
                                    }
                                }
                                .addOnFailureListener {
                                    Toast.makeText(requireContext(), "Login Berhasil (Tanpa Role)!", Toast.LENGTH_SHORT).show()
                                    findNavController().navigate(R.id.action_loginFragment_to_dashboardFragment)
                                }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Login Gagal: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}