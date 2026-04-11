package com.example.kayuhan

import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment

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

        thisParent = activity as MainActivity
        db = thisParent.getDbObject()

        tampilData()

        return view
    }

    private fun tampilData() {
        val list = ArrayList<String>()

        val cursor: Cursor = db.rawQuery("SELECT * FROM transaksi", null)

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