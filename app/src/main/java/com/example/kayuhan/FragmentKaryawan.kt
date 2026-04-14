package com.example.kayuhan

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
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
import java.util.Calendar

// ─── Data Classes ─────────────────────────────────────────────────────────────

data class KaryawanAdmin(val email: String, val nama: String, val noHp: String)

data class KaryawanBarista(
    val email: String, val idJabatan: String, val idRombong: String,
    val idCabang: String, val nama: String, val noHp: String, val posisi: String
)

data class DataGaji(
    val id: Int, val namaKaryawan: String, val periode: String,
    val gajiPokok: Long, val bonus: Long, val kompensasi: Long, val totalGaji: Long
)

data class JadwalShift(
    val idJadwal: String, val karyawan: String, val lokasi: String,
    val tanggal: String, val jam: String
)

data class Jabatan(val id: Int, val namaJabatan: String, val gajiPerHari: Long, val bonusPerCup: Long)

// ─── Main Fragment ─────────────────────────────────────────────────────────────

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
        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            tab.text = when (position) {
                0 -> "Data Karyawan"; 1 -> "Data Gaji"; 2 -> "Jadwal Shift"; 3 -> "Jabatan"; else -> ""
            }
        }.attach()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

class KaryawanPagerAdapter(fa: FragmentActivity) : FragmentStateAdapter(fa) {
    override fun getItemCount() = 4
    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> TabDataKaryawanFragment()
        1 -> TabDataGajiFragment()
        2 -> TabJadwalShiftFragment()
        3 -> TabJabatanFragment()
        else -> TabDataKaryawanFragment()
    }
}

// ─── Tab 1: Data Karyawan ─────────────────────────────────────────────────────

class TabDataKaryawanFragment : Fragment() {

    private var _binding: TabDataKaryawanBinding? = null
    private val binding get() = _binding!!

    private val listAdmin = mutableListOf(
        KaryawanAdmin("admin@kayuhan.com", "ananan", "9208932"),
        KaryawanAdmin("mgr@kayuhan.com", "bagas", "082325854560")
    )
    private val listBarista = mutableListOf(
        KaryawanBarista("barista1@kayuhan.com", "2", "R01", "C01", "Rudi", "081234567890", "Barista"),
        KaryawanBarista("barista2@kayuhan.com", "2", "R02", "C02", "Sari", "089876543210", "Senior Barista")
    )

    private lateinit var adminAdapter: AdminAdapter
    private lateinit var baristaAdapter: BaristaAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabDataKaryawanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAdapters()
        binding.btnTambahKaryawan.setOnClickListener { showKaryawanDialog(null, null) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupAdapters() {
        adminAdapter = AdminAdapter(listAdmin,
            onEdit = { showKaryawanDialog(it, null) },
            onHapus = { item ->
                val idx = listAdmin.indexOf(item)
                if (idx >= 0) { listAdmin.removeAt(idx); adminAdapter.notifyItemRemoved(idx) }
                Toast.makeText(requireContext(), "Dihapus", Toast.LENGTH_SHORT).show()
            }
        )
        baristaAdapter = BaristaAdapter(listBarista,
            onEdit = { showKaryawanDialog(null, it) },
            onHapus = { item ->
                val idx = listBarista.indexOf(item)
                if (idx >= 0) { listBarista.removeAt(idx); baristaAdapter.notifyItemRemoved(idx) }
                Toast.makeText(requireContext(), "Dihapus", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvKaryawanAdmin.layoutManager = LinearLayoutManager(requireContext())
        binding.rvKaryawanAdmin.adapter = adminAdapter
        binding.rvKaryawanBarista.layoutManager = LinearLayoutManager(requireContext())
        binding.rvKaryawanBarista.adapter = baristaAdapter
    }

    private fun showKaryawanDialog(admin: KaryawanAdmin?, barista: KaryawanBarista?) {
        val dialog = Dialog(requireContext())
        val d = DialogKelolaKaryawanBinding.inflate(layoutInflater)
        dialog.setContentView(d.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        d.spinnerRole.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("Admin", "Barista", "Kasir")))
        d.spinnerJabatan.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("1 - Admin", "2 - Barista", "3 - Kasir")))
        d.spinnerCabang.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("-", "Cabang 1", "Cabang 2")))
        d.spinnerRombong.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, listOf("-", "Rombong 1", "Rombong 2")))

        admin?.let {
            d.etEmail.setText(it.email as CharSequence)
            d.etNamaLengkap.setText(it.nama as CharSequence)
            d.etNoHp.setText(it.noHp as CharSequence)
            d.spinnerRole.setText("Admin", false)
        }
        barista?.let {
            d.etEmail.setText(it.email as CharSequence)
            d.etNamaLengkap.setText(it.nama as CharSequence)
            d.etNoHp.setText(it.noHp as CharSequence)
            d.spinnerRole.setText("Barista", false)
            d.spinnerJabatan.setText(it.idJabatan, false)
            d.spinnerCabang.setText(it.idCabang, false)
            d.spinnerRombong.setText(it.idRombong, false)
        }

        d.btnClose.setOnClickListener { dialog.dismiss() }
        d.btnSimpan.setOnClickListener {
            val email = d.etEmail.text.toString().trim()
            val nama = d.etNamaLengkap.text.toString().trim()
            val noHp = d.etNoHp.text.toString().trim()
            val role = d.spinnerRole.text.toString()
            if (email.isEmpty() || nama.isEmpty() || noHp.isEmpty()) {
                Toast.makeText(requireContext(), "Lengkapi semua field!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (role == "Admin") {
                val new = KaryawanAdmin(email, nama, noHp)
                if (admin == null) {
                    listAdmin.add(new); adminAdapter.notifyItemInserted(listAdmin.lastIndex)
                } else {
                    val idx = listAdmin.indexOf(admin)
                    listAdmin[idx] = new; adminAdapter.notifyItemChanged(idx)
                }
            } else {
                val new = KaryawanBarista(email, d.spinnerJabatan.text.toString(),
                    d.spinnerRombong.text.toString(), d.spinnerCabang.text.toString(), nama, noHp, role)
                if (barista == null) {
                    listBarista.add(new); baristaAdapter.notifyItemInserted(listBarista.lastIndex)
                } else {
                    val idx = listBarista.indexOf(barista)
                    listBarista[idx] = new; baristaAdapter.notifyItemChanged(idx)
                }
            }
            Toast.makeText(requireContext(), "Disimpan!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    inner class AdminAdapter(
        private val list: List<KaryawanAdmin>,
        private val onEdit: (KaryawanAdmin) -> Unit,
        private val onHapus: (KaryawanAdmin) -> Unit
    ) : RecyclerView.Adapter<AdminAdapter.VH>() {
        inner class VH(val b: ItemKaryawanAdminBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemKaryawanAdminBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = list[pos]
            with(h.b) {
                tvEmail.text = item.email
                tvNama.text = item.nama
                tvNoHp.text = item.noHp
                btnEdit.setOnClickListener { onEdit(item) }
                btnHapus.setOnClickListener { onHapus(item) }
            }
        }
        override fun getItemCount() = list.size
    }

    inner class BaristaAdapter(
        private val list: List<KaryawanBarista>,
        private val onEdit: (KaryawanBarista) -> Unit,
        private val onHapus: (KaryawanBarista) -> Unit
    ) : RecyclerView.Adapter<BaristaAdapter.VH>() {
        inner class VH(val b: ItemKaryawanBaristaBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemKaryawanBaristaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = list[pos]
            with(h.b) {
                tvEmail.text = item.email
                tvIdJabatan.text = item.idJabatan
                tvIdRombong.text = item.idRombong
                tvIdCabang.text = item.idCabang
                tvNama.text = item.nama
                tvNoHp.text = item.noHp
                tvPosisi.text = item.posisi
                btnEdit.setOnClickListener { onEdit(item) }
                btnHapus.setOnClickListener { onHapus(item) }
            }
        }
        override fun getItemCount() = list.size
    }
}

// ─── Tab 2: Data Gaji ─────────────────────────────────────────────────────────

class TabDataGajiFragment : Fragment() {

    private var _binding: TabDataGajiBinding? = null
    private val binding get() = _binding!!

    private val listGaji = mutableListOf(
        DataGaji(1, "Rudi", "2024-01", 2400000, 150000, 0, 2550000),
        DataGaji(2, "Sari", "2024-01", 2400000, 200000, 50000, 2650000)
    )

    private lateinit var gajiAdapter: GajiAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabDataGajiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        gajiAdapter = GajiAdapter(listGaji,
            onEdit = { showGajiDialog(it) },
            onHapus = { item ->
                val idx = listGaji.indexOf(item)
                if (idx >= 0) { listGaji.removeAt(idx); gajiAdapter.notifyItemRemoved(idx) }
                Toast.makeText(requireContext(), "Dihapus", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvGaji.layoutManager = LinearLayoutManager(requireContext())
        binding.rvGaji.adapter = gajiAdapter
        binding.btnTambahGaji.setOnClickListener { showGajiDialog(null) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showGajiDialog(gaji: DataGaji?) {
        val dialog = Dialog(requireContext())
        val d = DialogKelolaGajiBinding.inflate(layoutInflater)
        dialog.setContentView(d.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        val gajiPerHari = 80000L
        val bonusPerCup = 500L

        d.spinnerKaryawan.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
            listOf("Rudi", "Sari", "Bagas", "ananan")))

        fun hitungOtomatis() {
            val hari = d.etHariMasuk.text.toString().toLongOrNull() ?: 0L
            val cup = d.etJumlahCup.text.toString().toLongOrNull() ?: 0L
            d.etGajiPokok.setText("Rp ${hari * gajiPerHari}" as CharSequence)
            d.etTotalBonus.setText("Rp ${cup * bonusPerCup}" as CharSequence)
        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { hitungOtomatis() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        d.etHariMasuk.addTextChangedListener(watcher)
        d.etJumlahCup.addTextChangedListener(watcher)

        gaji?.let {
            d.spinnerKaryawan.setText(it.namaKaryawan, false)
            d.etPeriode.setText(it.periode as CharSequence)
            d.etGajiPokok.setText("Rp ${it.gajiPokok}" as CharSequence)
            d.etTotalBonus.setText("Rp ${it.bonus}" as CharSequence)
            d.etKompensasi.setText(it.kompensasi.toString() as CharSequence)
        }

        d.btnClose.setOnClickListener { dialog.dismiss() }
        d.btnSimpan.setOnClickListener {
            val nama = d.spinnerKaryawan.text.toString()
            val periode = d.etPeriode.text.toString().trim()
            val hari = d.etHariMasuk.text.toString().toLongOrNull() ?: 0L
            val cup = d.etJumlahCup.text.toString().toLongOrNull() ?: 0L
            val kompensasi = d.etKompensasi.text.toString().replace("Rp", "").trim().toLongOrNull() ?: 0L
            val gajiPokok = hari * gajiPerHari
            val bonus = cup * bonusPerCup
            if (nama.isEmpty() || periode.isEmpty()) {
                Toast.makeText(requireContext(), "Lengkapi data!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newId = if (listGaji.isEmpty()) 1 else listGaji.maxOf { it.id } + 1
            val new = DataGaji(gaji?.id ?: newId, nama, periode, gajiPokok, bonus, kompensasi, gajiPokok + bonus + kompensasi)
            if (gaji == null) {
                listGaji.add(new); gajiAdapter.notifyItemInserted(listGaji.lastIndex)
            } else {
                val idx = listGaji.indexOfFirst { it.id == gaji.id }
                listGaji[idx] = new; gajiAdapter.notifyItemChanged(idx)
            }
            Toast.makeText(requireContext(), "Disimpan!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    inner class GajiAdapter(
        private val list: List<DataGaji>,
        private val onEdit: (DataGaji) -> Unit,
        private val onHapus: (DataGaji) -> Unit
    ) : RecyclerView.Adapter<GajiAdapter.VH>() {
        inner class VH(val b: ItemGajiBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemGajiBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = list[pos]
            with(h.b) {
                tvNo.text = "${pos + 1}"
                tvNama.text = item.namaKaryawan
                tvPeriode.text = item.periode
                tvGajiPokok.text = "Rp ${item.gajiPokok}"
                tvBonus.text = "Rp ${item.bonus}"
                tvKompensasi.text = "Rp ${item.kompensasi}"
                tvTotalGaji.text = "Rp ${item.totalGaji}"
                btnEdit.setOnClickListener { onEdit(item) }
                btnHapus.setOnClickListener { onHapus(item) }
            }
        }
        override fun getItemCount() = list.size
    }
}

// ─── Tab 3: Jadwal Shift ──────────────────────────────────────────────────────

class TabJadwalShiftFragment : Fragment() {

    private var _binding: TabJadwalShiftBinding? = null
    private val binding get() = _binding!!

    private val listJadwal = mutableListOf(
        JadwalShift("J001", "Rudi", "Cabang 1", "2024-01-15", "08:00 - 16:00"),
        JadwalShift("J002", "Sari", "Cabang 2", "2024-01-15", "16:00 - 22:00")
    )

    private lateinit var jadwalAdapter: JadwalAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabJadwalShiftBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jadwalAdapter = JadwalAdapter(listJadwal,
            onEdit = { showJadwalDialog(it) },
            onHapus = { item ->
                val idx = listJadwal.indexOf(item)
                if (idx >= 0) { listJadwal.removeAt(idx); jadwalAdapter.notifyItemRemoved(idx) }
                Toast.makeText(requireContext(), "Dihapus", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvJadwal.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJadwal.adapter = jadwalAdapter
        binding.btnBuatJadwal.setOnClickListener { showJadwalDialog(null) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showJadwalDialog(jadwal: JadwalShift?) {
        val dialog = Dialog(requireContext())
        val d = DialogBuatJadwalBinding.inflate(layoutInflater)
        dialog.setContentView(d.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        d.spinnerKaryawan.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
            listOf("Rudi", "Sari", "Bagas", "ananan")))
        d.spinnerLokasi.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line,
            listOf("Cabang 1", "Cabang 2", "Cabang 3")))

        d.etTanggal.setOnClickListener {
            val cal = Calendar.getInstance()
            DatePickerDialog(requireContext(), { _, y, m, day ->
                val tgl = "$y-${(m + 1).toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
                d.etTanggal.setText(tgl as CharSequence)
            }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show()
        }
        d.etJamMulai.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, min ->
                val jam = "${h.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}"
                d.etJamMulai.setText(jam as CharSequence)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }
        d.etJamSelesai.setOnClickListener {
            val cal = Calendar.getInstance()
            TimePickerDialog(requireContext(), { _, h, min ->
                val jam = "${h.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}"
                d.etJamSelesai.setText(jam as CharSequence)
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }

        jadwal?.let {
            d.spinnerKaryawan.setText(it.karyawan, false)
            d.spinnerLokasi.setText(it.lokasi, false)
            d.etTanggal.setText(it.tanggal as CharSequence)
            val parts = it.jam.split(" - ")
            if (parts.size == 2) {
                d.etJamMulai.setText(parts[0] as CharSequence)
                d.etJamSelesai.setText(parts[1] as CharSequence)
            }
        }

        d.btnClose.setOnClickListener { dialog.dismiss() }
        d.btnSimpan.setOnClickListener {
            val karyawan = d.spinnerKaryawan.text.toString()
            val lokasi = d.spinnerLokasi.text.toString()
            val tanggal = d.etTanggal.text.toString().trim()
            val jamMulai = d.etJamMulai.text.toString().trim()
            val jamSelesai = d.etJamSelesai.text.toString().trim()
            if (karyawan.isEmpty() || lokasi.isEmpty() || tanggal.isEmpty() || jamMulai.isEmpty() || jamSelesai.isEmpty()) {
                Toast.makeText(requireContext(), "Lengkapi semua field!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val idBaru = "J${(listJadwal.size + 1).toString().padStart(3, '0')}"
            val new = JadwalShift(jadwal?.idJadwal ?: idBaru, karyawan, lokasi, tanggal, "$jamMulai - $jamSelesai")
            if (jadwal == null) {
                listJadwal.add(new); jadwalAdapter.notifyItemInserted(listJadwal.lastIndex)
            } else {
                val idx = listJadwal.indexOf(jadwal)
                listJadwal[idx] = new; jadwalAdapter.notifyItemChanged(idx)
            }
            Toast.makeText(requireContext(), "Disimpan!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    inner class JadwalAdapter(
        private val list: List<JadwalShift>,
        private val onEdit: (JadwalShift) -> Unit,
        private val onHapus: (JadwalShift) -> Unit
    ) : RecyclerView.Adapter<JadwalAdapter.VH>() {
        inner class VH(val b: ItemJadwalBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemJadwalBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = list[pos]
            with(h.b) {
                tvIdJadwal.text = item.idJadwal
                tvKaryawan.text = item.karyawan
                tvLokasi.text = item.lokasi
                tvTanggal.text = item.tanggal
                tvJam.text = item.jam
                btnEdit.setOnClickListener { onEdit(item) }
                btnHapus.setOnClickListener { onHapus(item) }
            }
        }
        override fun getItemCount() = list.size
    }
}

// ─── Tab 4: Jabatan ───────────────────────────────────────────────────────────

class TabJabatanFragment : Fragment() {

    private var _binding: TabJabatanBinding? = null
    private val binding get() = _binding!!

    private val listJabatan = mutableListOf(
        Jabatan(1, "Admin", 100000, 0),
        Jabatan(2, "Barista", 80000, 500),
        Jabatan(3, "Kasir", 75000, 0)
    )

    private lateinit var jabatanAdapter: JabatanAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = TabJabatanBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        jabatanAdapter = JabatanAdapter(listJabatan,
            onEdit = { showJabatanDialog(it) },
            onHapus = { item ->
                val idx = listJabatan.indexOf(item)
                if (idx >= 0) { listJabatan.removeAt(idx); jabatanAdapter.notifyItemRemoved(idx) }
                Toast.makeText(requireContext(), "Dihapus", Toast.LENGTH_SHORT).show()
            }
        )
        binding.rvJabatan.layoutManager = LinearLayoutManager(requireContext())
        binding.rvJabatan.adapter = jabatanAdapter
        binding.btnTambahJabatan.setOnClickListener { showJabatanDialog(null) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showJabatanDialog(jabatan: Jabatan?) {
        val dialog = Dialog(requireContext())
        val d = DialogKelolaJabatanBinding.inflate(layoutInflater)
        dialog.setContentView(d.root)
        dialog.window?.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.window?.setBackgroundDrawableResource(android.R.color.white)

        jabatan?.let {
            d.etNamaJabatan.setText(it.namaJabatan as CharSequence)
            d.etGajiPokok.setText(it.gajiPerHari.toString() as CharSequence)
            d.etBonusCup.setText(it.bonusPerCup.toString() as CharSequence)
        }

        d.btnClose.setOnClickListener { dialog.dismiss() }
        d.btnSimpan.setOnClickListener {
            val nama = d.etNamaJabatan.text.toString().trim()
            val gaji = d.etGajiPokok.text.toString().trim().toLongOrNull() ?: 0L
            val bonus = d.etBonusCup.text.toString().trim().toLongOrNull() ?: 0L
            if (nama.isEmpty()) {
                Toast.makeText(requireContext(), "Nama jabatan wajib diisi!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val newId = if (listJabatan.isEmpty()) 1 else listJabatan.maxOf { it.id } + 1
            val new = Jabatan(jabatan?.id ?: newId, nama, gaji, bonus)
            if (jabatan == null) {
                listJabatan.add(new); jabatanAdapter.notifyItemInserted(listJabatan.lastIndex)
            } else {
                val idx = listJabatan.indexOfFirst { it.id == jabatan.id }
                listJabatan[idx] = new; jabatanAdapter.notifyItemChanged(idx)
            }
            Toast.makeText(requireContext(), "Disimpan!", Toast.LENGTH_SHORT).show()
            dialog.dismiss()
        }
        dialog.show()
    }

    inner class JabatanAdapter(
        private val list: List<Jabatan>,
        private val onEdit: (Jabatan) -> Unit,
        private val onHapus: (Jabatan) -> Unit
    ) : RecyclerView.Adapter<JabatanAdapter.VH>() {
        inner class VH(val b: ItemJabatanBinding) : RecyclerView.ViewHolder(b.root)
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(ItemJabatanBinding.inflate(LayoutInflater.from(parent.context), parent, false))
        override fun onBindViewHolder(h: VH, pos: Int) {
            val item = list[pos]
            with(h.b) {
                tvId.text = item.id.toString()
                tvNamaJabatan.text = item.namaJabatan
                tvGajiHari.text = "Rp ${item.gajiPerHari}"
                tvBonusCup.text = "Rp ${item.bonusPerCup}"
                btnEdit.setOnClickListener { onEdit(item) }
                btnHapus.setOnClickListener { onHapus(item) }
            }
        }
        override fun getItemCount() = list.size
    }
}