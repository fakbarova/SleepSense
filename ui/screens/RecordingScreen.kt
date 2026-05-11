package com.circadianx.sleepsense.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.viewmodel.DashboardViewModel
import kotlinx.coroutines.delay
import kotlin.math.sin

@Composable
fun RecordingScreen(
    onStop: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val liveDbfs by viewModel.liveDbfs.collectAsStateWithLifecycle()
    val colors = SleepSenseTheme.colors

    // Pulsing ring animation
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue  = 0.94f,
        targetValue   = 1.06f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    // Waveform driven by real mic amplitude
    val waveAmplitudes = remember { mutableStateListOf(*Array(32) { 0.05f }) }
    var elapsed by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(100)
            elapsed += 100
        }
    }

    // Shift waveform buffer and append current amplitude
    LaunchedEffect(liveDbfs) {
        val normalised = ((liveDbfs + 60f) / 60f).coerceIn(0.05f, 1f)
        if (waveAmplitudes.size >= 32) waveAmplitudes.removeAt(0)
        waveAmplitudes.add(normalised)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        AmbientSleepParticles()
        Column(
            modifier            = Modifier
                .fillMaxSize()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text          = "RECORDING",
            fontFamily    = JetBrainsMono,
            fontSize      = 11.sp,
            letterSpacing = 2.sp,
            color         = colors.purple
        )

        Spacer(Modifier.height(32.dp))

        // Pulsing emoji ring
        Box(contentAlignment = Alignment.Center, modifier = Modifier.size(160.dp)) {
            Canvas(modifier = Modifier.size(160.dp * pulseScale)) {
                drawCircle(
                    color  = colors.purple.copy(alpha = 0.15f),
                    radius = size.minDimension / 2f,
                )
            }
            Canvas(modifier = Modifier.size(120.dp)) {
                drawCircle(
                    color  = colors.purple,
                    radius = size.minDimension / 2f - 3.dp.toPx(),
                    style  = androidx.compose.ui.graphics.drawscope.Stroke(3.dp.toPx())
                )
            }
            Text(text = "😴", fontSize = 48.sp)
        }

        Spacer(Modifier.height(24.dp))

        Text(
            text       = formatElapsed(elapsed),
            fontFamily = DmSerifDisplay,
            fontSize   = 36.sp,
            color      = colors.textPrimary
        )

        Spacer(Modifier.height(6.dp))

        Text(
            text       = "Recording in progress…",
            fontFamily = DmSans,
            fontSize   = 14.sp,
            color      = colors.textSecondary
        )

        Spacer(Modifier.height(36.dp))

        // Waveform
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
        ) {
            val barWidth = size.width / waveAmplitudes.size
            val midY = size.height / 2f
            waveAmplitudes.forEachIndexed { i, amp ->
                val x = i * barWidth + barWidth / 2
                val h = amp * size.height / 2f
                drawLine(
                    color  = colors.purple.copy(alpha = 0.5f + amp * 0.5f),
                    start  = Offset(x, midY - h),
                    end    = Offset(x, midY + h),
                    strokeWidth = barWidth * 0.55f,
                    cap    = StrokeCap.Round
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Live stats
        Row(
            modifier              = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            val dbLabel = if (liveDbfs > -20f) "Snoring" else if (liveDbfs > -40f) "Breathing" else "Quiet"
            LiveStat(label = "Mic level",  value = "%.0f dB".format(liveDbfs))
            LiveStat(label = "Status",     value = dbLabel)
            LiveStat(label = "Snore dB",   value = state.liveSnoreDb?.let { "%.0f dB".format(it) } ?: "—")
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick  = { viewModel.stopRecording(); onStop() },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape    = RoundedCornerShape(12.dp),
            colors   = ButtonDefaults.buttonColors(containerColor = colors.red)
        ) {
            Text(
                text       = "Stop recording",
                fontFamily = DmSans,
                fontWeight = FontWeight.Medium,
                fontSize   = 15.sp
            )
        }

            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AmbientSleepParticles() {
    val colors = SleepSenseTheme.colors
    val transition = rememberInfiniteTransition(label = "sleep_particles")
    val drift by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(5200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particleDrift"
    )
    Canvas(modifier = Modifier.fillMaxSize()) {
        repeat(14) { index ->
            val baseX = ((index * 73) % 100) / 100f
            val baseY = ((index * 41) % 100) / 100f
            val wobble = sin((drift * 6.28f) + index) * 18.dp.toPx()
            val x = baseX * size.width + wobble
            val y = ((baseY + drift * (0.08f + index * 0.002f)) % 1f) * size.height
            val radius = (2 + index % 3).dp.toPx()
            drawCircle(
                color = if (index % 3 == 0) colors.blue.copy(alpha = 0.14f) else colors.purple.copy(alpha = 0.18f),
                radius = radius,
                center = Offset(x.coerceIn(0f, size.width), y)
            )
        }
    }
}

@Composable
private fun LiveStat(label: String, value: String) {
    val colors = SleepSenseTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, fontFamily = DmSerifDisplay, fontSize = 20.sp, color = colors.textPrimary)
        Text(text = label, fontFamily = JetBrainsMono, fontSize = 10.sp, color = colors.textMuted)
    }
}

private fun formatElapsed(ms: Long): String {
    val h  = ms / 3_600_000
    val m  = (ms % 3_600_000) / 60_000
    val s  = (ms % 60_000) / 1_000
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
