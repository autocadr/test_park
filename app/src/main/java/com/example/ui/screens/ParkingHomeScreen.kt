package com.example.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.AddLocationAlt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircleOutline
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ParkingRecord
import com.example.data.PreferredMapApp
import com.example.ui.components.PrimaryYellowActionButton
import com.example.ui.components.SecondaryWhiteActionButton
import com.example.ui.theme.AmberSignal
import com.example.ui.theme.ErrorCoral
import com.example.ui.theme.IceWhite
import com.example.ui.theme.JetBrainsMonoFamily
import com.example.ui.theme.NavyBackground
import com.example.ui.theme.NavyBorder
import com.example.ui.theme.NavySurface
import com.example.ui.theme.NavySurfaceElevated
import com.example.ui.theme.PureWhite
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessGreenContainer
import com.example.ui.theme.SunYellow
import com.example.util.ParkingFormatters
import java.io.File

@Composable
fun ParkingHomeScreen(
    activeRecord: ParkingRecord?,
    preferredMapApp: PreferredMapApp,
    snackbarHostState: SnackbarHostState,
    onSaveCurrentLocationClick: () -> Unit,
    onNavigateToCarClick: (ParkingRecord) -> Unit,
    onTakePillarPhotoClick: () -> Unit,
    onRemovePhotoClick: () -> Unit,
    onPhotoPreviewClick: (ParkingRecord) -> Unit,
    onEditMemoClick: () -> Unit,
    onClearSpotClick: () -> Unit,
    onCopyCoordinatesClick: (ParkingRecord) -> Unit,
    onOpenMapPickerClick: () -> Unit,
    onOpenHistoryClick: () -> Unit,
) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = NavyBackground,
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(NavyBackground, NavySurface.copy(alpha = 0.7f), NavyBackground),
                    ),
                )
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = 600.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                // Top Header Bar
                TopBeaconHeader(
                    preferredMapApp = preferredMapApp,
                    onOpenMapPickerClick = onOpenMapPickerClick,
                    onOpenHistoryClick = onOpenHistoryClick,
                )

                AnimatedContent(
                    targetState = activeRecord,
                    label = "parking_state_transition",
                ) { record ->
                    if (record == null) {
                        EmptyParkingContent(
                            onSaveCurrentLocationClick = onSaveCurrentLocationClick,
                            onTakePillarPhotoClick = onTakePillarPhotoClick,
                        )
                    } else {
                        ActiveParkingContent(
                            record = record,
                            preferredMapApp = preferredMapApp,
                            onNavigateToCarClick = { onNavigateToCarClick(record) },
                            onTakePillarPhotoClick = onTakePillarPhotoClick,
                            onRemovePhotoClick = onRemovePhotoClick,
                            onPhotoPreviewClick = { onPhotoPreviewClick(record) },
                            onEditMemoClick = onEditMemoClick,
                            onSaveNewSpotClick = onSaveCurrentLocationClick,
                            onClearSpotClick = onClearSpotClick,
                            onCopyCoordinatesClick = { onCopyCoordinatesClick(record) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBeaconHeader(
    preferredMapApp: PreferredMapApp,
    onOpenMapPickerClick: () -> Unit,
    onOpenHistoryClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = SunYellow,
                modifier = Modifier.size(46.dp),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.DirectionsCar,
                        contentDescription = null,
                        tint = NavyBackground,
                        modifier = Modifier.size(28.dp),
                    )
                }
            }
            Column {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                )
                Text(
                    text = "고대비 GPS 주차 기억기",
                    style = MaterialTheme.typography.labelMedium.copy(
                        color = SunYellow,
                    ),
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Surface(
                onClick = onOpenMapPickerClick,
                modifier = Modifier
                    .height(48.dp)
                    .testTag("map_picker_button"),
                shape = RoundedCornerShape(14.dp),
                color = NavySurfaceElevated,
                border = BorderStroke(1.5.dp, PureWhite),
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = stringResource(R.string.map_selector_cd),
                        tint = SunYellow,
                        modifier = Modifier.size(18.dp),
                    )
                    Text(
                        text = preferredMapApp.shortLabel,
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontSize = 13.sp,
                            color = PureWhite,
                        ),
                    )
                }
            }

            Surface(
                onClick = onOpenHistoryClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("history_button"),
                shape = RoundedCornerShape(14.dp),
                color = NavySurfaceElevated,
                border = BorderStroke(1.5.dp, NavyBorder),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = stringResource(R.string.history_button_cd),
                        tint = PureWhite,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyParkingContent(
    onSaveCurrentLocationClick: () -> Unit,
    onTakePillarPhotoClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = NavySurface,
            border = BorderStroke(2.dp, NavyBorder),
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_parking_empty),
                    contentDescription = "주차 위치 대기 일러스트",
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Crop,
                )
                Text(
                    text = stringResource(R.string.empty_state_title),
                    style = MaterialTheme.typography.headlineMedium.copy(
                        color = PureWhite,
                        fontWeight = FontWeight.ExtraBold,
                    ),
                )
                Text(
                    text = stringResource(R.string.empty_state_body),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        color = IceWhite,
                        lineHeight = 24.sp,
                    ),
                )
            }
        }

        PrimaryYellowActionButton(
            text = stringResource(R.string.save_current_location_cta),
            subtitle = "GPS 좌표 · 시각 · 층수 메모 즉시 저장",
            icon = Icons.Default.AddLocationAlt,
            onClick = onSaveCurrentLocationClick,
            modifier = Modifier.testTag("save_parking_button"),
            minHeight = 76.dp,
        )

        SecondaryWhiteActionButton(
            text = stringResource(R.string.take_pillar_photo_cta),
            icon = Icons.Default.CameraAlt,
            onClick = onTakePillarPhotoClick,
            modifier = Modifier.testTag("empty_take_photo_button"),
            minHeight = 62.dp,
        )
    }
}

@Composable
private fun ActiveParkingContent(
    record: ParkingRecord,
    preferredMapApp: PreferredMapApp,
    onNavigateToCarClick: () -> Unit,
    onTakePillarPhotoClick: () -> Unit,
    onRemovePhotoClick: () -> Unit,
    onPhotoPreviewClick: () -> Unit,
    onEditMemoClick: () -> Unit,
    onSaveNewSpotClick: () -> Unit,
    onClearSpotClick: () -> Unit,
    onCopyCoordinatesClick: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Active Spot Summary Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = NavySurface,
            border = BorderStroke(2.dp, SunYellow),
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
            ) {
                // Status Pills Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Surface(
                        color = SunYellow,
                        shape = RoundedCornerShape(10.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.Schedule,
                                contentDescription = null,
                                tint = NavyBackground,
                                modifier = Modifier.size(16.dp),
                            )
                            Text(
                                text = "주차 중 · ${ParkingFormatters.formatElapsedDuration(record.parkedAtMillis)}",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NavyBackground,
                                ),
                            )
                        }
                    }

                    if (record.hasCoordinates) {
                        Surface(
                            color = SuccessGreenContainer,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, SuccessGreen),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.GpsFixed,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = ParkingFormatters.formatAccuracy(record.accuracyMeters)
                                        ?.let { "GPS $it" } ?: "GPS 저장됨",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        color = SuccessGreen,
                                        fontFamily = JetBrainsMonoFamily,
                                    ),
                                )
                            }
                        }
                    } else {
                        Surface(
                            color = NavySurfaceElevated,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, AmberSignal),
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOff,
                                    contentDescription = null,
                                    tint = AmberSignal,
                                    modifier = Modifier.size(15.dp),
                                )
                                Text(
                                    text = "좌표 없음",
                                    style = MaterialTheme.typography.labelMedium.copy(color = AmberSignal),
                                )
                            }
                        }
                    }
                }

                // Floor / Zone Memo Headline
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "층수 / 구역 메모",
                            style = MaterialTheme.typography.labelMedium.copy(color = IceWhite),
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = record.memo.ifBlank { "메모 없음 (수정 눌러 입력)" },
                            style = MaterialTheme.typography.displaySmall.copy(
                                fontSize = 28.sp,
                                lineHeight = 36.sp,
                                color = if (record.memo.isBlank()) IceWhite.copy(alpha = 0.7f) else PureWhite,
                            ),
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }

                    Surface(
                        onClick = onEditMemoClick,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("edit_memo_button"),
                        shape = RoundedCornerShape(14.dp),
                        color = NavySurfaceElevated,
                        border = BorderStroke(1.5.dp, PureWhite),
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "층수 및 구역 메모 수정",
                                tint = PureWhite,
                            )
                        }
                    }
                }

                // GPS Coordinates + Timestamp Box
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = NavyBackground,
                    border = BorderStroke(1.dp, NavyBorder),
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "저장된 GPS 위도 · 경도",
                                    style = MaterialTheme.typography.labelMedium.copy(color = IceWhite),
                                )
                                Text(
                                    text = ParkingFormatters.formatCoordinates(
                                        record.latitude,
                                        record.longitude,
                                    ),
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        color = SunYellow,
                                    ),
                                )
                            }
                            if (record.hasCoordinates) {
                                IconButton(
                                    onClick = onCopyCoordinatesClick,
                                    modifier = Modifier.size(48.dp),
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "GPS 좌표 복사",
                                        tint = PureWhite,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                            }
                        }

                        Text(
                            text = "저장 시각: ${ParkingFormatters.formatDateTime(record.parkedAtMillis)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontFamily = JetBrainsMonoFamily,
                                color = PureWhite,
                            ),
                        )
                    }
                }
            }
        }

        // Primary Walking Navigation Button (Sun Yellow High Contrast)
        PrimaryYellowActionButton(
            text = stringResource(R.string.navigate_to_car_cta),
            subtitle = if (record.hasCoordinates) {
                "${preferredMapApp.shortLabel} · 도보 길찾기 바로 안내"
            } else {
                "GPS 좌표가 없어 길찾기를 실행할 수 없습니다"
            },
            icon = Icons.AutoMirrored.Filled.DirectionsWalk,
            onClick = onNavigateToCarClick,
            enabled = record.hasCoordinates,
            modifier = Modifier.testTag("navigate_car_button"),
            minHeight = 76.dp,
        )

        // Pillar Photo Card or Capture Button
        if (record.photoPath != null && File(record.photoPath).exists()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = NavySurface,
                border = BorderStroke(1.5.dp, PureWhite),
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Text(
                            text = "저장된 주차 기둥 사진",
                            style = MaterialTheme.typography.titleMedium.copy(color = PureWhite),
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            Icon(
                                imageVector = Icons.Default.ZoomIn,
                                contentDescription = null,
                                tint = SunYellow,
                                modifier = Modifier.size(18.dp),
                            )
                            Text(
                                text = "눌러서 크게 보기",
                                style = MaterialTheme.typography.labelMedium.copy(color = SunYellow),
                            )
                        }
                    }

                    AsyncImage(
                        model = File(record.photoPath),
                        contentDescription = "저장된 주차 기둥 사진 크게 보기",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(210.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable(onClick = onPhotoPreviewClick)
                            .testTag("saved_pillar_photo"),
                        contentScale = ContentScale.Crop,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        OutlinedButton(
                            onClick = onTakePillarPhotoClick,
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("retake_photo_button"),
                            border = BorderStroke(1.5.dp, PureWhite),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = PureWhite)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = stringResource(R.string.retake_pillar_photo_cta),
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                            )
                        }
                        OutlinedButton(
                            onClick = onRemovePhotoClick,
                            modifier = Modifier.height(50.dp),
                            border = BorderStroke(1.5.dp, ErrorCoral),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(
                                Icons.Default.DeleteOutline,
                                contentDescription = "저장된 기둥 사진 삭제",
                                tint = ErrorCoral,
                            )
                        }
                    }
                }
            }
        } else {
            SecondaryWhiteActionButton(
                text = stringResource(R.string.take_pillar_photo_cta),
                icon = Icons.Default.CameraAlt,
                onClick = onTakePillarPhotoClick,
                modifier = Modifier.testTag("take_photo_button"),
                minHeight = 62.dp,
            )
        }

        // Bottom Secondary Actions Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SecondaryWhiteActionButton(
                text = stringResource(R.string.save_new_spot_cta),
                icon = Icons.Default.AddLocationAlt,
                onClick = onSaveNewSpotClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("overwrite_parking_button"),
                minHeight = 56.dp,
            )
            SecondaryWhiteActionButton(
                text = stringResource(R.string.clear_parking_cta),
                icon = Icons.Default.CheckCircleOutline,
                onClick = onClearSpotClick,
                modifier = Modifier
                    .weight(1f)
                    .testTag("clear_parking_button"),
                borderColor = SunYellow,
                contentColor = SunYellow,
                minHeight = 56.dp,
            )
        }
    }
}
