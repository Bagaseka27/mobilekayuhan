package com.example.kayuhan

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class FragmentLokasi : Fragment() {

    private val URL_ROOT    = "http://192.168.1.31/php-mobile-kayuhan"
    private val URL_LOKASI  = "$URL_ROOT/lokasi_action.php"
    private val URL_ROMBONG = "$URL_ROOT/rombong_action.php"

    private val listIdCabangSpinner   = mutableListOf<String>()
    private val listNamaCabangSpinner = mutableListOf<String>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.activity_fragment_lokasi, container, false)

        view.findViewById<Button>(R.id.btnTambahCabang)
            .setOnClickListener { showDialogCabang(null, null, null) }
        view.findViewById<Button>(R.id.btnTambahRombong)
            .setOnClickListener { showDialogRombong(null, null) }

        loadData(view)
        return view
    }

    override fun onResume() {
        super.onResume()
        view?.let { loadData(it) }
    }

    private fun loadData(view: View) {
        val tableCabang  = view.findViewById<TableLayout>(R.id.tableCabang)
        val tableRombong = view.findViewById<TableLayout>(R.id.tableRombong)

        tableCabang.removeAllViews()
        tableRombong.removeAllViews()

        val requestCabang = StringRequest(
            Request.Method.GET, URL_LOKASI,
            { response ->
                try {
                    listIdCabangSpinner.clear()
                    listNamaCabangSpinner.clear()

                    val jsonArray = JSONArray(response)
                    for (x in 0 until jsonArray.length()) {
                        val obj    = jsonArray.getJSONObject(x)
                        val id     = obj.getString("ID_CABANG")
                        val nama   = obj.getString("NAMA_LOKASI")
                        val alamat = obj.optString("alamat", "Tidak Ada Alamat")

                        listIdCabangSpinner.add(id)
                        listNamaCabangSpinner.add("$nama ($id)")

                        val row = TableRow(context).apply {
                            setPadding(8, 12, 8, 12)
                            setBackgroundColor(Color.WHITE)
                        }

                        row.addView(buatTextView(id,   TableRow.LayoutParams(0, -2, 1f)))
                        row.addView(buatTextView(nama, TableRow.LayoutParams(0, -2, 2f)))

                        val actionLayout = buatActionLayout()
                        actionLayout.addView(buatImageButton(android.R.drawable.ic_menu_edit) {
                            showDialogCabang(id, nama, alamat)
                        })
                        actionLayout.addView(buatImageButton(android.R.drawable.ic_menu_delete) {
                            deleteCabang(id)
                        })

                        row.addView(actionLayout)
                        tableCabang.addView(row)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Format data cabang tidak valid", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(context, "Gagal memuat data cabang", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(requestCabang)

        val requestRombong = StringRequest(
            Request.Method.GET, URL_ROMBONG,
            { response ->
                try {
                    val jsonArray = JSONArray(response)
                    for (x in 0 until jsonArray.length()) {
                        val obj        = jsonArray.getJSONObject(x)
                        val idRombong  = obj.getString("ID_ROMBONG")
                        val idCabang   = obj.getString("ID_CABANG")
                        val namaLokasi = obj.optString("NAMA_LOKASI", obj.optString("NAMA_CABANG", "Cabang $idCabang"))

                        val row = TableRow(context).apply {
                            setPadding(8, 12, 8, 12)
                            setBackgroundColor(Color.WHITE)
                        }

                        row.addView(buatTextView(idRombong,  TableRow.LayoutParams(0, -2, 1f)))
                        row.addView(buatTextView(namaLokasi, TableRow.LayoutParams(0, -2, 2f)))

                        val actionLayout = buatActionLayout()
                        actionLayout.addView(buatImageButton(android.R.drawable.ic_menu_edit) {
                            showDialogRombong(idRombong, idCabang)
                        })
                        actionLayout.addView(buatImageButton(android.R.drawable.ic_menu_delete) {
                            deleteRombong(idRombong)
                        })

                        row.addView(actionLayout)
                        tableRombong.addView(row)
                    }
                } catch (e: Exception) {
                    Toast.makeText(context, "Format data rombong tidak valid", Toast.LENGTH_SHORT).show()
                }
            },
            {
                Toast.makeText(context, "Gagal memuat data rombong", Toast.LENGTH_SHORT).show()
            }
        )
        Volley.newRequestQueue(requireContext()).add(requestRombong)
    }

    private fun buatTextView(isi: String, params: TableRow.LayoutParams): TextView {
        return TextView(context).apply {
            text        = isi
            layoutParams = params
        }
    }

    private fun buatActionLayout(): LinearLayout {
        return LinearLayout(context).apply {
            orientation  = LinearLayout.HORIZONTAL
            gravity      = Gravity.CENTER
            layoutParams = TableRow.LayoutParams(0, -2, 1f)
        }
    }

    private fun buatImageButton(iconRes: Int, onClick: () -> Unit): ImageButton {
        return ImageButton(context).apply {
            setImageResource(iconRes)
            setBackgroundColor(Color.TRANSPARENT)
            setOnClickListener { onClick() }
        }
    }

    private fun buildParams(vararg pairs: Pair<String, String>): MutableMap<String, String> =
        hashMapOf(*pairs)

    private fun showDialogCabang(id: String?, nama: String?, alamat: String?) {
        val ctx        = requireContext()
        val dialogView = layoutInflater.inflate(R.layout.dialog_kelola_cabang, null)
        val etId       = dialogView.findViewById<EditText>(R.id.etIdCabang)
        val etNama     = dialogView.findViewById<EditText>(R.id.etNamaLokasi)
        val btnSimpan  = dialogView.findViewById<Button>(R.id.btnSimpan)
        val btnBatal   = dialogView.findViewById<Button>(R.id.btnBatal)
        val tvTitle    = dialogView.findViewById<TextView>(R.id.tvTitle)

        val etAlamat: EditText = dialogView.findViewById<EditText>(
            resources.getIdentifier("etAlamatCabang", "id", ctx.packageName)
        ) ?: etNama

        if (id != null) {
            tvTitle.text = "Update Cabang"
            etId.setText(id)
            etId.isEnabled = false
            etNama.setText(nama)
            if (alamat != null && etAlamat !== etNama) etAlamat.setText(alamat)
        }

        val dialog = AlertDialog.Builder(ctx).setView(dialogView).create()
        btnBatal.setOnClickListener { dialog.dismiss() }
        btnSimpan.setOnClickListener {
            val modeAction = if (id == null) "insert" else "update"
            val request = object : StringRequest(
                Method.POST, URL_LOKASI,
                { response ->
                    try {
                        val json = JSONObject(response)
                        if (json.getString("kode") == "000") {
                            Toast.makeText(ctx, "Data Cabang Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                            loadData(requireView())
                            dialog.dismiss()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Response tidak valid", Toast.LENGTH_SHORT).show()
                    }
                },
                {
                    Toast.makeText(ctx, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> = buildParams(
                    "mode"        to modeAction,
                    "id_cabang"   to etId.text.toString(),
                    "nama_cabang" to etNama.text.toString(),
                    "alamat"      to if (etAlamat !== etNama) etAlamat.text.toString() else "Alamat Default"
                )
            }
            Volley.newRequestQueue(ctx).add(request)
        }
        dialog.show()
    }

    private fun showDialogRombong(idR: String?, idC: String?) {
        val ctx        = requireContext()
        val dialogView = layoutInflater.inflate(R.layout.dialog_tambah_rombong, null)
        val etId       = dialogView.findViewById<EditText>(R.id.etIdRombong)
        val spCabang   = dialogView.findViewById<Spinner>(R.id.spCabang)
        val btnSimpan  = dialogView.findViewById<Button>(R.id.btnSimpan)
        val btnBatal   = dialogView.findViewById<Button>(R.id.btnBatal)
        val tvTitle    = dialogView.findViewById<TextView>(R.id.tvTitle)

        val adapter = ArrayAdapter(ctx, android.R.layout.simple_spinner_item, listNamaCabangSpinner)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spCabang.adapter = adapter

        if (idR != null) {
            tvTitle.text = "Update Rombong"
            etId.setText(idR)
            etId.isEnabled = false
            val index = listIdCabangSpinner.indexOf(idC)
            if (index != -1) spCabang.setSelection(index)
        }

        val dialog = AlertDialog.Builder(ctx).setView(dialogView).create()
        btnBatal.setOnClickListener { dialog.dismiss() }
        btnSimpan.setOnClickListener {
            if (listIdCabangSpinner.isEmpty()) {
                Toast.makeText(ctx, "Tambah Cabang terlebih dahulu!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val modeAction       = if (idR == null) "insert" else "update"
            val selectedIdCabang = listIdCabangSpinner[spCabang.selectedItemPosition]

            val request = object : StringRequest(
                Method.POST, URL_ROMBONG,
                { response ->
                    try {
                        val json = JSONObject(response)
                        if (json.getString("kode") == "000") {
                            Toast.makeText(ctx, "Data Rombong Berhasil Disimpan", Toast.LENGTH_SHORT).show()
                            loadData(requireView())
                            dialog.dismiss()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(ctx, "Response tidak valid", Toast.LENGTH_SHORT).show()
                    }
                },
                {
                    Toast.makeText(ctx, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
                }
            ) {
                override fun getParams(): MutableMap<String, String> = buildParams(
                    "mode"       to modeAction,
                    "id_rombong" to etId.text.toString(),
                    "id_cabang"  to selectedIdCabang
                )
            }
            Volley.newRequestQueue(ctx).add(request)
        }
        dialog.show()
    }

    private fun deleteCabang(id: String) {
        val ctx = requireContext()
        AlertDialog.Builder(ctx)
            .setTitle("Hapus Cabang")
            .setMessage("Apakah Anda yakin ingin menghapus cabang ini? Semua rombong di cabang ini juga akan terhapus.")
            .setPositiveButton("Ya") { _, _ ->
                val request = object : StringRequest(
                    Method.POST, URL_LOKASI,
                    { response ->
                        try {
                            val json = JSONObject(response)
                            if (json.getString("kode") == "000") {
                                Toast.makeText(ctx, "Cabang berhasil dihapus", Toast.LENGTH_SHORT).show()
                                loadData(requireView())
                            }
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Response tidak valid", Toast.LENGTH_SHORT).show()
                        }
                    },
                    {
                        Toast.makeText(ctx, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> = buildParams(
                        "mode"      to "delete",
                        "id_cabang" to id
                    )
                }
                Volley.newRequestQueue(ctx).add(request)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun deleteRombong(id: String) {
        val ctx = requireContext()
        AlertDialog.Builder(ctx)
            .setTitle("Hapus Rombong")
            .setMessage("Yakin hapus unit ini?")
            .setPositiveButton("Ya") { _, _ ->
                val request = object : StringRequest(
                    Method.POST, URL_ROMBONG,
                    { response ->
                        try {
                            val json = JSONObject(response)
                            if (json.getString("kode") == "000") {
                                Toast.makeText(ctx, "Rombong berhasil dihapus", Toast.LENGTH_SHORT).show()
                                loadData(requireView())
                            }
                        } catch (e: Exception) {
                            Toast.makeText(ctx, "Response tidak valid", Toast.LENGTH_SHORT).show()
                        }
                    },
                    {
                        Toast.makeText(ctx, "Gagal koneksi ke server", Toast.LENGTH_SHORT).show()
                    }
                ) {
                    override fun getParams(): MutableMap<String, String> = buildParams(
                        "mode"       to "delete",
                        "id_rombong" to id
                    )
                }
                Volley.newRequestQueue(ctx).add(request)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}