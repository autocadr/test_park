package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.GpsAcquisitionState
import com.example.ui.SaveSpotDraftState
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun SaveSpotBottomSheet(
    draftState: SaveSpotDraftState,
    onMemoChange: (String) -> Unit,
    onQuickTagClick: (String) -> Unit,
    onRetryGps: () -> Unit,
    onRequestLocationPermission: () -> Unit,
    onTakePhotoClick: () -> Unit,
    onRemovePhotoClick: () -> Unit,
    onConfirmSave: () -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val quickTags = listOf("지하 1층", "지하 2층", "지하 3층", "지상", "옥상", "A구역", "B구역", "C구역", "기둥")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NavySurface,
        contentColor = PureWhite,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.save_current_location_cta),
                    style = MaterialTheme.typography.headlineSmall.copy(color = PureWhite),
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp),
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(R.string.close),
                        tint = PureWhite,
                    )
                }
            }

            // GPS Status Card
            GpsDraftStatusCard(
                gpsState = draftState.gpsState,
                capturedAtMillis = draftState.capturedAtMillis,
                onRetryGps = onRetryGps,
                onRequestPermission = onRequestLocationPermission,
            )

            // Memo Input
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = stringResource(R.string.memo_label),
                    style = MaterialTheme.typography.titleMedium.copy(color = SunYellow),
                )
                OutlinedTextField(
                    value = draftState.memo,
                    onValueChange = onMemoChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("memo_input_field"),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.memo_hint),
                            color = IceWhite.copy(alpha = 0.65f),
                        )
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.titleMedium.copy(color = PureWhite),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SunYellow,
                        unfocusedBorderColor = NavyBorder,
                        focusedContainerColor = NavyBackground,
                        unfocusedContainerColor = NavyBackground,
                        cursorColor = SunYellow,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                    ),
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    quickTags.forEach { tag ->
                        QuickTagChip(
                            label = tag,
                            onClick = { onQuickTagClick(tag) },
                        )
                    }
                }
            }

            // Pillar Photo Attachment
            if (draftState.photoPath != null) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = NavyBackground,
                    border = BorderStroke(1.5.dp, NavyBorder),
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        AsyncImage(
                            model = File(draftState.photoPath),
                            contentDescription = "촬영된 주차 기둥 사진",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            contentScale = ContentScale.Crop,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            OutlinedButton(
                                onClick = onTakePhotoClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .height(48.dp),
                                border = BorderStroke(1.5.dp, PureWhite),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = PureWhite)
                                Spacer(Modifier.width(6.dp))
                                Text("다시 찍기", color = PureWhite, fontWeight = FontWeight.Bold)
                            }
                            OutlinedButton(
                                onClick = onRemovePhotoClick,
                                modifier = Modifier.height(48.dp),
                                border = BorderStroke(1.5.dp, ErrorCoral),
                                shape = RoundedCornerShape(12.dp),
                            ) {
                                Icon(
                                    Icons.Default.DeleteOutline,
                                    contentDescription = "사진 삭제",
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
                    onClick = onTakePhotoClick,
                    modifier = Modifier.testTag("draft_take_photo_button"),
                    minHeight = 56.dp,
                )
            }

            // Confirm Save CTA
            PrimaryYellowActionButton(
                text = if (draftState.isSaving) "저장 중…" else stringResource(R.string.confirm_save_spot),
                icon = Icons.Default.CheckCircle,
                onClick = onConfirmSave,
                enabled = !draftState.isSaving,
                modifier = Modifier.testTag("confirm_save_button"),
                minHeight = 68.dp,
            )
        }
    }
}

@Composable
private fun GpsDraftStatusCard(
    gpsState: GpsAcquisitionState,
    capturedAtMillis: Long,
    onRetryGps: () -> Unit,
    onRequestPermission: () -> Unit,
) {
    val borderColor = when (gpsState) {
        is GpsAcquisitionState.Locked -> SuccessGreen
        is GpsAcquisitionState.Failed -> AmberSignal
        else -> NavyBorder
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = NavyBackground,
        border = BorderStroke(1.5.dp, borderColor),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    when (gpsState) {
                        GpsAcquisitionState.Idle, GpsAcquisitionState.Loading -> {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = SunYellow,
                                strokeWidth = 2.5.dp,
                            )
                            Text(
                                text = stringResource(R.string.gps_acquiring),
                                style = MaterialTheme.typography.titleMedium.copy(color = SunYellow),
                            )
                        }
                        is GpsAcquisitionState.Locked -> {
                            Surface(
                                color = SuccessGreenContainer,
                                shape = RoundedCornerShape(8.dp),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Icon(
                                        Icons.Default.GpsFixed,
                                        contentDescription = null,
                                        tint = SuccessGreen,
                                        modifier = Modifier.size(16.dp),
                                    )
                                    Text(
                                        text = stringResource(R.string.gps_locked),
                                        style = MaterialTheme.typography.labelMedium.copy(color = SuccessGreen),
                                    )
                                }
                            }
                            ParkingFormatters.formatAccuracy(gpsState.accuracyMeters)?.let { acc ->
                                Text(
                                    text = "오차 $acc",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontFamily = JetBrainsMonoFamily,
                                        color = IceWhite,
                                    ),
                                )
                            }
                        }
                        is GpsAcquisitionState.Failed -> {
                            Icon(
                                Icons.Default.WarningAmber,
                                contentDescription = null,
                                tint = AmberSignal,
                                modifier = Modifier.size(20.dp),
                            )
                            Text(
                                text = stringResource(R.string.gps_unavailable),
                                style = MaterialTheme.typography.titleMedium.copy(color = AmberSignal),
                            )
                        }
                    }
                }

                if (gpsState !is GpsAcquisitionState.Loading) {
                    TextButton(
                        onClick = if (gpsState is GpsAcquisitionState.Failed && gpsState.isPermissionIssue) {
                            onRequestPermission
                        } else {
                            onRetryGps
                        },
                    ) {
                        Icon(
                            imageVector = if (gpsState is GpsAcquisitionState.Failed && gpsState.isPermissionIssue) {
                                Icons.Default.MyLocation
                            } else {
                                Icons.Default.Refresh
                            },
                            contentDescription = null,
                            tint = SunYellow,
                            modifier = Modifier.size(18.dp),
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = if (gpsState is GpsAcquisitionState.Failed && gpsState.isPermissionIssue) {
                                "권한 허용"
                            } else {
                                "다시 측정"
                            },
                            color = SunYellow,
                            fontWeight = FontWeight.Bold,
                        )
                    }
                }
            }

            when (gpsState) {
                is GpsAcquisitionState.Locked -> {
                    Text(
                        text = ParkingFormatters.formatCoordinates(gpsState.latitude, gpsState.longitude),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontFamily = JetBrainsMonoFamily,
                            fontSize = 16.sp,
                            color = PureWhite,
                        ),
                    )
                }
                is GpsAcquisitionState.Failed -> {
                    Text(
                        text = gpsState.message,
                        style = MaterialTheme.typography.bodyMedium.copy(color = IceWhite),
                    )
                }
                else -> Unit
            }

            Text(
                text = "저장 시각: ${ParkingFormatters.formatDateTime(capturedAtMillis)}",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontFamily = JetBrainsMonoFamily,
                    color = IceWhite,
                ),
            )
        }
    }
}
