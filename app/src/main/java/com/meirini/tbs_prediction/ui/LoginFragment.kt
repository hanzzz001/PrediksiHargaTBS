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

            // Proses Login ke Firebase
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = auth.currentUser?.uid
                        if (userId != null) {
                            // Cek Role di Firestore (Admin atau Petani)
                            firestore.collection("users").document(userId).get()
                                .addOnSuccessListener { document ->
                                    val role = document.getString("role")

                                    // Siapkan opsi untuk menghapus halaman Login dari memori (biar ngga bocor pas di-back)
                                    val navOptions = androidx.navigation.NavOptions.Builder()
                                        .setPopUpTo(R.id.loginFragment, true)
                                        .build()

                                    if (role == "Admin") {
                                        Toast.makeText(requireContext(), "Selamat Datang Admin!", Toast.LENGTH_SHORT).show()
                                        // Lempar ke halaman Admin
                                        findNavController().navigate(R.id.action_loginFragment_to_adminFragment, null, navOptions)
                                    } else {
                                        Toast.makeText(requireContext(), "Login Berhasil!", Toast.LENGTH_SHORT).show()
                                        // Lempar ke halaman Dashboard Petani
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