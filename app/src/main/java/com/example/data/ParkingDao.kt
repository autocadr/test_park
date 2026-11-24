package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ParkingDao {
    @Query("SELECT * FROM parking_records WHERE isActive = 1 ORDER BY parkedAtMillis DESC LIMIT 1")
    fun observeActiveRecord(): Flow<ParkingRecord?>

    @Query("SELECT * FROM parking_records ORDER BY parkedAtMillis DESC LIMIT 30")
    fun observeHistory(): Flow<List<ParkingRecord>>

    @Query("UPDATE parking_records SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAll()

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: ParkingRecord): Long

    @Update
    suspend fun update(record: ParkingRecord)

    @Query("SELECT * FROM parking_records WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): ParkingRecord?

    @Query("DELETE FROM parking_records WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("SELECT COUNT(*) FROM parking_records WHERE photoPath = :path AND id != :excludingId")
    suspend fun countOtherReferencesToPhoto(path: String, excludingId: Long): Int
}
