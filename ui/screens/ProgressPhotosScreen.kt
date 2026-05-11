package com.circadianx.sleepsense.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.data.local.db.entity.ProgressPhotoEntity
import com.circadianx.sleepsense.ui.components.SsEmptyState
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.ProgressPhotosViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressPhotosScreen(viewModel: ProgressPhotosViewModel = hiltViewModel()) {
    val state      by viewModel.uiState.collectAsStateWithLifecycle()
    val colors     = SleepSenseTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val photos     = state.photos

    var detailIndex by remember { mutableIntStateOf(-1) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        SsTopBar(tag = "Progress", title = "Timelapse")

        if (photos.isEmpty()) {
            Box(
                modifier         = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                SsEmptyState(
                    icon  = Icons.Filled.CameraAlt,
                    title = "No progress photos yet",
                    body  = "Take a photo inside a challenge to build your timelapse gallery."
                )
            }
        } else {
            LazyVerticalGrid(
                columns         = GridCells.Fixed(3),
                modifier        = Modifier.fillMaxSize(),
                contentPadding  = PaddingValues(Spacing.m),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s),
                verticalArrangement   = Arrangement.spacedBy(Spacing.s)
            ) {
                items(photos, key = { it.id }) { photo ->
                    val bytes  = viewModel.loadDecryptedJpeg(photo)
                    val bitmap = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
                    val idx    = photos.indexOf(photo)

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(colors.bgCard)
                            .border(1.dp, colors.border, RoundedCornerShape(10.dp))
                            .clickable { detailIndex = idx }
                    ) {
                        if (bitmap != null) {
                            Image(
                                bitmap             = bitmap.asImageBitmap(),
                                contentDescription = "Progress photo",
                                modifier           = Modifier.fillMaxSize(),
                                contentScale       = ContentScale.Crop
                            )
                        }
                        // Date overlay
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .background(colors.bgDeep.copy(alpha = 0.55f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text       = SimpleDateFormat("MMM d", Locale.getDefault())
                                    .format(Date(photo.takenAtMs)),
                                fontFamily = JetBrainsMono,
                                fontSize   = 8.sp,
                                color      = colors.textSecondary
                            )
                        }
                    }
                }
            }
        }
    }

    // Detail bottom sheet
    if (detailIndex >= 0 && detailIndex <= photos.lastIndex) {
        ModalBottomSheet(
            onDismissRequest = { detailIndex = -1 },
            sheetState       = sheetState,
            containerColor   = colors.bgCard,
            dragHandle       = {
                Box(
                    modifier = Modifier
                        .padding(top = 12.dp, bottom = 8.dp)
                        .size(width = 36.dp, height = 4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(colors.border)
                )
            }
        ) {
            PhotoDetailContent(
                photos      = photos,
                index       = detailIndex,
                onPrev      = { if (detailIndex > 0) detailIndex-- },
                onNext      = { if (detailIndex < photos.lastIndex) detailIndex++ },
                loadBitmap  = viewModel::loadDecryptedJpeg
            )
        }
    }
}

@Composable
private fun PhotoDetailContent(
    photos: List<ProgressPhotoEntity>,
    index: Int,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    loadBitmap: (ProgressPhotoEntity) -> ByteArray?
) {
    val colors  = SleepSenseTheme.colors
    val photo   = photos[index]
    val bytes   = loadBitmap(photo)
    val bitmap  = bytes?.let { BitmapFactory.decodeByteArray(it, 0, it.size) }
    val dateFmt = SimpleDateFormat("EEEE, MMM d yyyy", Locale.getDefault())

    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(Spacing.l),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (bitmap != null) {
            Image(
                bitmap             = bitmap.asImageBitmap(),
                contentDescription = "Progress photo detail",
                modifier           = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .clip(RoundedCornerShape(14.dp)),
                contentScale       = ContentScale.Crop
            )
        }
        Spacer(Modifier.height(Spacing.m))
        Text(
            text       = dateFmt.format(Date(photo.takenAtMs)),
            fontFamily = DmSans,
            fontWeight = FontWeight.Medium,
            fontSize   = 14.sp,
            color      = colors.textPrimary
        )
        Text(
            text       = "${index + 1} / ${photos.size}",
            fontFamily = JetBrainsMono,
            fontSize   = 10.sp,
            color      = colors.textMuted
        )
        Spacer(Modifier.height(Spacing.l))
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            Button(
                onClick  = onPrev,
                enabled  = index > 0,
                modifier = Modifier.weight(1f).height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = colors.bgBase)
            ) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Previous", tint = colors.textPrimary, modifier = Modifier.size(16.dp))
                androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                Text("Prev", fontFamily = DmSans, color = colors.textPrimary)
            }
            Button(
                onClick  = onNext,
                enabled  = index < photos.lastIndex,
                modifier = Modifier.weight(1f).height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = colors.purple)
            ) {
                Text("Next", fontFamily = DmSans)
                androidx.compose.foundation.layout.Spacer(Modifier.size(4.dp))
                Icon(Icons.Filled.ArrowForward, contentDescription = "Next", modifier = Modifier.size(16.dp))
            }
        }
        Spacer(Modifier.height(Spacing.xxl))
    }
}
