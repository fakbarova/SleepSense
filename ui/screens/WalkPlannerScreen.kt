package com.circadianx.sleepsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.WalkPlannerViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.ui.graphics.Color
import com.google.maps.android.PolyUtil

@Composable
fun WalkPlannerScreen(
    stepsRemaining: Int,
    onBack: () -> Unit,
    viewModel: WalkPlannerViewModel = hiltViewModel()
) {
    val colors = SleepSenseTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadOriginFromLocation()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        SsTopBar(tag = "Activity", title = "Plan a walk") {
            // Keep empty for now (back handled by system)
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            val origin = state.origin
            val destination = state.destination

            val cameraState = rememberCameraPositionState()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(colors.bgCard)
            ) {
                if (origin != null) {
                    GoogleMap(
                        modifier = Modifier.fillMaxSize(),
                        cameraPositionState = cameraState,
                        onMapClick = { latLng ->
                            viewModel.setDestination(latLng.latitude, latLng.longitude)
                        }
                    ) {
                        Marker(
                            state = com.google.maps.android.compose.rememberMarkerState(
                                position = LatLng(origin.lat, origin.lng)
                            ),
                            title = "You"
                        )
                        if (destination != null) {
                            Marker(
                                state = com.google.maps.android.compose.rememberMarkerState(
                                    position = LatLng(destination.lat, destination.lng)
                                ),
                                title = "Destination"
                            )
                        }
                        val poly = state.suggestion?.encodedPolyline
                        if (!poly.isNullOrBlank()) {
                            val decoded = PolyUtil.decode(poly)
                            Polyline(points = decoded, color = Color(0xFFB14CFF), width = 12f)
                        }
                    }
                } else {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = colors.purple)
                    }
                }
            }

            Button(
                onClick = { viewModel.planWalk(stepsRemaining) },
                enabled = state.origin != null && state.destination != null && !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = colors.bgDeep)
                } else {
                    Text("Get walking route", fontFamily = DmSans, fontSize = 15.sp)
                }
            }

            state.suggestion?.let { suggestion ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(colors.bgCard)
                        .padding(Spacing.l),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(suggestion.summary, fontFamily = DmSans, color = colors.textPrimary, fontSize = 14.sp)
                    Text(
                        text = "Distance: ${suggestion.distanceMeters}m • Time: ${suggestion.extraMinutes} min",
                        fontFamily = DmSans,
                        color = colors.textMuted,
                        fontSize = 12.sp
                    )
                }
            }

            if (!state.error.isNullOrBlank()) {
                Text(state.error.orEmpty(), fontFamily = DmSans, color = colors.red, fontSize = 12.sp)
            }

            Spacer(Modifier.height(Spacing.l))
        }
    }
}

