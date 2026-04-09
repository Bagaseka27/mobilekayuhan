package com.example.kayuhan

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.FragmentTransaction
import com.example.kayuhan.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {
    lateinit var binding: ActivityMainBinding
    lateinit var fragmentLokasi: FragmentLokasi

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.bottomNavigationView.setOnItemSelectedListener(this)
        binding.bottomNavigationView.itemIconTintList = null
        fragmentLokasi = FragmentLokasi()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.itemBeranda -> {
                // Tangani klik Beranda
                return true
            }
            R.id.itemKaryawan -> {
                // Tangani klik Karyawan
                return true
            }
            R.id.itemMenu -> {
                // Tangani klik Menu
                return true
            }
            R.id.itemLokasi -> {
                supportFragmentManager.beginTransaction()
                    .replace(binding.frameLayout.id, fragmentLokasi) // Pastikan ID container sesuai di layout
                    .commit()
                return true
            }
        }
        return false
    }
}