package com.afzzal0039.aplikasimanajemenorderlayanancuci.database

import androidx.room.*
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Category
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order
import kotlinx.coroutines.flow.Flow

@Dao
interface OrderDao {

    @Query("SELECT * FROM laundry_order WHERE isDeleted = 0 ORDER BY tanggalInput DESC")
    fun getAllActiveOrders(): Flow<List<Order>>

    @Query("SELECT * FROM laundry_order WHERE isDeleted = 1 ORDER BY tanggalInput DESC")
    fun getTrashOrders(): Flow<List<Order>>

    @Insert
    suspend fun insertOrder(order: Order)

    @Update
    suspend fun updateOrder(order: Order)

    @Query("SELECT * FROM laundry_order WHERE id = :id")
    suspend fun getOrderById(id: Int): Order?


    @Query("UPDATE laundry_order SET isDeleted = 1 WHERE id = :orderId")
    suspend fun moveToTrash(orderId: Int)

    @Query("UPDATE laundry_order SET isDeleted = 0 WHERE id = :orderId")
    suspend fun restoreFromTrash(orderId: Int)

    @Delete
    suspend fun deletePermanently(order: Order)

    @Query("SELECT * FROM categories")
    fun getAllCategories(): Flow<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category)

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int
}