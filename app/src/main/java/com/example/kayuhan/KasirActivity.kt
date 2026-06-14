package com.example.kayuhan

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class KasirActivity : AppCompatActivity() {

    private lateinit var db: SQLiteDatabase
    private lateinit var rvProducts: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var etSearch: EditText
    private lateinit var btnFloatingCart: Button

    private lateinit var btnCatAll: TextView
    private lateinit var btnCatCoffee: TextView
    private lateinit var btnCatNonCoffee: TextView

    private var emailLogin: String = ""
    private var selectedCategory: String = "Semua"

    val cartMap = mutableMapOf<String, CartItem>()

    data class CartItem(
        val idProduk: String,
        val namaProduk: String,
        val hargaJual: Int,
        var qty: Int
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_kasir)

        emailLogin = intent.getStringExtra("EXTRA_EMAIL") ?: ""
        db = DBOpenHelper(this).writableDatabase

        rvProducts      = findViewById(R.id.rvProducts)
        etSearch        = findViewById(R.id.etSearchMenu)
        btnFloatingCart = findViewById(R.id.btnFloatingCart)
        btnCatAll       = findViewById(R.id.btnCatAll)
        btnCatCoffee    = findViewById(R.id.btnCatCoffee)
        btnCatNonCoffee = findViewById(R.id.btnCatNonCoffee)

        // Grid 2 kolom untuk card produk
        rvProducts.layoutManager = GridLayoutManager(this, 2)

        btnCatAll.setOnClickListener       { setCategory("Semua") }
        btnCatCoffee.setOnClickListener    { setCategory("Coffee") }
        btnCatNonCoffee.setOnClickListener { setCategory("Non-Coffee") }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { loadProducts() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnFloatingCart.setOnClickListener { showCartBottomSheet() }

        loadProducts()
    }

    private fun setCategory(category: String) {
        selectedCategory = category

        // reset semua chip
        listOf(btnCatAll, btnCatCoffee, btnCatNonCoffee).forEach {
            it.setBackgroundResource(R.drawable.bg_chip_unselected)
            it.setTextColor(getColor(android.R.color.black))
        }

        val selectedChip = when (category) {
            "Coffee"     -> btnCatCoffee
            "Non-Coffee" -> btnCatNonCoffee
            else         -> btnCatAll
        }
        selectedChip.setBackgroundResource(R.drawable.bg_chip_selected)
        selectedChip.setTextColor(getColor(android.R.color.white))

        loadProducts()
    }

    private fun loadProducts() {
        val keyword = etSearch.text.toString().trim()

        // 1. Sesuaikan URL dengan IP Laptop/Laragon kamu yang aktif saat ini
        val url = "http://10.187.224.115/kayuhanmobile/get_menu.php"

        val queue = com.android.volley.toolbox.Volley.newRequestQueue(this)
        val stringRequest = com.android.volley.toolbox.StringRequest(
            com.android.volley.Request.Method.GET, url,
            { response ->
                try {
                    val jsonArray = org.json.JSONArray(response)
                    val productList = mutableListOf<ProductAdapter.Product>()

                    // 2. Looping langsung dari hasil JSON MySQL
                    for (i in 0 until jsonArray.length()) {
                        val item = jsonArray.getJSONObject(i)

                        val id = item.getString("id_produk")
                        val nama = item.getString("nama_produk")
                        val kat = item.getString("kategori") // Membaca "Coffee" atau "Non-Coffee" dari MySQL
                        val harga = item.getInt("harga_jual")

                        // Filter Kategori di sisi Android (Client-side filtering)
                        if (selectedCategory != "Semua" && kat != selectedCategory) {
                            continue
                        }

                        // Filter Search Keyword di sisi Android
                        if (keyword.isNotEmpty() && !nama.contains(keyword, ignoreCase = true)) {
                            continue
                        }

                        val qty = cartMap[id]?.qty ?: 0
                        productList.add(ProductAdapter.Product(id, nama, kat, harga, qty))
                    }

                    // 3. Set ke Adapter
                    productAdapter = ProductAdapter(
                        products = productList,
                        onQtyChanged = { product, newQty ->
                            if (newQty > 0) {
                                cartMap[product.id] = CartItem(product.id, product.nama, product.harga, newQty)
                            } else {
                                cartMap.remove(product.id)
                            }
                            updateCartButton()
                        }
                    )
                    rvProducts.adapter = productAdapter

                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Gagal mengurai data menu MySQL", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Gagal konek ke MySQL server", Toast.LENGTH_SHORT).show()
            }
        )
        queue.add(stringRequest)
    }

    private fun updateCartButton() {
        val totalItem = cartMap.values.sumOf { it.qty }
        btnFloatingCart.text = "Lihat Keranjang ($totalItem Item)"
    }

    private fun showCartBottomSheet() {
        if (cartMap.isEmpty()) {
            Toast.makeText(this, "Keranjang masih kosong", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = BottomSheetDialog(this)
        val view   = layoutInflater.inflate(R.layout.bottom_sheet_cart, null)
        dialog.setContentView(view)

        val rvCart      = view.findViewById<RecyclerView>(R.id.rvCartItems)
        val tvTotal     = view.findViewById<TextView>(R.id.tvTotalPrice)
        val btnBayar    = view.findViewById<Button>(R.id.btnBayar)
        val optTunai    = view.findViewById<LinearLayout>(R.id.optTunai)
        val optQris     = view.findViewById<LinearLayout>(R.id.optQris)
        val tvClearCart = view.findViewById<TextView>(R.id.tvClearCart)

        var metodePembayaran = "Tunai"

        fun hitungTotal() = cartMap.values.sumOf { it.hargaJual * it.qty }
        fun formatRp(amount: Int) = "Rp ${String.format("%,d", amount).replace(',', '.')}"

        tvTotal.text = formatRp(hitungTotal())

        rvCart.layoutManager = LinearLayoutManager(this)
        val cartAdapter = CartAdapter(
            items = cartMap.values.toMutableList(),
            onQtyChanged = { item, newQty ->
                if (newQty > 0) cartMap[item.idProduk]?.qty = newQty
                else cartMap.remove(item.idProduk)
                tvTotal.text = formatRp(hitungTotal())
                updateCartButton()
                if (cartMap.isEmpty()) dialog.dismiss()
            }
        )
        rvCart.adapter = cartAdapter

        fun selectMetode(metode: String) {
            metodePembayaran = metode
            if (metode == "Tunai") {
                optTunai.setBackgroundResource(R.drawable.bg_payment_selected)
                optQris.setBackgroundResource(R.drawable.bg_payment_unselected)
            } else {
                optTunai.setBackgroundResource(R.drawable.bg_payment_unselected)
                optQris.setBackgroundResource(R.drawable.bg_payment_selected)
            }
        }

        optTunai.setOnClickListener { selectMetode("Tunai") }
        optQris.setOnClickListener  { selectMetode("QRIS") }

        tvClearCart.setOnClickListener {
            cartMap.clear()
            updateCartButton()
            dialog.dismiss()
            loadProducts()
        }

        btnBayar.setOnClickListener {
            simpanTransaksi(metodePembayaran)
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun simpanTransaksi(metode: String) {
        if (cartMap.isEmpty()) return

        val sdf         = SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault())
        val idTransaksi = "TRX-${sdf.format(Date())}"
        val datetime    = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        val totalBayar  = cartMap.values.sumOf { it.hargaJual * it.qty }

        cartMap.values.forEachIndexed { index, item ->
            val values = ContentValues().apply {
                put("id_transaksi",      if (index == 0) idTransaksi else "$idTransaksi-${index + 1}")
                put("email",             emailLogin)
                put("jumlah_item",       item.qty)
                put("harga_item",        item.hargaJual)
                put("datetime",          datetime)
                put("total_bayar",       totalBayar)
                put("metode_pembayaran", metode)
            }
            db.insert("transaksi", null, values)
        }

        cartMap.clear()
        updateCartButton()
        loadProducts()
        Toast.makeText(this, "Transaksi berhasil disimpan!", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        db.close()
    }
}