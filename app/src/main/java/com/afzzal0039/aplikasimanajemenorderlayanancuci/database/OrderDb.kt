package com.afzzal0039.aplikasimanajemenorderlayanancuci.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Category

@Database(entities = [Order::class, Category::class], version = 3, exportSchema = false)
abstract class OrderDb : RoomDatabase() {

    abstract val dao: OrderDao

    companion object {
        @Volatile
        private var INSTANCE: OrderDb? = null

        fun getInstance(context: Context): OrderDb {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    OrderDb::class.java,
                    "order_db"
                )

                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}