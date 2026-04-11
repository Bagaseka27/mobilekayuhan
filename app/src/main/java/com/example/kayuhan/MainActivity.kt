package com.example.kayuhan
import DBOpenHelper
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

    lateinit var ft: FragmentTransaction

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db = DBOpenHelper(this).writableDatabase
        fragmentLokasi = FragmentLokasi()
        fragmentDashboardAdmin = FragmentDashboardAdmin()

        binding.bottomNavigationView.setOnItemSelectedListener(this)
        binding.bottomNavigationView.itemIconTintList = null

        if (savedInstanceState == null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.frameLayout, fragmentDashboardAdmin).commit()
        }

    }

    //memberikan akses database ke class lain
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