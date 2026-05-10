package com.afzzal0039.aplikasimanajemenorderlayanancuci.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "laundry_order")
data class Order(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val namaPelanggan: String,
    val berat: Float,
    val isJaket: Boolean,
    val isSprei: Boolean,
    val paketLayanan: String,
    val totalHarga: Int,
    val estimasiSelesai: String,
    val tanggalInput: Long = System.currentTimeMillis()
)