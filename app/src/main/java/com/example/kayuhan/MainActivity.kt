package com.example.kayuhan

import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentTransaction
import com.example.kayuhan.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView

class MainActivity : AppCompatActivity(), NavigationBarView.OnItemSelectedListener {

    lateinit var db: SQLiteDatabase
    lateinit var binding: ActivityMainBinding

    lateinit var fragmentLokasi: FragmentLokasi
    lateinit var fragmentDashboardAdmin: FragmentDashboardAdmin
    lateinit var fragmentTransaksi: FragmentTransaksi
    
    lateinit var ft: FragmentTransaction

    lateinit var fragmentMenu: FragmentMenu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBOpenHelper(this).writableDatabase

        fragmentLokasi = FragmentLokasi()
        fragmentDashboardAdmin = FragmentDashboardAdmin()

        fragmentMenu = FragmentMenu()

        fragmentTransaksi = FragmentTransaksi()


        binding.bottomNavigationView.setOnItemSelectedListener(this)
        binding.bottomNavigationView.itemIconTintList = null

        if (savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragmentDashboardAdmin)
                .commit()
        }
    }

    // akses database
    fun getDbObject() : SQLiteDatabase{
        return db
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {

            R.id.itemBeranda -> {
                ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.frameLayout, fragmentDashboardAdmin).commit()
                binding.frameLayout.setBackgroundColor(
                    Color.argb(245, 255, 255, 225)
                )
                binding.frameLayout.visibility = View.VISIBLE
                return true
            }

            R.id.itemTransaksi -> {
                ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.frameLayout, fragmentTransaksi).commit()
                return true
            }

            R.id.itemKaryawan -> {
                return true
            }

            R.id.itemMenu -> {
                ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.frameLayout, fragmentMenu).commit()
                binding.frameLayout.visibility = View.VISIBLE

                return true
            }

            R.id.itemLokasi -> {
                ft = supportFragmentManager.beginTransaction()
                ft.replace(R.id.frameLayout, fragmentLokasi).commit()
                return true
            }
        }
        return false
    }
}