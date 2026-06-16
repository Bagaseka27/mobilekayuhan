package com.example.kayuhan

import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.kayuhan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var db: SQLiteDatabase
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. AMBIL DATA SESI LOGIN DARI INTENT
        val roleLogin = intent.getStringExtra("EXTRA_ROLE")
        val emailLogin = intent.getStringExtra("EXTRA_EMAIL")

        // 2. LOGIKA ROUTING HALAMAN BERDASARKAN ROLE USER
        if (roleLogin.equals("barista", ignoreCase = true)) {
            val intentBarista = Intent(this, DashboardBaristaActivity::class.java).apply {
                putExtra("EXTRA_EMAIL", emailLogin)
                putExtra("EXTRA_ROLE", roleLogin)
            }
            startActivity(intentBarista)
            finish()
            return
        } else if (roleLogin.equals("admin", ignoreCase = true)) {
            Toast.makeText(this, "Selamat datang, Admin!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Sesi tidak valid, silakan login kembali.", Toast.LENGTH_LONG).show()
            val intentLogin = Intent(this, LoginActivity::class.java)
            startActivity(intentLogin)
            finish()
            return
        }

        // 3. JIKA USER ADALAH ADMIN, LANJUTKAN PROSES INISIALISASI DASHBOARD UTAMA ADMIN
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inisialisasi Database SQLite Lokal
        db = DBOpenHelper(this).writableDatabase

        // Inisialisasi ViewPager2 dengan Adapter internal (Sekarang isi 4 Fragment)
        val adapter = MainPagerAdapter(this)
        binding.viewPagerMain.adapter = adapter

        // Sinkronisasi klik item BottomNavigationView ke ViewPager2 atau Aksi Spesifik
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemBeranda -> {
                    binding.viewPagerMain.currentItem = 0
                    true
                }
                R.id.itemKaryawan -> {
                    binding.viewPagerMain.currentItem = 1
                    true
                }
                R.id.itemAbsensi -> {
                    // Routing langsung ke MonitoringAdminActivity sesuai request
                    val intentAbsensi = Intent(this, MonitoringAdminActivity::class.java)
                    startActivity(intentAbsensi)
                    false // Return false agar highlight bottom nav tidak stuck di Absensi setelah pindah Activity
                }
                R.id.itemMenu -> {
                    binding.viewPagerMain.currentItem = 2
                    true
                }
                R.id.itemMore -> {
                    // Tampilkan Popup Menu saat item "More" diklik
                    showMoreMenu()
                    false // Return false agar item More tidak terlihat 'terseleksi/aktif' secara permanen
                }
                else -> false
            }
        }

        // Sinkronisasi swipe / geser halaman ViewPager2 ke item BottomNavigationView
        binding.viewPagerMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                // Karena index 2 di ViewPager sekarang adalah Menu (Absensi melompat ke Activity lain)
                val menuIndex = when(position) {
                    0 -> 0 // Beranda
                    1 -> 1 // Karyawan
                    2 -> 3 // Menu (mengambil indeks itemMenu di BottomNav, yaitu ke-4/indeks 3)
                    else -> 0
                }
                binding.bottomNavigationView.menu.getItem(menuIndex).isChecked = true
            }
        })

        // Menonaktifkan auto-tint warna default Android
        binding.bottomNavigationView.itemIconTintList = null
    }

    // Fungsi untuk memunculkan Popup Menu "More"
    private fun showMoreMenu() {
        // Menggunakan anchor view dari item 'itemMore' itu sendiri agar posisinya pas di atas tombolnya
        val viewMore = binding.bottomNavigationView.findViewById<android.view.View>(R.id.itemMore)
        val popup = PopupMenu(this, viewMore)
        popup.menuInflater.inflate(R.menu.more_option_menu, popup.menu)

        popup.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.subLokasi -> {
                    // Jalankan fragment lokasi atau activity lokasi (Tetap/Sesuaikan kebutuhan)
                    // Jika ingin membuka FragmentLokasi, Anda bisa mengaturnya via Intent/pindah section.
                    Toast.makeText(this, "Membuka Lokasi", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.subTransaksi -> {
                    // Jalankan fragment transaksi atau activity transaksi (Tetap/Sesuaikan kebutuhan)
                    Toast.makeText(this, "Membuka Riwayat Transaksi", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.subLogout -> {
                    // Routing logout ke LoginActivity sesuai request
                    val intentLogout = Intent(this, LoginActivity::class.java)
                    // Clear task agar user tidak bisa menekan tombol 'back' kembali ke Main Activity
                    intentLogout.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intentLogout)
                    finish()
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    // Fungsi helper untuk membagikan objek database
    fun getDbObject(): SQLiteDatabase = db

    // Adapter Fragment yang disesuaikan menjadi 3 Fragment internal (Beranda, Karyawan, Menu)
    inner class MainPagerAdapter(fa: AppCompatActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 3
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> FragmentDashboardAdmin()
                1 -> FragmentKaryawan()
                2 -> FragmentMenu()
                else -> FragmentDashboardAdmin()
            }
        }
    }
}