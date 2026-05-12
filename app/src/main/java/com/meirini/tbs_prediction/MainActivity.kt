package com.meirini.tbs_prediction

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val topHeader = findViewById<LinearLayout>(R.id.topHeader)
        val ivLogout = findViewById<ImageView>(R.id.ivLogout)

        // Sambungkan Navigasi bawah
        bottomNavigationView.setupWithNavController(navController)

        // FUNGSI LOGOUT FIREBASE
        ivLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Toast.makeText(this, "Berhasil Keluar", Toast.LENGTH_SHORT).show()

            // Cara paling ampuh hapus SEMUA riwayat halaman sebelumnya
            val navOptions = androidx.navigation.NavOptions.Builder()
                .setPopUpTo(navController.graph.id, true) // Hapus graf navigasi dari akar
                .build()

            navController.navigate(R.id.loginFragment, null, navOptions)
        }

        // Atur Sembunyi/Muncul Header dan Navbar
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.splashFragment, R.id.loginFragment, R.id.registerFragment -> {
                    bottomNavigationView.visibility = View.GONE
                    topHeader.visibility = View.GONE // Header sembunyi di form login
                }
                else -> {
                    bottomNavigationView.visibility = View.VISIBLE
                    topHeader.visibility = View.VISIBLE // Header muncul di Dashboard
                }
            }
        }
    }
}