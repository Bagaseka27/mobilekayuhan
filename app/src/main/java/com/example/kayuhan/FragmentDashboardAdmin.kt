package com.example.kayuhan

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.kayuhan.databinding.ActivityFragmentDashboardAdminBinding
import java.text.NumberFormat
import java.util.Locale

class FragmentDashboardAdmin : Fragment() {

    private var vb: ActivityFragmentDashboardAdminBinding? = null
    private val binding get() = vb!!

    lateinit var thisParent: MainActivity
    lateinit var db: SQLiteDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inisialisasi parent dan database
        thisParent = activity as MainActivity
        db = thisParent.getDbObject()

        // Inflate layout menggunakan View Binding
        vb = ActivityFragmentDashboardAdminBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        // Panggil fungsi untuk mengisi data setiap kali fragment tampil
        loadDashboardStats()
    }

    private fun loadDashboardStats() {
        // 1. Hitung Jumlah Karyawan
        val cKaryawan: Cursor = db.rawQuery("SELECT COUNT(*) FROM karyawan", null)
        if (cKaryawan.moveToFirst()) {
            binding.tvJumlahKaryawan.text = cKaryawan.getInt(0).toString()
        }
        cKaryawan.close()

        // 2. Hitung Total Gaji (dari tabel jabatan)
        val cGaji: Cursor = db.rawQuery("SELECT SUM(gaji_pokok_per_hari) FROM jabatan", null)
        if (cGaji.moveToFirst()) {
            binding.tvTotalGaji.text = formatKeRupiah(cGaji.getDouble(0))
        }
        cGaji.close()

        // 3. Hitung Jumlah Menu
        val cMenu: Cursor = db.rawQuery("SELECT COUNT(*) FROM menu", null)
        if (cMenu.moveToFirst()) {
            binding.tvJumlahMenu.text = cMenu.getInt(0).toString()
        }
        cMenu.close()

        // 4. Hitung Omset (dari tabel transaksi)
        val cOmset: Cursor = db.rawQuery("SELECT SUM(total_bayar) FROM transaksi", null)
        if (cOmset.moveToFirst()) {
            binding.tvTotalOmset.text = formatKeRupiah(cOmset.getDouble(0))
        }
        cOmset.close()
    }

    private fun formatKeRupiah(angka: Double): String {
        val localeID = Locale("in", "ID")
        val format = NumberFormat.getCurrencyInstance(localeID)
        return format.format(angka).replace(",00", "")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        vb = null // Penting untuk mencegah memory leak
    }
}