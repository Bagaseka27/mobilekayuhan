package com.example.kayuhan

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.ContentValues
import android.os.Bundle
import java.util.*
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
    val idJadwal: Int, val email: String, val nama: String,
    val tanggal: String, val jamMulai: String, val jamSelesai: String
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
        // PERBAIKAN: Admin sekarang muncul (Filter posisi != 'Admin' dihapus)
        val cursor = db.readableDatabase.rawQuery("SELECT * FROM karyawan", null)
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

        // 1. Spinner Jabatan
        val jabNames = mutableListOf<String>()
        val jabIds = mutableListOf<Int>()
        val curJab = db.readableDatabase.rawQuery("SELECT id_jabatan, nama_jabatan FROM jabatan", null)
        while(curJab.moveToNext()){
            jabIds.add(curJab.getInt(0))
            jabNames.add(curJab.getString(1))
        }
        curJab.close()
        d.spinnerJabatan.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, jabNames))

        // 2. Role Logic (RadioGroup)
        d.rgRole.setOnCheckedChangeListener { _, checkedId ->
            if (checkedId == d.rbAdmin.id) {
                d.layoutLokasi.visibility = View.GONE
            } else {
                d.layoutLokasi.visibility = View.VISIBLE
            }
        }

        // 3. Spinner Cabang
        val cabNames = mutableListOf<String>()
        val cabIds = mutableListOf<String>()
        val curCab = db.readableDatabase.rawQuery("SELECT id_cabang, nama_lokasi FROM cabang", null)
        while(curCab.moveToNext()){
            cabIds.add(curCab.getString(0))
            cabNames.add("${curCab.getString(0)} - ${curCab.getString(1)}")
        }
        curCab.close()
        d.spinnerCabang.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cabNames))

        // 4. Spinner Rombong
        fun loadRombong(idCabang: String? = null) {
            val romList = mutableListOf<String>()
            val query = if(idCabang == null) "SELECT id_rombong FROM rombong"
            else "SELECT id_rombong FROM rombong WHERE id_cabang = '$idCabang'"
            val curR = db.readableDatabase.rawQuery(query, null)
            while(curR.moveToNext()) romList.add(curR.getString(0))
            curR.close()
            d.spinnerRombong.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, romList))
            if(romList.isNotEmpty()) d.spinnerRombong.setText(romList[0], false)
            else d.spinnerRombong.setText("", false)
        }

        d.spinnerCabang.setOnItemClickListener { _, _, position, _ ->
            loadRombong(cabIds[position])
        }

        item?.let {
            d.etEmail.setText(it.email); d.etEmail.isEnabled = false
            d.etNamaLengkap.setText(it.nama)
            d.etNoHp.setText(it.noHp)
            
            if(it.posisi == "Admin") {
                d.rbAdmin.isChecked = true
                d.layoutLokasi.visibility = View.GONE
            } else {
                d.rbBarista.isChecked = true
                d.layoutLokasi.visibility = View.VISIBLE
            }

            // Set Jabatan
            val jIdx = jabIds.indexOf(it.idJabatan)
            if(jIdx != -1) d.spinnerJabatan.setText(jabNames[jIdx], false)

            // Set Cabang and Rombong
            val cur = db.readableDatabase.rawQuery("SELECT id_cabang FROM rombong WHERE id_rombong = ?", arrayOf(it.idRombong))
            if(cur.moveToFirst()){
                val idC = cur.getString(0)
                val idx = cabIds.indexOf(idC)
                if(idx != -1) {
                    d.spinnerCabang.setText(cabNames[idx], false)
                    loadRombong(idC)
                    d.spinnerRombong.setText(it.idRombong, false)
                }
            }
            cur.close()
        } ?: run {
            if(jabNames.isNotEmpty()) d.spinnerJabatan.setText(jabNames[0], false)
            if(cabNames.isNotEmpty()) {
                d.spinnerCabang.setText(cabNames[0], false)
                loadRombong(cabIds[0])
            } else {
                loadRombong()
            }
        }

        d.btnSimpan.setOnClickListener {
            val email = d.etEmail.text?.toString() ?: ""
            val nama = d.etNamaLengkap.text?.toString() ?: ""
            val noHp = d.etNoHp.text?.toString() ?: ""

            if (email.isEmpty() || nama.isEmpty()) {
                Toast.makeText(requireContext(), "Email dan Nama wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selPos = jabNames.indexOf(d.spinnerJabatan.text.toString())
            val idJab = if(selPos != -1) jabIds[selPos] else 0
            
            val idRombong = if (d.rbAdmin.isChecked) "" else d.spinnerRombong.text.toString()
            val role = if(d.rbAdmin.isChecked) "Admin" else "Barista"

            val v = ContentValues().apply {
                put("email", email)
                put("nama", nama)
                put("no_hp", noHp)
                put("posisi", role)
                put("id_jabatan", idJab)
                put("id_rombong", idRombong)
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
    private lateinit var adapter: GajiAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabDataGajiBinding.inflate(inflater, container, false)
        db = DBOpenHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = GajiAdapter(listGaji)
        binding.rvGaji.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGaji.adapter = adapter

        loadGaji()
        binding.btnTambahGaji.setOnClickListener { showGajiDialog(null) }
    }

    private fun loadGaji() {
        listGaji.clear()
        val cursor = db.readableDatabase.rawQuery("SELECT g.*, k.nama FROM gaji g JOIN karyawan k ON g.email = k.email", null)
        while (cursor.moveToNext()) {
            listGaji.add(DataGaji(cursor.getString(0), cursor.getString(7), cursor.getString(2),
                cursor.getLong(3), cursor.getLong(4), cursor.getLong(5), cursor.getLong(6)))
        }
        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun showGajiDialog(item: DataGaji?) {
        val d = DialogKelolaGajiBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(d.root).create()

        val karEmails = mutableListOf<String>()
        val karNames = mutableListOf<String>()
        val gajisHarian = mutableListOf<Long>()
        val bonusCup = mutableListOf<Long>()

        val curK = db.readableDatabase.rawQuery("SELECT k.email, k.nama, j.gaji_pokok_per_hari, j.bonus_percup FROM karyawan k JOIN jabatan j ON k.id_jabatan = j.id_jabatan", null)
        while(curK.moveToNext()){
            karEmails.add(curK.getString(0))
            karNames.add(curK.getString(1))
            gajisHarian.add(curK.getLong(2))
            bonusCup.add(curK.getLong(3))
        }
        curK.close()
        d.spinnerKaryawan.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, karNames))

        fun hitungOtomatis() {
            val sel = karNames.indexOf(d.spinnerKaryawan.text.toString())
            if(sel == -1) return
            val hari = d.etHariMasuk.text.toString().toLongOrNull() ?: 0L
            val cup = d.etJumlahCup.text.toString().toLongOrNull() ?: 0L
            
            val totalGapok = hari * gajisHarian[sel]
            val totalBonus = cup * bonusCup[sel]
            
            d.etGajiPokok.setText(totalGapok.toString())
            d.etTotalBonus.setText(totalBonus.toString())
        }

        d.spinnerKaryawan.setOnItemClickListener { _, _, _, _ -> hitungOtomatis() }
        
        // Listener manual jika user input angka
        val watcher = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { hitungOtomatis() }
            override fun afterTextChanged(s: android.text.Editable?) {}
        }
        d.etHariMasuk.addTextChangedListener(watcher)
        d.etJumlahCup.addTextChangedListener(watcher)

        item?.let {
            d.spinnerKaryawan.setText(it.nama, false)
            d.etPeriode.setText(it.periode)
            d.etGajiPokok.setText(it.gajiPokok.toString())
            d.etTotalBonus.setText(it.bonus.toString())
            d.etKompensasi.setText(it.kompensasi.toString())
        }

        d.btnSimpan.setOnClickListener {
            val sel = karNames.indexOf(d.spinnerKaryawan.text.toString())
            if(sel == -1) {
                Toast.makeText(requireContext(), "Pilih Karyawan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val gapok = d.etGajiPokok.text.toString().toLongOrNull() ?: 0L
            val bonus = d.etTotalBonus.text.toString().toLongOrNull() ?: 0L
            val kompen = d.etKompensasi.text.toString().toLongOrNull() ?: 0L
            val total = gapok + bonus + kompen

            val v = ContentValues().apply {
                put("email", karEmails[sel])
                put("periode", d.etPeriode.text.toString())
                put("total_gaji_pokok", gapok)
                put("total_bonus", bonus)
                put("total_kompensasi", kompen)
                put("total_gaji_akhir", total)
            }
            if(item == null) {
                val idGaji = "G" + System.currentTimeMillis().toString().takeLast(5)
                v.put("id_gaji", idGaji)
                db.writableDatabase.insert("gaji", null, v)
            } else {
                db.writableDatabase.update("gaji", v, "id_gaji=?", arrayOf(item.idGaji))
            }
            loadGaji(); dialog.dismiss()
        }
        d.btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    inner class GajiAdapter(val list: List<DataGaji>) : RecyclerView.Adapter<GajiAdapter.VH>() {
        inner class VH(val b: ItemGajiBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(ItemGajiBinding.inflate(LayoutInflater.from(p.context), p, false))
        override fun onBindViewHolder(h: VH, pos: Int) {
            val i = list[pos]
            h.b.tvNo.text = (pos + 1).toString()
            h.b.tvNama.text = i.nama
            h.b.tvPeriode.text = i.periode
            h.b.tvGajiPokok.text = "Rp ${i.gajiPokok}"
            h.b.tvBonus.text = "Rp ${i.bonus}"
            h.b.tvKompensasi.text = "Rp ${i.kompensasi}"
            h.b.tvTotalGaji.text = "Rp ${i.total}"
            h.b.btnEdit.setOnClickListener { showGajiDialog(i) }
            h.b.btnHapus.setOnClickListener {
                db.writableDatabase.delete("gaji", "id_gaji=?", arrayOf(i.idGaji))
                loadGaji()
            }
        }
        override fun getItemCount() = list.size
    }
}

// ─── TAB 3: JADWAL SHIFT ───────────────────────────────────────────────────

class TabJadwalShiftFragment : Fragment() {
    private var _binding: TabJadwalShiftBinding? = null
    private val binding get() = _binding!!
    private lateinit var db: DBOpenHelper
    private val listShift = mutableListOf<JadwalShift>()
    private lateinit var adapter: ShiftAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabJadwalShiftBinding.inflate(inflater, container, false)
        db = DBOpenHelper(requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ShiftAdapter(listShift)
        binding.rvJadwal.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJadwal.adapter = adapter
        
        loadShift()
        binding.btnBuatJadwal.setOnClickListener { showShiftDialog(null) }
    }

    private fun loadShift() {
        listShift.clear()
        val q = "SELECT s.*, k.nama FROM jadwal_shift s JOIN karyawan k ON s.email = k.email"
        val cursor = db.readableDatabase.rawQuery(q, null)
        while (cursor.moveToNext()) {
            listShift.add(JadwalShift(cursor.getInt(0), cursor.getString(1), cursor.getString(5),
                cursor.getString(2), cursor.getString(3), cursor.getString(4)))
        }
        cursor.close()
        adapter.notifyDataSetChanged()
    }

    private fun showShiftDialog(item: JadwalShift?) {
        val d = DialogBuatJadwalBinding.inflate(layoutInflater)
        val dialog = AlertDialog.Builder(requireContext()).setView(d.root).create()
        
        // 1. List Karyawan untuk Spinner
        val karNames = mutableListOf<String>()
        val karEmails = mutableListOf<String>()
        val curK = db.readableDatabase.rawQuery("SELECT email, nama FROM karyawan", null)
        while(curK.moveToNext()){
            karEmails.add(curK.getString(0))
            karNames.add(curK.getString(1))
        }
        curK.close()
        d.spinnerKaryawan.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, karNames))

        // 2. List Lokasi Cabang
        val cabNames = mutableListOf<String>()
        val curC = db.readableDatabase.rawQuery("SELECT nama_lokasi FROM cabang", null)
        while(curC.moveToNext()) cabNames.add(curC.getString(0))
        curC.close()
        d.spinnerLokasi.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cabNames))

        // 3. Date Picker untuk Tanggal
        d.etTanggal.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, day ->
                val dateStr = String.format("%d-%02d-%02d", y, m + 1, day)
                d.etTanggal.setText(dateStr)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }

        // 4. Time Picker untuk Jam
        fun showTimePicker(et: com.google.android.material.textfield.TextInputEditText) {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, min ->
                et.setText(String.format("%02d:%02d", h, min))
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
        d.etJamMulai.setOnClickListener { showTimePicker(d.etJamMulai) }
        d.etJamSelesai.setOnClickListener { showTimePicker(d.etJamSelesai) }

        item?.let {
            d.spinnerKaryawan.setText(it.nama, false)
            d.etTanggal.setText(it.tanggal)
            d.etJamMulai.setText(it.jamMulai)
            d.etJamSelesai.setText(it.jamSelesai)
            
            // Cari lokasi berdasarkan rombong karyawan (opsional, jika ingin auto-fill lokasi)
            val curL = db.readableDatabase.rawQuery(
                "SELECT c.nama_lokasi FROM karyawan k JOIN rombong r ON k.id_rombong = r.id_rombong JOIN cabang c ON r.id_cabang = c.id_cabang WHERE k.email = ?", 
                arrayOf(it.email)
            )
            if(curL.moveToFirst()) d.spinnerLokasi.setText(curL.getString(0), false)
            curL.close()
        } ?: run {
            if(karNames.isNotEmpty()) d.spinnerKaryawan.setText(karNames[0], false)
            if(cabNames.isNotEmpty()) d.spinnerLokasi.setText(cabNames[0], false)
        }

        d.btnSimpan.setOnClickListener {
            val selPos = karNames.indexOf(d.spinnerKaryawan.text.toString())
            if(selPos == -1) {
                Toast.makeText(context, "Pilih karyawan!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if(d.etTanggal.text.isNullOrEmpty()){
                Toast.makeText(context, "Pilih tanggal!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val v = ContentValues().apply {
                put("email", karEmails[selPos])
                put("tanggal", d.etTanggal.text.toString())
                put("jam_mulai", d.etJamMulai.text.toString())
                put("jam_selesai", d.etJamSelesai.text.toString())
            }
            if (item == null) db.writableDatabase.insert("jadwal_shift", null, v)
            else db.writableDatabase.update("jadwal_shift", v, "id_jadwal=?", arrayOf(item.idJadwal.toString()))
            loadShift(); dialog.dismiss()
        }
        d.btnClose.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    inner class ShiftAdapter(val list: List<JadwalShift>) : RecyclerView.Adapter<ShiftAdapter.VH>() {
        inner class VH(val b: ItemJadwalBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(p: ViewGroup, t: Int) = VH(ItemJadwalBinding.inflate(LayoutInflater.from(p.context), p, false))
        override fun onBindViewHolder(h: VH, pos: Int) {
            val i = list[pos]
            h.b.tvIdJadwal.text = i.idJadwal.toString()
            h.b.tvKaryawan.text = i.nama
            h.b.tvTanggal.text = i.tanggal
            h.b.tvJam.text = "${i.jamMulai} - ${i.jamSelesai}"
            h.b.btnEdit.setOnClickListener { showShiftDialog(i) }
            h.b.btnHapus.setOnClickListener {
                db.writableDatabase.delete("jadwal_shift", "id_jadwal=?", arrayOf(i.idJadwal.toString()))
                loadShift()
            }
        }
        override fun getItemCount() = list.size
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
                put("gaji_pokok_per_hari", d.etGajiPokok.text.toString().toLongOrNull() ?: 0L)
                put("bonus_percup", d.etBonusCup.text.toString().toLongOrNull() ?: 0L)
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
