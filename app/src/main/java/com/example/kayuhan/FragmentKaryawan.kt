package com.example.kayuhan

import android.app.Dialog
import android.content.ContentValues
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.kayuhan.databinding.*
import com.google.android.material.tabs.TabLayoutMediator

// ─── DATA CLASSES ──────────────────────────────────────────────────────────

data class Karyawan(
    val email: String, val idJabatan: Int, val idRombong: String,
    val nama: String, val noHp: String, val posisi: String
)

data class Jabatan(val id: Int, val nama: String, val gaji: Long, val bonus: Long)

data class DataGaji(
    val idGaji: String, val nama: String, val periode: String,
    val gajiPokok: Long, val bonus: Long, val kompensasi: Long, val total: Long
)

data class JadwalShift(
    val idJadwal: String, val nama: String, val lokasi: String,
    val tanggal: String, val jam: String
)

// ─── MAIN FRAGMENT (CONTAINER) ─────────────────────────────────────────────

class FragmentKaryawan : Fragment() {
    private var _binding: ActivityFragmentKaryawanBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = ActivityFragmentKaryawanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.viewPager.adapter = KaryawanPagerAdapter(requireActivity())
        binding.viewPager.isUserInputEnabled = false

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, pos ->
            tab.text = when (pos) {
                0 -> "Data Karyawan"; 1 -> "Data Gaji"; 2 -> "Jadwal Shift"; 3 -> "Jabatan"; else -> ""
            }
        }.attach()
    }
}

class KaryawanPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount() = 4
    override fun createFragment(pos: Int): Fragment = when (pos) {
        0 -> TabDataKaryawanFragment()
        1 -> TabDataGajiFragment()
        2 -> TabJadwalShiftFragment()
        3 -> TabJabatanFragment()
        else -> TabDataKaryawanFragment()
    }
}

// ─── TAB 1: DATA KARYAWAN ──────────────────────────────────────────────────

class TabDataKaryawanFragment : Fragment() {
    private var _binding: TabDataKaryawanBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DBOpenHelper
    private val listBarista = mutableListOf<Karyawan>()
    private lateinit var adapter: KaryawanAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabDataKaryawanBinding.inflate(inflater, container, false)
        db = DBOpenHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = KaryawanAdapter(listBarista)
        binding.rvKaryawanBarista.layoutManager = LinearLayoutManager(requireContext())
        binding.rvKaryawanBarista.adapter = adapter

        loadData()
        binding.btnTambahKaryawan.setOnClickListener { showKaryawanDialog(null) }
    }

    private fun loadData() {
        listBarista.clear()
        // Filter: Admin jangan muncul di sini
        val cursor = db.readableDatabase.rawQuery("SELECT * FROM karyawan WHERE posisi != 'Admin'", null)
        while (cursor.moveToNext()) {
            listBarista.add(Karyawan(cursor.getString(0), cursor.getInt(1), cursor.getString(2),
                cursor.getString(3), cursor.getString(4), cursor.getString(5)))
        }
        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun showKaryawanDialog(item: Karyawan?) {
        val dialog = Dialog(requireContext())
        val d = DialogKelolaKaryawanBinding.inflate(layoutInflater)
        dialog.setContentView(d.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        // Spinner Jabatan Ambil dari DB
        val jabNames = mutableListOf<String>()
        val jabIds = mutableListOf<Int>()
        val curJab = db.readableDatabase.rawQuery("SELECT id_jabatan, nama_jabatan FROM jabatan", null)
        while(curJab.moveToNext()){
            jabIds.add(curJab.getInt(0))
            jabNames.add(curJab.getString(1))
        }
        curJab.close()
        d.spinnerJabatan.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, jabNames))
        d.spinnerRole.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("Senior", "Junior", "Admin")))

        item?.let {
            d.etEmail.setText(it.email); d.etEmail.isEnabled = false
            d.etNamaLengkap.setText(it.nama)
            d.etNoHp.setText(it.noHp)
        }

        d.btnSimpan.setOnClickListener {
            val selPos = jabNames.indexOf(d.spinnerJabatan.text.toString())
            val idJab = if(selPos != -1) jabIds[selPos] else 0

            val v = ContentValues().apply {
                put("email", d.etEmail.text.toString())
                put("nama", d.etNamaLengkap.text.toString())
                put("no_hp", d.etNoHp.text.toString())
                put("posisi", d.spinnerRole.text.toString())
                put("id_jabatan", idJab)
                put("id_rombong", d.spinnerRombong.text.toString())
            }
            if (item == null) db.writableDatabase.insert("karyawan", null, v)
            else db.writableDatabase.update("karyawan", v, "email=?", arrayOf(item.email))
            loadData(); dialog.dismiss()
        }
        d.btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    inner class KaryawanAdapter(val list: List<Karyawan>) : RecyclerView.Adapter<KaryawanAdapter.VH>() {
        inner class VH(val b: ItemKaryawanBaristaBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(ItemKaryawanBaristaBinding.inflate(LayoutInflater.from(p.context), p, false))
        override fun onBindViewHolder(h: VH, pos: Int) {
            val i = list[pos]
            h.b.tvEmail.text = i.email
            h.b.tvNama.text = i.nama
            h.b.tvPosisi.text = i.posisi
            // Set Logo Kayuhan
            //h.b.ivProfilKaryawan.setImageResource(R.drawable.logo_kayuhan)

            h.b.btnEdit.setOnClickListener { showKaryawanDialog(i) }
            h.b.btnHapus.setOnClickListener {
                db.writableDatabase.delete("karyawan", "email=?", arrayOf(i.email))
                loadData()
            }
        }
        override fun getItemCount() = list.size
    }
}

// ─── TAB 2: DATA GAJI ──────────────────────────────────────────────────────

class TabDataGajiFragment : Fragment() {
    private var _binding: TabDataGajiBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DBOpenHelper
    private val listGaji = mutableListOf<DataGaji>()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabDataGajiBinding.inflate(inflater, container, false)
        db = DBOpenHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.rvGaji.layoutManager = LinearLayoutManager(requireContext())
        loadGaji()
    }

    private fun loadGaji() {
        listGaji.clear()
        val cursor = db.readableDatabase.rawQuery("SELECT g.*, k.nama FROM gaji g JOIN karyawan k ON g.email = k.email", null)
        while (cursor.moveToNext()) {
            listGaji.add(DataGaji(cursor.getString(0), cursor.getString(7), cursor.getString(2),
                cursor.getLong(3), cursor.getLong(4), cursor.getLong(5), cursor.getLong(6)))
        }
        cursor.close()
        // Pasang adapter di sini...
    }
}

// ─── TAB 4: JABATAN ────────────────────────────────────────────────────────

class TabJabatanFragment : Fragment() {
    private var _binding: TabJabatanBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DBOpenHelper
    private val listJabatan = mutableListOf<Jabatan>()
    private lateinit var adapter: JabatanAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabJabatanBinding.inflate(inflater, container, false)
        db = DBOpenHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = JabatanAdapter(listJabatan)
        binding.rvJabatan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJabatan.adapter = adapter

        loadJabatan()
        binding.btnTambahJabatan.setOnClickListener { showJabatanDialog(null) }
    }

    private fun loadJabatan() {
        listJabatan.clear()
        val cursor = db.readableDatabase.rawQuery("SELECT * FROM jabatan", null)
        while (cursor.moveToNext()) {
            listJabatan.add(Jabatan(cursor.getInt(0), cursor.getString(1), cursor.getLong(2), cursor.getLong(3)))
        }
        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun showJabatanDialog(item: Jabatan?) {
        val dialog = Dialog(requireContext())
        val d = DialogKelolaJabatanBinding.inflate(layoutInflater)
        dialog.setContentView(d.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        item?.let {
            d.etNamaJabatan.setText(it.nama)
            d.etGajiPokok.setText(it.gaji.toString())
            d.etBonusCup.setText(it.bonus.toString())
        }

        d.btnSimpan.setOnClickListener {
            val v = ContentValues().apply {
                put("nama_jabatan", d.etNamaJabatan.text.toString())
                put("gaji_per_hari", d.etGajiPokok.text.toString().toLongOrNull() ?: 0L)
                put("bonus_per_cup", d.etBonusCup.text.toString().toLongOrNull() ?: 0L)
            }
            if (item == null) db.writableDatabase.insert("jabatan", null, v)
            else db.writableDatabase.update("jabatan", v, "id_jabatan=?", arrayOf(item.id.toString()))
            loadJabatan(); dialog.dismiss()
        }
        d.btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    inner class JabatanAdapter(val list: List<Jabatan>) : RecyclerView.Adapter<JabatanAdapter.VH>() {
        inner class VH(val b: ItemJabatanBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(ItemJabatanBinding.inflate(LayoutInflater.from(p.context), p, false))
        override fun onBindViewHolder(h: VH, pos: Int) {
            val i = list[pos]
            h.b.tvNamaJabatan.text = i.nama
            h.b.tvGajiHari.text = "Rp ${i.gaji}"
            h.b.btnEdit.setOnClickListener { showJabatanDialog(i) }
            h.b.btnHapus.setOnClickListener {
                db.writableDatabase.delete("jabatan", "id_jabatan=?", arrayOf(i.id.toString()))
                loadJabatan()
            }
        }
        override fun getItemCount() = list.size
    }
}

// Tambahkan logika serupa untuk TabJadwalShiftFragment menggunakan DBOpenHelper
class TabJadwalShiftFragment : Fragment() {
    // Mirip dengan TabJabatan, gunakan table 'jadwal'
}