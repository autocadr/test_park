package com.example.ui

import android.app.Application
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.camera.PhotoStorage
import com.example.data.ParkDatabase
import com.example.data.ParkingRecord
import com.example.data.ParkingRepository
import com.example.data.PreferredMapApp
import com.example.data.UserPreferencesRepository
import com.example.location.LocationResult
import com.example.location.LocationTracker
import com.example.navigation.MapNavigator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface GpsAcquisitionState {
    data object Idle : GpsAcquisitionState
    data object Loading : GpsAcquisitionState
    data class Locked(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float?,
    ) : GpsAcquisitionState

    data class Failed(
        val message: String,
        val isPermissionIssue: Boolean = false,
    ) : GpsAcquisitionState
}

data class SaveSpotDraftState(
    val isVisible: Boolean = false,
    val gpsState: GpsAcquisitionState = GpsAcquisitionState.Idle,
    val capturedAtMillis: Long = System.currentTimeMillis(),
    val memo: String = "",
    val photoPath: String? = null,
    val isSaving: Boolean = false,
)

class ParkingViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ParkDatabase.getInstance(application)
    private val preferencesRepository = UserPreferencesRepository(application)
    private val repository = ParkingRepository(database.parkingDao(), preferencesRepository)
    private val locationTracker = LocationTracker(application)

    val activeRecord: StateFlow<ParkingRecord?> = repository.activeRecord
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = null,
        )

    val historyRecords: StateFlow<List<ParkingRecord>> = repository.historyRecords
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList(),
        )

    val preferredMapApp: StateFlow<PreferredMapApp> = repository.preferredMapApp
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = PreferredMapApp.AUTO,
        )

    private val _installedMapApps = MutableStateFlow(
        MapNavigator.detectInstalledMapApps(application),
    )
    val installedMapApps: StateFlow<Set<PreferredMapApp>> = _installedMapApps.asStateFlow()

    private val _draftState = MutableStateFlow(SaveSpotDraftState())
    val draftState: StateFlow<SaveSpotDraftState> = _draftState.asStateFlow()

    private val _uiMessages = MutableSharedFlow<String>(extraBufferCapacity = 4)
    val uiMessages: SharedFlow<String> = _uiMessages.asSharedFlow()

    fun hasLocationPermission(): Boolean = locationTracker.hasLocationPermission()

    fun hasCameraPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            getApplication(),
            android.Manifest.permission.CAMERA,
        ) == PackageManager.PERMISSION_GRANTED
    }

    fun refreshInstalledMapApps() {
        _installedMapApps.value = MapNavigator.detectInstalledMapApps(getApplication())
    }

    fun openSaveSheet(initialPhotoPath: String? = null) {
        _draftState.value = SaveSpotDraftState(
            isVisible = true,
            gpsState = GpsAcquisitionState.Idle,
            capturedAtMillis = System.currentTimeMillis(),
            memo = "",
            photoPath = initialPhotoPath,
            isSaving = false,
        )
        if (locationTracker.hasLocationPermission()) {
            refreshDraftGps()
        } else {
            _draftState.update {
                it.copy(
                    gpsState = GpsAcquisitionState.Failed(
                        message = "위치 권한을 허용하면 현재 GPS 위도·경도가 자동으로 입력됩니다.",
                        isPermissionIssue = true,
                    ),
                )
            }
        }
    }

    fun dismissSaveSheet(deleteDraftPhotoIfUnsaved: Boolean = true) {
        val currentDraftPhoto = _draftState.value.photoPath
        if (deleteDraftPhotoIfUnsaved && currentDraftPhoto != null) {
            PhotoStorage.deleteFileQuietly(currentDraftPhoto)
        }
        _draftState.value = SaveSpotDraftState(isVisible = false)
    }

    fun refreshDraftGps() {
        viewModelScope.launch {
            _draftState.update {
                it.copy(
                    gpsState = GpsAcquisitionState.Loading,
                    capturedAtMillis = System.currentTimeMillis(),
                )
            }
            val result = locationTracker.fetchCurrentLocation()
            _draftState.update { state ->
                when (result) {
                    is LocationResult.Success -> state.copy(
                        gpsState = GpsAcquisitionState.Locked(
                            latitude = result.latitude,
                            longitude = result.longitude,
                            accuracyMeters = result.accuracyMeters,
                        ),
                        capturedAtMillis = System.currentTimeMillis(),
                    )
                    LocationResult.PermissionDenied -> state.copy(
                        gpsState = GpsAcquisitionState.Failed(
                            message = "위치 권한이 거부되어 GPS 좌표를 가져올 수 없습니다.",
                            isPermissionIssue = true,
                        ),
                    )
                    is LocationResult.Unavailable -> state.copy(
                        gpsState = GpsAcquisitionState.Failed(
                            message = result.reason,
                            isPermissionIssue = false,
                        ),
                    )
                }
            }
        }
    }

    fun updateDraftMemo(memo: String) {
        _draftState.update { it.copy(memo = memo) }
    }

    fun appendDraftQuickTag(tag: String) {
        _draftState.update { state ->
            val current = state.memo.trim()
            val updated = if (current.isEmpty()) tag else "$current $tag"
            state.copy(memo = updated)
        }
    }

    fun setDraftPhotoPath(newPath: String?) {
        val oldPath = _draftState.value.photoPath
        if (oldPath != null && oldPath != newPath) {
            PhotoStorage.deleteFileQuietly(oldPath)
        }
        _draftState.update { it.copy(photoPath = newPath) }
    }

    fun confirmSaveDraft() {
        val draft = _draftState.value
        if (draft.isSaving) return

        val locked = draft.gpsState as? GpsAcquisitionState.Locked
        if (locked == null && draft.memo.isBlank() && draft.photoPath == null) {
            _uiMessages.tryEmit("GPS 좌표가 없을 때는 층수·구역 메모나 기둥 사진 중 하나를 입력해 주세요.")
            return
        }

        viewModelScope.launch {
            _draftState.update { it.copy(isSaving = true) }
            repository.saveNewSpot(
                latitude = locked?.latitude,
                longitude = locked?.longitude,
                accuracyMeters = locked?.accuracyMeters,
                memo = draft.memo,
                photoPath = draft.photoPath,
                parkedAtMillis = draft.capturedAtMillis,
            )
            _draftState.value = SaveSpotDraftState(isVisible = false)
            _uiMessages.emit("현재 주차 위치가 저장되었습니다.")
        }
    }

    fun updateActiveMemo(newMemo: String) {
        val record = activeRecord.value ?: return
        viewModelScope.launch {
            repository.updateMemo(record.id, newMemo)
            _uiMessages.emit("층수·구역 메모가 수정되었습니다.")
        }
    }

    fun attachPhotoToActiveRecord(photoPath: String) {
        val record = activeRecord.value ?: return
        viewModelScope.launch {
            repository.updatePhoto(record.id, photoPath)
            _uiMessages.emit("기둥 사진이 저장되었습니다.")
        }
    }

    fun removeActivePhoto() {
        val record = activeRecord.value ?: return
        viewModelScope.launch {
            repository.updatePhoto(record.id, null)
            _uiMessages.emit("기둥 사진이 삭제되었습니다.")
        }
    }

    fun clearActiveSpot() {
        viewModelScope.launch {
            repository.clearActiveSpot()
            _uiMessages.emit("출차 완료! 주차 위치가 기록으로 이동되었습니다.")
        }
    }

    fun restoreRecordAsActive(record: ParkingRecord) {
        viewModelScope.launch {
            repository.restoreAsActive(record)
            _uiMessages.emit("선택한 주차 위치를 현재 위치로 불러왔습니다.")
        }
    }

    fun deleteHistoryRecord(record: ParkingRecord) {
        viewModelScope.launch {
            repository.deleteHistoryRecord(record)
        }
    }

    fun setPreferredMapApp(app: PreferredMapApp) {
        viewModelScope.launch {
            repository.setPreferredMapApp(app)
            _uiMessages.emit("기본 길찾기 지도: ${app.shortLabel}")
        }
    }

    fun emitMessage(message: String) {
        _uiMessages.tryEmit(message)
    }
}
