package com.example.kayuhan

import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.kayuhan.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    lateinit var db: SQLiteDatabase
    lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBOpenHelper(this).writableDatabase

        val adapter = MainPagerAdapter(this)
        binding.viewPagerMain.adapter = adapter
        
        // Agar ViewPager berpindah saat BottomNav diklik
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.itemBeranda -> binding.viewPagerMain.currentItem = 0
                R.id.itemTransaksi -> binding.viewPagerMain.currentItem = 1
                R.id.itemKaryawan -> binding.viewPagerMain.currentItem = 2
                R.id.itemMenu -> binding.viewPagerMain.currentItem = 3
                R.id.itemLokasi -> binding.viewPagerMain.currentItem = 4
            }
            true
        }

        // Agar BottomNav berpindah saat ViewPager di-slide
        binding.viewPagerMain.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.bottomNavigationView.menu.getItem(position).isChecked = true
            }
        })

        binding.bottomNavigationView.itemIconTintList = null
    }

    fun getDbObject(): SQLiteDatabase = db

    inner class MainPagerAdapter(fa: AppCompatActivity) : FragmentStateAdapter(fa) {
        override fun getItemCount(): Int = 5
        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> FragmentDashboardAdmin()
                1 -> FragmentTransaksi()
                2 -> FragmentKaryawan()
                3 -> FragmentMenu()
                4 -> FragmentLokasi()
                else -> FragmentDashboardAdmin()
            }
        }
    }
}