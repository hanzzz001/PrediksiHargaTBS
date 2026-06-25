package com.meirini.tbs_prediction

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val topHeader = findViewById<LinearLayout>(R.id.topHeader)
        val ivLogout = findViewById<ImageView>(R.id.ivLogout)

        // ======================================================================
        // PENJAGA PINTU: BAJAK NAVIGASI BAWAAN (CEK ROLE REAL-TIME)
        // ======================================================================
        bottomNavigationView.setOnItemSelectedListener { item ->
            // Ambil data role terbaru dari SharedPreferences setiap kali tombol diklik
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val role = sharedPref.getString("role", "petani") ?: "petani"

            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(navController.graph.startDestinationId, false)
                .setLaunchSingleTop(true)
                .build()

            when (item.itemId) {
                // 1. TOMBOL DASHBOARD DIKLIK
                R.id.dashboardFragment -> {
                    // Gunakan equals dengan ignoreCase = true agar kebal huruf besar/kecil (Admin / admin)
                    if (role.equals("admin", ignoreCase = true)) {
                        navController.navigate(R.id.adminFragment, null, navOptions)
                    } else {
                        navController.navigate(R.id.dashboardFragment, null, navOptions)
                    }
                    return@setOnItemSelectedListener true
                }

                // 2. TOMBOL RIWAYAT DIKLIK
                R.id.historyFragment -> {
                    if (role.equals("admin", ignoreCase = true)) {
                        navController.navigate(R.id.riwayatAdminFragment, null, navOptions)
                    } else {
                        navController.navigate(R.id.historyFragment, null, navOptions)
                    }
                    return@setOnItemSelectedListener true
                }

                // 3. TOMBOL PROFIL DIKLIK
                R.id.profilFragment -> {
                    navController.navigate(R.id.profilFragment, null, navOptions)
                    return@setOnItemSelectedListener true
                }

                else -> return@setOnItemSelectedListener false
            }
        }

        // FUNGSI LOGOUT FIREBASE
        ivLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            // PENTING: Bersihkan SharedPreferences saat logout agar sisa role tidak terbawa ke akun berikutnya!
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            sharedPref.edit().clear().apply()

            Toast.makeText(this, "Berhasil Keluar", Toast.LENGTH_SHORT).show()

            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(navController.graph.id, true) // Hapus total stack halaman
                .build()

            navController.navigate(R.id.loginFragment, null, navOptions)
        }

        // ======================================================================
        // ATUR UTAMA: UPDATE MENU & HEADER SECARA DINAMIS (ANTI-BUG SESSION)
        // ======================================================================
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Ambil data role terbaru setiap kali fragmen berubah
            val sharedPref = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val role = sharedPref.getString("role", "petani") ?: "petani"

            // Ganti trik removeItem menjadi isVisible agar menu bisa muncul/hilang fleksibel pas ganti akun
            val menuRiwayat = bottomNavigationView.menu.findItem(R.id.historyFragment)
            if (role.equals("petani", ignoreCase = true)) {
                menuRiwayat?.isVisible = false // Hilangkan untuk petani
            } else {
                menuRiwayat?.isVisible = true  // Munculkan untuk admin
            }

            // Atur Sembunyi/Muncul Header Atas & Navbar
            when (destination.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.registerFragment -> {
                    bottomNavigationView.visibility = View.GONE
                    topHeader.visibility = View.GONE
                }
                R.id.riwayatAdminFragment, R.id.historyFragment, R.id.profilFragment -> {
                    bottomNavigationView.visibility = View.VISIBLE
                    topHeader.visibility = View.VISIBLE
                }
                else -> { // Dashboard Admin & Petani
                    bottomNavigationView.visibility = View.VISIBLE
                    topHeader.visibility = View.VISIBLE
                }
            }
        }
    }
}