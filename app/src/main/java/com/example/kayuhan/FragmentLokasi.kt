package com.example.kayuhan

import android.app.AlertDialog
import android.content.ContentValues
import android.database.Cursor
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment

class FragmentLokasi : Fragment() {
    private lateinit var dbHelper: DBOpenHelper

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.activity_fragment_lokasi, container, false)
        dbHelper = DBOpenHelper(requireContext())

        val btnTambahCabang = view.findViewById<Button>(R.id.btnTambahCabang)
        val btnTambahRombong = view.findViewById<Button>(R.id.btnTambahRombong)

        btnTambahCabang.setOnClickListener { showDialogCabang(null, null) }
        btnTambahRombong.setOnClickListener { showDialogRombong(null, null) }

        loadData(view)
        return view
    }

    private fun loadData(view: View) {
        val tableCabang = view.findViewById<TableLayout>(R.id.tableCabang)
        val tableRombong = view.findViewById<TableLayout>(R.id.tableRombong)

        // Bersihkan data lama
        tableCabang.removeAllViews()
        tableRombong.removeAllViews()

        val db = dbHelper.readableDatabase

        // Load Cabang
        val cursorCabang = db.rawQuery("SELECT * FROM cabang", null)
        while (cursorCabang.moveToNext()) {
            val id = cursorCabang.getString(0)
            val nama = cursorCabang.getString(1)
            
            val row = TableRow(context).apply { 
                setPadding(8, 12, 8, 12)
                setBackgroundColor(Color.WHITE)
            }
            row.addView(TextView(context).apply { text = id; layoutParams = TableRow.LayoutParams(0, -2, 1f) })
            row.addView(TextView(context).apply { text = nama; layoutParams = TableRow.LayoutParams(0, -2, 2f) })
            
            val actionLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, -2, 1f)
            }
            
            val btnEdit = ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_edit)
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { showDialogCabang(id, nama) }
            }
            val btnDelete = ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { deleteCabang(id) }
            }
            
            actionLayout.addView(btnEdit)
            actionLayout.addView(btnDelete)
            row.addView(actionLayout)
            tableCabang.addView(row)
        }
        cursorCabang.close()

        // Load Rombong
        val cursorRombong = db.rawQuery("SELECT r.id_rombong, c.nama_lokasi, r.id_cabang FROM rombong r JOIN cabang c ON r.id_cabang = c.id_cabang", null)
        while (cursorRombong.moveToNext()) {
            val idRombong = cursorRombong.getString(0)
            val namaLokasi = cursorRombong.getString(1)
            val idCabang = cursorRombong.getString(2)

            val row = TableRow(context).apply { 
                setPadding(8, 12, 8, 12)
                setBackgroundColor(Color.WHITE)
            }
            row.addView(TextView(context).apply { text = idRombong; layoutParams = TableRow.LayoutParams(0, -2, 1f) })
            row.addView(TextView(context).apply { text = namaLokasi; layoutParams = TableRow.LayoutParams(0, -2, 2f) })
            
            val actionLayout = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER
                layoutParams = TableRow.LayoutParams(0, -2, 1f)
            }

            val btnEdit = ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_edit)
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { showDialogRombong(idRombong, idCabang) }
            }
            val btnDelete = ImageButton(context).apply {
                setImageResource(android.R.drawable.ic_menu_delete)
                setBackgroundColor(Color.TRANSPARENT)
                setOnClickListener { deleteRombong(idRombong) }
            }

            actionLayout.addView(btnEdit)
            actionLayout.addView(btnDelete)
            row.addView(actionLayout)
            tableRombong.addView(row)
        }
        cursorRombong.close()
    }

    private fun showDialogCabang(id: String?, nama: String?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_kelola_cabang, null)
        val etId = dialogView.findViewById<EditText>(R.id.etIdCabang)
        val etNama = dialogView.findViewById<EditText>(R.id.etNamaLokasi)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpan)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)

        if (id != null) {
            tvTitle.text = "Update Cabang"
            etId.setText(id)
            etId.isEnabled = false
            etNama.setText(nama)
        }

        val dialog = AlertDialog.Builder(context).setView(dialogView).create()
        
        btnBatal.setOnClickListener { dialog.dismiss() }
        btnSimpan.setOnClickListener {
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("id_cabang", etId.text.toString())
                put("nama_lokasi", etNama.text.toString())
            }

            if (id == null) {
                db.insert("cabang", null, values)
            } else {
                db.update("cabang", values, "id_cabang=?", arrayOf(id))
            }
            loadData(requireView())
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun showDialogRombong(idR: String?, idC: String?) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_tambah_rombong, null)
        val etId = dialogView.findViewById<EditText>(R.id.etIdRombong)
        val spCabang = dialogView.findViewById<Spinner>(R.id.spCabang)
        val btnSimpan = dialogView.findViewById<Button>(R.id.btnSimpan)
        val btnBatal = dialogView.findViewById<Button>(R.id.btnBatal)
        val tvTitle = dialogView.findViewById<TextView>(R.id.tvTitle)

        // Load Spinner Data
        val listCabang = mutableListOf<String>()
        val listIdCabang = mutableListOf<String>()
        val dbRead = dbHelper.readableDatabase
        val cursor = dbRead.rawQuery("SELECT id_cabang, nama_lokasi FROM cabang", null)
        while (cursor.moveToNext()) {
            listIdCabang.add(cursor.getString(0))
            listCabang.add("${cursor.getString(1)} (${cursor.getString(0)})")
        }
        cursor.close()
        
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, listCabang)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCabang.adapter = adapter

        if (idR != null) {
            tvTitle.text = "Update Rombong"
            etId.setText(idR)
            etId.isEnabled = false
            val index = listIdCabang.indexOf(idC)
            if (index != -1) spCabang.setSelection(index)
        }

        val dialog = AlertDialog.Builder(context).setView(dialogView).create()
        btnBatal.setOnClickListener { dialog.dismiss() }
        btnSimpan.setOnClickListener {
            if (listIdCabang.isEmpty()) {
                Toast.makeText(context, "Tambah Cabang terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            val db = dbHelper.writableDatabase
            val values = ContentValues().apply {
                put("id_rombong", etId.text.toString())
                put("id_cabang", listIdCabang[spCabang.selectedItemPosition])
            }

            if (idR == null) {
                db.insert("rombong", null, values)
            } else {
                db.update("rombong", values, "id_rombong=?", arrayOf(idR))
            }
            loadData(requireView())
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun deleteCabang(id: String) {
        AlertDialog.Builder(context)
            .setTitle("Hapus Cabang")
            .setMessage("Apakah Anda yakin ingin menghapus cabang ini? Semua rombong di cabang ini juga akan terhapus.")
            .setPositiveButton("Ya") { _, _ ->
                val db = dbHelper.writableDatabase
                db.delete("rombong", "id_cabang=?", arrayOf(id))
                db.delete("cabang", "id_cabang=?", arrayOf(id))
                loadData(requireView())
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteRombong(id: String) {
        AlertDialog.Builder(context)
            .setTitle("Hapus Rombong")
            .setMessage("Yakin hapus unit ini?")
            .setPositiveButton("Ya") { _, _ ->
                val db = dbHelper.writableDatabase
                db.delete("rombong", "id_rombong=?", arrayOf(id))
                loadData(requireView())
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}
