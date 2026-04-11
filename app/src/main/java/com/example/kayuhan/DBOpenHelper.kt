import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
class DBOpenHelper(context: Context) :
    SQLiteOpenHelper(context, DB_Name, null, DB_Ver) {
    override fun onCreate(db: SQLiteDatabase?) {

        val tKaryawan = "create table karyawan(" +
                "email text primary key, " +
                "id_jabatan integer not null, " +
                "id_rombong text not null, " +
                "nama text not null, " +
                "no_hp text not null, " +
                "posisi text not null)"

        val tJabatan = "create table jabatan(" +
                "id_jabatan integer primary key, " +
                "nama_jabatan text not null, " +
                "gaji_pokok_per_hari integer not null, " +
                "bonus_percup integer not null)"

        val tGaji = "create table gaji(" +
                "id_gaji text primary key, " +
                "email text not null, " +
                "periode text not null, " +
                "total_gaji_pokok integer not null, " +
                "total_bonus integer not null, " +
                "total_kompensasi integer not null, " +
                "total_gaji_akhir integer not null)"

        val tMenu = "create table menu(" +
                "id_produk text primary key, " +
                "nama_produk text not null, " +
                "harga_dasar integer not null, " +
                "harga_jual integer not null)"

        val tTransaksi = "create table transaksi(" +
                "id_transaksi text primary key, " +
                "email text not null, " +
                "jumlah_item integer not null, " +
                "harga_item integer not null, " +
                "datetime text not null, " +
                "total_bayar integer not null, " +
                "metode_pembayaran text not null)"



        val insJabatan = "insert into jabatan(id_jabatan, nama_jabatan, gaji_pokok_per_hari, bonus_percup) " +
                "values(1, 'Admin', 45000, 0), " +
                "(2, 'Senior', 40000, 1000), " +
                "(3, 'Junior', 35000, 500), " +
                "(4, 'Trainer', 30000, 0)"
//eksekusi query
        db?.execSQL(tKaryawan)
        db?.execSQL(tJabatan)
        db?.execSQL(tGaji)
        db?.execSQL(tMenu)
        db?.execSQL(tTransaksi)
        db?.execSQL(insJabatan)
    }
    //fungsi berjalan saat versi database diubah
    override fun onUpgrade(
        db: SQLiteDatabase?,
        oldVersion: Int,
        newVersion: Int
    ) {
//digunakan untuk update struktur tabel
    }
    companion object {
        val DB_Name = "kayuhanmobile"
        val DB_Ver = 1 // versi database
    }
}