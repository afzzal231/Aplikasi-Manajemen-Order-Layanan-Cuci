package com.afzzal0039.aplikasimanajemenorderlayanancuci.database

import androidx.room.*
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Category
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {
    @Insert
    suspend fun insertOrder(order: Order)

    @Query("SELECT * FROM laundry_order ORDER BY tanggalInput DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Update
    suspend fun updateOrder(order: Order)

    @Delete
    suspend fun deleteOrder(order: Order)

    @Query("SELECT * FROM laundry_order WHERE id = :id")
    suspend fun getOrderById(id: Int): Order?


    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}