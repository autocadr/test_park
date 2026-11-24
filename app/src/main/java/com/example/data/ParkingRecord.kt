package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parking_records")
data class ParkingRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val latitude: Double?,
    val longitude: Double?,
    val accuracyMeters: Float? = null,
    val memo: String = "",
    val photoPath: String? = null,
    val parkedAtMillis: Long = System.currentTimeMillis(),
    val isActive: Boolean = true,
) {
    val hasCoordinates: Boolean
        get() = latitude != null && longitude != null
}
