package com.circadianx.sleepsense.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.BuildConfig
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.SpotifyViewModel

@Composable
fun SpotifyScreen(
    onBack: () -> Unit,
    viewModel: SpotifyViewModel = hiltViewModel()
) {
    val colors = SleepSenseTheme.colors
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.consumePendingAuthCode(BuildConfig.SPOTIFY_REDIRECT_URI)
    }

    LaunchedEffect(state.authorizeUrl) {
        val url = state.authorizeUrl ?: return@LaunchedEffect
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
    ) {
        SsTopBar(tag = "Integrations", title = "Spotify")

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Connect Spotify to use music in your wind-down routines.",
                fontFamily = DmSans,
                color = colors.textSecondary,
                fontSize = 14.sp
            )

            Button(
                onClick = {
                    viewModel.buildAuthorizeUrl(
                        clientId = BuildConfig.SPOTIFY_CLIENT_ID,
                        redirectUri = BuildConfig.SPOTIFY_REDIRECT_URI
                    )
                },
                enabled = !state.isLoading,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = colors.purple)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(strokeWidth = 2.dp, color = colors.bgDeep)
                } else {
                    Text("Connect Spotify", fontFamily = DmSans, fontSize = 15.sp)
                }
            }

            if (state.connected) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colors.bgCard, RoundedCornerShape(16.dp))
                        .padding(Spacing.l),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Connected", fontFamily = DmSans, color = colors.textPrimary)
                    Text(
                        "Open a wind-down playlist in Spotify.",
                        fontFamily = DmSans,
                        color = colors.textMuted,
                        fontSize = 12.sp
                    )
                    Button(
                        onClick = {
                            if (state.windDownLink.isNullOrBlank()) {
                                viewModel.loadWindDownLink()
                            } else {
                                context.startActivity(
                                    Intent(Intent.ACTION_VIEW, Uri.parse(state.windDownLink))
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().height(48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = colors.bgBase)
                    ) {
                        Text("Open wind-down playlist", fontFamily = DmSans, color = colors.textPrimary)
                    }
                }
            }

            if (!state.error.isNullOrBlank()) {
                Text(state.error.orEmpty(), fontFamily = DmSans, color = colors.red, fontSize = 12.sp)
            }

            Spacer(Modifier.height(Spacing.l))
        }
    }
}

