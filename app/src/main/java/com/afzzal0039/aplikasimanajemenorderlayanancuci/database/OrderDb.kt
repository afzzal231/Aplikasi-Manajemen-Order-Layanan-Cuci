package com.afzzal0039.aplikasimanajemenorderlayanancuci.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.afzzal0039.aplikasimanajemenorderlayanancuci.model.Order


@Database(entities = [Order::class], version = 1, exportSchema = false)
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
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}