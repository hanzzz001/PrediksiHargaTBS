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

class RegisterFragment : Fragment() {

    // Siapkan variabel untuk Firebase
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inisialisasi Firebase
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Kenalkan elemen-elemen dari desain XML tadi
        val etNamaLengkap = view.findViewById<EditText>(R.id.etNamaLengkap)
        val etRegUsername = view.findViewById<EditText>(R.id.etRegUsername)
        val etRegPassword = view.findViewById<EditText>(R.id.etRegPassword)
        val etRegPasswordConfirm = view.findViewById<EditText>(R.id.etRegPasswordConfirm) // Tambahan untuk Retype Password
        val btnRegister = view.findViewById<Button>(R.id.btnRegister)
        val tvBackToLogin = view.findViewById<TextView>(R.id.tvBackToLogin)

        // Tombol kembali ke Login (diperbaiki biar ngga lari ke Splash)
        tvBackToLogin.setOnClickListener {
            findNavController().popBackStack() // Perintah mutlak untuk mundur 1 halaman
        }

        // Aksi pas tombol Daftar ditekan
        btnRegister.setOnClickListener {
            val nama = etNamaLengkap.text.toString().trim()
            val email = etRegUsername.text.toString().trim()
            val password = etRegPassword.text.toString().trim()
            val confirmPassword = etRegPasswordConfirm.text.toString().trim() // Ambil nilai Retype Password

            // Cek kalau ada kolom yang kosong
            if (nama.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "Semua kolom wajib diisi, Bang!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Cek panjang password
            if (password.length < 6) {
                Toast.makeText(requireContext(), "Password minimal 6 karakter ya", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Validasi Retype Password
            if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Password tidak sama, Bang! Cek lagi.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Proses daftar ke Firebase Authentication
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Kalau sukses bikin akun, simpan data nama dan role ke Firestore
                        val userId = auth.currentUser?.uid
                        val userData = hashMapOf(
                            "namaLengkap" to nama,
                            "email" to email,
                            "role" to "Petani"
                        )

                        if (userId != null) {
                            firestore.collection("users").document(userId)
                                .set(userData)
                                .addOnSuccessListener {
                                    Toast.makeText(requireContext(), "Akun berhasil dibuat! Silakan Login.", Toast.LENGTH_LONG).show()
                                    findNavController().popBackStack() // Mundur ke halaman login pas sukses
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(requireContext(), "Gagal simpan data: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Gagal daftar: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }
}