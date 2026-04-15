package com.example.kayuhan

import android.app.DatePickerDialog
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import java.util.*
import kotlin.collections.ArrayList

class FragmentTransaksi : Fragment() {

    private lateinit var listView: ListView
    private lateinit var tvTotal: TextView

    lateinit var thisParent: MainActivity
    lateinit var db: SQLiteDatabase

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.activity_fragment_transaksi, container, false)

        listView = view.findViewById(R.id.listTransaksi)
        tvTotal = view.findViewById(R.id.tvTotalPendapatan)
        val etDari: EditText = view.findViewById(R.id.etDari)
        val etSampai: EditText = view.findViewById(R.id.etSampai)
        val btnTampil: Button = view.findViewById(R.id.btnTampil)

        thisParent = activity as MainActivity
        db = thisParent.getDbObject()

        val calendar = Calendar.getInstance()

        fun showDatePicker(editText: EditText) {
            DatePickerDialog(
                requireContext(),
                { _, year, month, dayOfMonth ->
                    val selectedDate = String.format("%d-%02d-%02d", year, month + 1, dayOfMonth)
                    editText.setText(selectedDate)
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        fun setupDateTimePicker(editText: EditText) {
            editText.setOnTouchListener { v, event ->
                if (event.action == MotionEvent.ACTION_UP) {
                    // Cek jika klik pada ikon kalender (kanan)
                    val drawableRight = 2
                    if (editText.compoundDrawables[drawableRight] != null) {
                        if (event.rawX >= (editText.right - editText.compoundDrawables[drawableRight].bounds.width() - editText.paddingEnd)) {
                            showDatePicker(editText)
                            return@setOnTouchListener true
                        }
                    }
                }
                false
            }
        }

        setupDateTimePicker(etDari)
        setupDateTimePicker(etSampai)

        btnTampil.setOnClickListener {
            val dari = etDari.text.toString()
            val sampai = etSampai.text.toString()
            tampilData(dari, sampai)
        }

        tampilData()

        return view
    }

    private fun tampilData(dari: String = "", sampai: String = "") {
        val list = ArrayList<String>()

        var query = "SELECT * FROM transaksi"
        val selectionArgs = ArrayList<String>()

        if (dari.isNotEmpty() && sampai.isNotEmpty()) {
            query += " WHERE tanggal BETWEEN ? AND ?"
            selectionArgs.add("$dari 00:00:00")
            selectionArgs.add("$sampai 23:59:59")
        }

        val cursor: Cursor = db.rawQuery(query, selectionArgs.toTypedArray())

        var totalPendapatan = 0

        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getString(0)
                val jumlahItem = cursor.getInt(2)
                val datetime = cursor.getString(4)
                val total = cursor.getInt(5)
                val metode = cursor.getString(6)

                totalPendapatan += total

                val text = """
                    ID: $id
                    Tanggal: $datetime
                    Item: $jumlahItem item
                    Metode: $metode
                    Total: Rp $total
                """.trimIndent()

                list.add(text)

            } while (cursor.moveToNext())
        }

        cursor.close()

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            list
        )

        listView.adapter = adapter
        tvTotal.text = "Rp $totalPendapatan"
    }
}