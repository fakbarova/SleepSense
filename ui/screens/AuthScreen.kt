package com.circadianx.sleepsense.ui.screens

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.DmSerifDisplay
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    onSignedIn: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val colors = SleepSenseTheme.colors
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.isSignedIn) {
        if (state.isSignedIn) {
            onSignedIn()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
            .statusBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "SleepSense",
            fontFamily = DmSerifDisplay,
            fontSize = 32.sp,
            color = colors.purple
        )
        Spacer(Modifier.height(Spacing.l))
        Text(
            text = "Sign in to sync your habits and challenges across devices.",
            fontFamily = DmSans,
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            color = colors.textSecondary
        )

        Spacer(Modifier.height(Spacing.xl))
        Button(
            onClick = {
                val activity = context as? Activity ?: return@Button
                viewModel.signInWithGoogle(activity)
            },
            enabled = !state.isLoading,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
        ) {
            if (state.isLoading) {
                CircularProgressIndicator(
                    strokeWidth = 2.dp,
                    modifier = Modifier.height(18.dp),
                    color = colors.bgDeep
                )
            } else {
                Text(
                    text = "Continue with Google",
                    fontFamily = DmSans,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        if (!state.error.isNullOrBlank()) {
            Spacer(Modifier.height(Spacing.m))
            Text(
                text = state.error.orEmpty(),
                fontFamily = DmSans,
                color = colors.red,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}
