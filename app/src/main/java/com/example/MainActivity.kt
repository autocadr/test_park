package com.example

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.camera.PendingPhotoCapture
import com.example.camera.PhotoStorage
import com.example.data.ParkingRecord
import com.example.navigation.MapNavigator
import com.example.ui.ParkingViewModel
import com.example.ui.components.ClearSpotConfirmDialog
import com.example.ui.components.EditMemoDialog
import com.example.ui.components.HistoryBottomSheet
import com.example.ui.components.MapAppPickerDialog
import com.example.ui.components.PhotoViewerDialog
import com.example.ui.components.SaveSpotBottomSheet
import com.example.ui.screens.ParkingHomeScreen
import com.example.ui.theme.ParkFinderTheme
import com.example.util.ParkingFormatters

private enum class CameraLaunchTarget {
    ACTIVE_RECORD,
    DRAFT_SHEET,
    NEW_SPOT_FROM_EMPTY,
}

class MainActivity : ComponentActivity() {

    private val viewModel: ParkingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ParkFinderTheme {
                val context = LocalContext.current
                val activeRecord by viewModel.activeRecord.collectAsStateWithLifecycle()
                val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()
                val preferredMapApp by viewModel.preferredMapApp.collectAsStateWithLifecycle()
                val installedMapApps by viewModel.installedMapApps.collectAsStateWithLifecycle()
                val draftState by viewModel.draftState.collectAsStateWithLifecycle()

                val snackbarHostState = remember { SnackbarHostState() }

                var showEditMemoDialog by remember { mutableStateOf(false) }
                var showClearConfirmDialog by remember { mutableStateOf(false) }
                var showMapPickerDialog by remember { mutableStateOf(false) }
                var showHistorySheet by remember { mutableStateOf(false) }
                var previewPhotoRecord by remember { mutableStateOf<ParkingRecord?>(null) }

                var pendingCapture by remember { mutableStateOf<PendingPhotoCapture?>(null) }
                var cameraTarget by remember { mutableStateOf(CameraLaunchTarget.ACTIVE_RECORD) }

                LaunchedEffect(Unit) {
                    viewModel.uiMessages.collect { message ->
                        snackbarHostState.showSnackbar(message)
                    }
                }

                // Location Permission Launcher
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions(),
                ) { permissions ->
                    val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                        permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                    if (granted) {
                        if (draftState.isVisible) {
                            viewModel.refreshDraftGps()
                        } else {
                            viewModel.openSaveSheet()
                        }
                    } else {
                        if (!draftState.isVisible) {
                            viewModel.openSaveSheet()
                        }
                        viewModel.emitMessage("위치 권한이 거부되어 GPS 좌표를 가져올 수 없습니다.")
                    }
                }

                // Camera TakePicture Launcher
                val takePictureLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.TakePicture(),
                ) { success ->
                    val capture = pendingCapture
                    pendingCapture = null
                    if (success && capture != null && PhotoStorage.isCapturedFileValid(capture.file)) {
                        val path = capture.file.absolutePath
                        when (cameraTarget) {
                            CameraLaunchTarget.ACTIVE_RECORD -> viewModel.attachPhotoToActiveRecord(path)
                            CameraLaunchTarget.DRAFT_SHEET -> viewModel.setDraftPhotoPath(path)
                            CameraLaunchTarget.NEW_SPOT_FROM_EMPTY -> viewModel.openSaveSheet(initialPhotoPath = path)
                        }
                    } else {
                        capture?.file?.let { PhotoStorage.deleteFileQuietly(it.absolutePath) }
                    }
                }

                fun launchCameraNow(target: CameraLaunchTarget) {
                    cameraTarget = target
                    val targetCapture = PhotoStorage.createCaptureTarget(context)
                    pendingCapture = targetCapture
                    takePictureLauncher.launch(targetCapture.contentUri)
                }

                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission(),
                ) { granted ->
                    if (granted) {
                        launchCameraNow(cameraTarget)
                    } else {
                        viewModel.emitMessage(getString(R.string.permission_camera_rationale))
                    }
                }

                fun requestCameraFor(target: CameraLaunchTarget) {
                    cameraTarget = target
                    if (viewModel.hasCameraPermission()) {
                        launchCameraNow(target)
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                }

                fun requestOpenSaveSheet() {
                    if (viewModel.hasLocationPermission()) {
                        viewModel.openSaveSheet()
                    } else {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                            ),
                        )
                    }
                }

                ParkingHomeScreen(
                    activeRecord = activeRecord,
                    preferredMapApp = preferredMapApp,
                    snackbarHostState = snackbarHostState,
                    onSaveCurrentLocationClick = { requestOpenSaveSheet() },
                    onNavigateToCarClick = { record ->
                        val lat = record.latitude
                        val lng = record.longitude
                        if (lat == null || lng == null) {
                            viewModel.emitMessage("저장된 GPS 좌표가 없어 길찾기를 실행할 수 없습니다.")
                        } else {
                            val result = MapNavigator.launchWalkingDirections(
                                context = context,
                                latitude = lat,
                                longitude = lng,
                                label = record.memo.ifBlank { "내 차 주차 위치" },
                                preferredApp = preferredMapApp,
                            )
                            result.fallbackNotice?.let { viewModel.emitMessage(it) }
                            result.errorMessage?.let { viewModel.emitMessage(it) }
                        }
                    },
                    onTakePillarPhotoClick = {
                        val target = if (activeRecord == null) {
                            CameraLaunchTarget.NEW_SPOT_FROM_EMPTY
                        } else {
                            CameraLaunchTarget.ACTIVE_RECORD
                        }
                        requestCameraFor(target)
                    },
                    onRemovePhotoClick = { viewModel.removeActivePhoto() },
                    onPhotoPreviewClick = { record -> previewPhotoRecord = record },
                    onEditMemoClick = { showEditMemoDialog = true },
                    onClearSpotClick = { showClearConfirmDialog = true },
                    onCopyCoordinatesClick = { record ->
                        val coords = ParkingFormatters.formatCoordinates(record.latitude, record.longitude)
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clipboard.setPrimaryClip(ClipData.newPlainText("주차 GPS 좌표", coords))
                        viewModel.emitMessage("GPS 좌표가 복사되었습니다: $coords")
                    },
                    onOpenMapPickerClick = {
                        viewModel.refreshInstalledMapApps()
                        showMapPickerDialog = true
                    },
                    onOpenHistoryClick = { showHistorySheet = true },
                )

                if (draftState.isVisible) {
                    SaveSpotBottomSheet(
                        draftState = draftState,
                        onMemoChange = viewModel::updateDraftMemo,
                        onQuickTagClick = viewModel::appendDraftQuickTag,
                        onRetryGps = viewModel::refreshDraftGps,
                        onRequestLocationPermission = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION,
                                ),
                            )
                        },
                        onTakePhotoClick = { requestCameraFor(CameraLaunchTarget.DRAFT_SHEET) },
                        onRemovePhotoClick = { viewModel.setDraftPhotoPath(null) },
                        onConfirmSave = viewModel::confirmSaveDraft,
                        onDismiss = { viewModel.dismissSaveSheet(deleteDraftPhotoIfUnsaved = true) },
                    )
                }

                if (showEditMemoDialog && activeRecord != null) {
                    EditMemoDialog(
                        initialMemo = activeRecord?.memo.orEmpty(),
                        onSave = { updated ->
                            viewModel.updateActiveMemo(updated)
                            showEditMemoDialog = false
                        },
                        onDismiss = { showEditMemoDialog = false },
                    )
                }

                if (showClearConfirmDialog) {
                    ClearSpotConfirmDialog(
                        onConfirmClear = {
                            viewModel.clearActiveSpot()
                            showClearConfirmDialog = false
                        },
                        onDismiss = { showClearConfirmDialog = false },
                    )
                }

                if (showMapPickerDialog) {
                    MapAppPickerDialog(
                        selectedApp = preferredMapApp,
                        installedApps = installedMapApps,
                        onSelectApp = viewModel::setPreferredMapApp,
                        onDismiss = { showMapPickerDialog = false },
                    )
                }

                if (showHistorySheet) {
                    HistoryBottomSheet(
                        records = historyRecords,
                        onRestoreRecord = viewModel::restoreRecordAsActive,
                        onDeleteRecord = viewModel::deleteHistoryRecord,
                        onDismiss = { showHistorySheet = false },
                    )
                }

                val photoRecord = previewPhotoRecord
                if (photoRecord?.photoPath != null) {
                    PhotoViewerDialog(
                        photoPath = photoRecord.photoPath,
                        memo = photoRecord.memo,
                        onDismiss = { previewPhotoRecord = null },
                    )
                }
            }
        }
    }
}
