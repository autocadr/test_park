package com.example.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

class ParkingRepository(
    private val dao: ParkingDao,
    private val preferences: UserPreferencesRepository,
) {
    val activeRecord: Flow<ParkingRecord?> = dao.observeActiveRecord()
    val historyRecords: Flow<List<ParkingRecord>> = dao.observeHistory()
    val preferredMapApp: Flow<PreferredMapApp> = preferences.preferredMapApp

    suspend fun saveNewSpot(
        latitude: Double?,
        longitude: Double?,
        accuracyMeters: Float?,
        memo: String,
        photoPath: String?,
        parkedAtMillis: Long,
    ): Long {
        dao.deactivateAll()
        return dao.insert(
            ParkingRecord(
                latitude = latitude,
                longitude = longitude,
                accuracyMeters = accuracyMeters,
                memo = memo.trim(),
                photoPath = photoPath,
                parkedAtMillis = parkedAtMillis,
                isActive = true,
            ),
        )
    }

    suspend fun updateMemo(recordId: Long, memo: String) {
        val current = dao.getById(recordId) ?: return
        dao.update(current.copy(memo = memo.trim()))
    }

    suspend fun updatePhoto(recordId: Long, newPhotoPath: String?) {
        val current = dao.getById(recordId) ?: return
        val oldPath = current.photoPath
        dao.update(current.copy(photoPath = newPhotoPath))
        if (oldPath != null && oldPath != newPhotoPath) {
            deletePhotoIfUnreferenced(oldPath, excludingId = recordId)
        }
    }

    suspend fun clearActiveSpot() {
        dao.deactivateAll()
    }

    suspend fun restoreAsActive(record: ParkingRecord) {
        dao.deactivateAll()
        dao.update(record.copy(isActive = true))
    }

    suspend fun deleteHistoryRecord(record: ParkingRecord) {
        dao.deleteById(record.id)
        record.photoPath?.let { path ->
            deletePhotoIfUnreferenced(path, excludingId = record.id)
        }
    }

    suspend fun setPreferredMapApp(app: PreferredMapApp) {
        preferences.setPreferredMapApp(app)
    }

    private suspend fun deletePhotoIfUnreferenced(path: String, excludingId: Long) {
        val refs = dao.countOtherReferencesToPhoto(path, excludingId)
        if (refs == 0) {
            withContext(Dispatchers.IO) {
                runCatching { File(path).delete() }
            }
        }
    }
}
