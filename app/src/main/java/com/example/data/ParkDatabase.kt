package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [ParkingRecord::class],
    version = 1,
    exportSchema = false,
)
abstract class ParkDatabase : RoomDatabase() {
    abstract fun parkingDao(): ParkingDao

    companion object {
        @Volatile
        private var INSTANCE: ParkDatabase? = null

        fun getInstance(context: Context): ParkDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    ParkDatabase::class.java,
                    "park_finder.db",
                ).build().also { INSTANCE = it }
            }
        }
    }
}
