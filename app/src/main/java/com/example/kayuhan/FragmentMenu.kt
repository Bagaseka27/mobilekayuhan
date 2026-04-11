package com.example.kayuhan

import android.app.AlertDialog
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.kayuhan.databinding.ActivityFragmentMenuBinding

class FragmentMenu : Fragment() {

    private var _binding: ActivityFragmentMenuBinding? = null
    private val binding get() = _binding!!
    lateinit var db: SQLiteDatabase

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = ActivityFragmentMenuBinding.inflate(inflater, container, false)
        val dbHelper = DBOpenHelper(requireContext())
        db = dbHelper.writableDatabase
        loadMenu()
        binding.btnTambah.setOnClickListener { showTambahDialog() }
        return binding.root
    }

    private fun loadMenu() {
        // Hapus semua row kecuali header (index 0)
        val childCount = binding.tableMenu.childCount
        if (childCount > 1) {
            binding.tableMenu.removeViews(1, childCount - 1)
        }

        val cursor = db.rawQuery("SELECT * FROM menu", null)

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(cursor.getColumnIndexOrThrow("id_produk"))
                val nama = cursor.getString(cursor.getColumnIndexOrThrow("nama_produk"))
                val kategori = cursor.getString(cursor.getColumnIndexOrThrow("kategori"))
                val hargaDasar = cursor.getInt(cursor.getColumnIndexOrThrow("harga_dasar"))
                val hargaJual = cursor.getInt(cursor.getColumnIndexOrThrow("harga_jual"))

                val row = TableRow(requireContext()).apply {
                    setPadding(0, 4, 0, 4)
                }

                fun buatTextView(isi: String, lebar: Int): TextView {
                    return TextView(requireContext()).apply {
                        text = isi
                        setPadding(8, 12, 8, 12)
                        layoutParams = TableRow.LayoutParams(lebar, TableRow.LayoutParams.WRAP_CONTENT)
                    }
                }

                row.addView(buatTextView(id, 55.dp))
                row.addView(buatTextView(nama, 100.dp))
                row.addView(buatTextView(kategori, 100.dp))
                row.addView(buatTextView(hargaJual.toString(), 80.dp))

                // Layout Aksi (Edit + Hapus)
                val layoutAksi = LinearLayout(requireContext()).apply {
                    orientation = LinearLayout.HORIZONTAL
                    gravity = android.view.Gravity.CENTER
                    layoutParams = TableRow.LayoutParams(160.dp, TableRow.LayoutParams.WRAP_CONTENT)
                }

                val btnEdit = Button(requireContext()).apply {
                    text = "Edit"
                    textSize = 12f
                    setTextColor(Color.WHITE)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#FFC107"))
                    minWidth = 0
                    minimumWidth = 0
                    setPadding(16, 0, 16, 0)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, 80
                    )
                }

                val btnHapus = Button(requireContext()).apply {
                    text = "Hapus"
                    textSize = 12f
                    setTextColor(Color.WHITE)
                    backgroundTintList = android.content.res.ColorStateList.valueOf(Color.parseColor("#E53935"))
                    minWidth = 0
                    minimumWidth = 0
                    setPadding(16, 0, 16, 0)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT, 80
                    ).apply { marginStart = 8 }
                }

                layoutAksi.addView(btnEdit)
                layoutAksi.addView(btnHapus)
                row.addView(layoutAksi)

                binding.tableMenu.addView(row)

                btnEdit.setOnClickListener {
                    showEditDialog(id, nama, kategori, hargaDasar, hargaJual)
                }

                btnHapus.setOnClickListener {
                    AlertDialog.Builder(requireContext())
                        .setTitle("Konfirmasi")
                        .setMessage("Apakah yakin hapus menu ini?")
                        .setPositiveButton("Ya") { _, _ ->
                            db.delete("menu", "id_produk=?", arrayOf(id))
                            loadMenu()
                        }
                        .setNegativeButton("Tidak", null)
                        .show()
                }

            } while (cursor.moveToNext())
        }
        cursor.close()
    }

    // Extension property dp
    private val Int.dp: Int get() = (this * resources.displayMetrics.density).toInt()

    private fun setupSpinner(spinner: Spinner, selected: String = "Coffee") {
        val kategoriList = listOf("Coffee", "Non Coffee")
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            kategoriList
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        val idx = kategoriList.indexOf(selected)
        if (idx >= 0) spinner.setSelection(idx)
    }

    private fun showTambahDialog() {
        val view = layoutInflater.inflate(R.layout.dialog_menu, null)
        val etId        = view.findViewById<EditText>(R.id.etIdMenu)
        val etNama      = view.findViewById<EditText>(R.id.etNamaMenu)
        val spinner     = view.findViewById<Spinner>(R.id.spKategori)
        val etHargaDasar = view.findViewById<EditText>(R.id.etHargaDasar)
        val etHargaJual  = view.findViewById<EditText>(R.id.etHargaJual)

        setupSpinner(spinner)

        AlertDialog.Builder(requireContext())
            .setTitle("Tambah Menu")
            .setView(view)
            .setPositiveButton("Simpan") { _, _ ->
                val id       = etId.text.toString().trim()
                val nama     = etNama.text.toString().trim()
                val kategori = spinner.selectedItem.toString()
                val hd       = etHargaDasar.text.toString().trim()
                val hj       = etHargaJual.text.toString().trim()

                if (id.isNotEmpty() && nama.isNotEmpty() && hd.isNotEmpty() && hj.isNotEmpty()) {
                    val values = ContentValues().apply {
                        put("id_produk", id)
                        put("nama_produk", nama)
                        put("kategori", kategori)
                        put("harga_dasar", hd.toIntOrNull() ?: 0)
                        put("harga_jual", hj.toIntOrNull() ?: 0)
                    }
                    db.insert("menu", null, values)
                    loadMenu()
                } else {
                    Toast.makeText(context, "Semua field harus diisi", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    private fun showEditDialog(id: String, nama: String, kategori: String, hargaDasar: Int, hargaJual: Int) {
        val view = layoutInflater.inflate(R.layout.dialog_menu, null)
        val etId         = view.findViewById<EditText>(R.id.etIdMenu)
        val etNama       = view.findViewById<EditText>(R.id.etNamaMenu)
        val spinner      = view.findViewById<Spinner>(R.id.spKategori)
        val etHargaDasar = view.findViewById<EditText>(R.id.etHargaDasar)
        val etHargaJual  = view.findViewById<EditText>(R.id.etHargaJual)

        etId.setText(id)
        etId.isEnabled = false
        etNama.setText(nama)
        setupSpinner(spinner, kategori)
        etHargaDasar.setText(hargaDasar.toString())
        etHargaJual.setText(hargaJual.toString())

        AlertDialog.Builder(requireContext())
            .setTitle("Edit Menu")
            .setView(view)
            .setPositiveButton("Update") { _, _ ->
                val newNama     = etNama.text.toString().trim()
                val newKategori = spinner.selectedItem.toString()
                val newHd       = etHargaDasar.text.toString().trim()
                val newHj       = etHargaJual.text.toString().trim()

                if (newNama.isNotEmpty() && newHd.isNotEmpty() && newHj.isNotEmpty()) {
                    val values = ContentValues().apply {
                        put("nama_produk", newNama)
                        put("kategori", newKategori)
                        put("harga_dasar", newHd.toIntOrNull() ?: 0)
                        put("harga_jual", newHj.toIntOrNull() ?: 0)
                    }
                    db.update("menu", values, "id_produk=?", arrayOf(id))
                    loadMenu()
                } else {
                    Toast.makeText(context, "Field tidak boleh kosong", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Batal", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}