package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.R
import com.example.data.ParkingRecord
import com.example.data.PreferredMapApp
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditMemoDialog(
    initialMemo: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var memoText by remember(initialMemo) { mutableStateOf(initialMemo) }
    val quickTags = listOf("지하 1층", "지하 2층", "지하 3층", "A구역", "B구역", "C구역", "기둥")

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        titleContentColor = PureWhite,
        textContentColor = IceWhite,
        title = {
            Text(
                text = "층수 / 구역 메모 수정",
                style = MaterialTheme.typography.titleLarge,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = memoText,
                    onValueChange = { memoText = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.memo_hint),
                            color = IceWhite.copy(alpha = 0.6f),
                        )
                    },
                    singleLine = true,
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
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    quickTags.forEach { tag ->
                        QuickTagChip(
                            label = tag,
                            onClick = {
                                val trimmed = memoText.trim()
                                memoText = if (trimmed.isEmpty()) tag else "$trimmed $tag"
                            },
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(memoText) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = SunYellow,
                    contentColor = NavyBackground,
                ),
            ) {
                Text(stringResource(R.string.save), fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = PureWhite)
            }
        },
    )
}

@Composable
fun ClearSpotConfirmDialog(
    onConfirmClear: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        titleContentColor = PureWhite,
        textContentColor = IceWhite,
        title = {
            Text("출차 완료 처리할까요?", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Text(
                "현재 저장된 주차 위치가 홈 화면에서 해제되고 최근 기록에 보관됩니다.",
                style = MaterialTheme.typography.bodyLarge,
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirmClear,
                colors = ButtonDefaults.buttonColors(
                    containerColor = SunYellow,
                    contentColor = NavyBackground,
                ),
            ) {
                Text("출차 완료", fontWeight = FontWeight.ExtraBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = PureWhite)
            }
        },
    )
}

@Composable
fun MapAppPickerDialog(
    selectedApp: PreferredMapApp,
    installedApps: Set<PreferredMapApp>,
    onSelectApp: (PreferredMapApp) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = NavySurface,
        titleContentColor = PureWhite,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Icon(Icons.Default.Map, contentDescription = null, tint = SunYellow)
                Text("도보 길찾기 지도 앱", style = MaterialTheme.typography.titleLarge)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PreferredMapApp.entries.forEach { app ->
                    val isSelected = app == selectedApp
                    val isInstalled = app == PreferredMapApp.AUTO || app in installedApps
                    Surface(
                        onClick = {
                            onSelectApp(app)
                            onDismiss()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) NavySurfaceElevated else NavyBackground,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) SunYellow else NavyBorder,
                        ),
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.displayName,
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        color = if (isSelected) SunYellow else PureWhite,
                                    ),
                                )
                                if (app != PreferredMapApp.AUTO) {
                                    Text(
                                        text = if (isInstalled) "기기에 설치됨" else "미설치 (선택 시 자동 대체)",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            color = if (isInstalled) SuccessGreen else IceWhite.copy(alpha = 0.7f),
                                        ),
                                    )
                                }
                            }
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "선택됨",
                                    tint = SunYellow,
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.close), color = SunYellow, fontWeight = FontWeight.Bold)
            }
        },
    )
}

@Composable
fun PhotoViewerDialog(
    photoPath: String,
    memo: String,
    onDismiss: () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NavyBackground)
                .statusBarsPadding()
                .navigationBarsPadding(),
        ) {
            AsyncImage(
                model = File(photoPath),
                contentDescription = "확대된 주차 기둥 사진",
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, _ ->
                            val newScale = (scale * zoom).coerceIn(1f, 4f)
                            scale = newScale
                            offset = if (newScale > 1f) offset + pan else Offset.Zero
                        }
                    }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offset.x,
                        translationY = offset.y,
                    ),
                contentScale = ContentScale.Fit,
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (memo.isNotBlank()) {
                    Surface(
                        color = NavySurface.copy(alpha = 0.9f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.5.dp, SunYellow),
                    ) {
                        Text(
                            text = memo,
                            style = MaterialTheme.typography.titleMedium.copy(color = SunYellow),
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        )
                    }
                } else {
                    Spacer(Modifier.width(1.dp))
                }

                Surface(
                    onClick = onDismiss,
                    shape = CircleShape,
                    color = NavySurface,
                    border = BorderStroke(2.dp, PureWhite),
                    modifier = Modifier.size(48.dp),
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = stringResource(R.string.close),
                            tint = PureWhite,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryBottomSheet(
    records: List<ParkingRecord>,
    onRestoreRecord: (ParkingRecord) -> Unit,
    onDeleteRecord: (ParkingRecord) -> Unit,
    onDismiss: () -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = NavySurface,
        contentColor = PureWhite,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp),
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
                    Icon(Icons.Default.History, contentDescription = null, tint = SunYellow)
                    Text("최근 주차 기록", style = MaterialTheme.typography.headlineSmall)
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = PureWhite)
                }
            }

            if (records.isEmpty()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = NavyBackground,
                    border = BorderStroke(1.dp, NavyBorder),
                ) {
                    Text(
                        text = "아직 저장된 주차 기록이 없습니다.",
                        style = MaterialTheme.typography.bodyLarge.copy(color = IceWhite),
                        modifier = Modifier.padding(20.dp),
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    items(records, key = { it.id }) { record ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            color = NavyBackground,
                            border = BorderStroke(
                                width = if (record.isActive) 2.dp else 1.dp,
                                color = if (record.isActive) SunYellow else NavyBorder,
                            ),
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    ) {
                                        if (record.isActive) {
                                            Surface(
                                                color = SuccessGreenContainer,
                                                shape = RoundedCornerShape(6.dp),
                                            ) {
                                                Text(
                                                    text = "현재 위치",
                                                    style = MaterialTheme.typography.labelMedium.copy(
                                                        color = SuccessGreen,
                                                    ),
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                                )
                                            }
                                        }
                                        Text(
                                            text = record.memo.ifBlank { "메모 없음" },
                                            style = MaterialTheme.typography.titleMedium.copy(color = PureWhite),
                                        )
                                        if (record.photoPath != null) {
                                            Icon(
                                                Icons.Default.PhotoCamera,
                                                contentDescription = "사진 포함",
                                                tint = SunYellow,
                                                modifier = Modifier.size(16.dp),
                                            )
                                        }
                                    }
                                    Text(
                                        text = ParkingFormatters.formatDateTime(record.parkedAtMillis),
                                        style = MaterialTheme.typography.bodyMedium.copy(color = IceWhite),
                                    )
                                    Text(
                                        text = ParkingFormatters.formatCoordinates(record.latitude, record.longitude),
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontFamily = JetBrainsMonoFamily,
                                            color = IceWhite.copy(alpha = 0.85f),
                                        ),
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (!record.isActive) {
                                        TextButton(onClick = {
                                            onRestoreRecord(record)
                                            onDismiss()
                                        }) {
                                            Text("불러오기", color = SunYellow, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    IconButton(
                                        onClick = { onDeleteRecord(record) },
                                        modifier = Modifier.size(48.dp),
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = "기록 삭제",
                                            tint = ErrorCoral,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
