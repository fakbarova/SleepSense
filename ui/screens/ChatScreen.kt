package com.circadianx.sleepsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.ChatMessage
import com.circadianx.sleepsense.viewmodel.ChatViewModel

private val suggestedPrompts = listOf(
    "Why did I sleep badly last night?",
    "How can I improve my sleep score?",
    "What are signs of sleep apnea?",
    "Tips for a better bedtime routine"
)

@Composable
fun ChatScreen(viewModel: ChatViewModel = hiltViewModel()) {
    val uiState   by viewModel.uiState.collectAsStateWithLifecycle()
    val colors    = SleepSenseTheme.colors
    val listState = rememberLazyListState()
    var input     by remember { mutableStateOf("") }

    // Auto-scroll to latest message
    LaunchedEffect(uiState.messages.size) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.bgDeep)
            .imePadding()
    ) {
        SsTopBar(tag = "AI Chat", title = "Ask SleepSense")

        // Backend offline banner
        if (uiState.backendReachable == false) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.red.copy(alpha = 0.15f))
                    .border(1.dp, colors.red.copy(alpha = 0.3f), RoundedCornerShape(0.dp))
                    .padding(horizontal = Spacing.xl, vertical = Spacing.s),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                Icon(Icons.Filled.WifiOff, null, tint = colors.red, modifier = Modifier.size(14.dp))
                Text(
                    text       = "Backend not reachable · start the server then retry",
                    fontFamily = DmSans,
                    fontSize   = 11.sp,
                    color      = colors.red
                )
            }
        }

        // Messages
        LazyColumn(
            state           = listState,
            modifier        = Modifier.weight(1f),
            contentPadding  = PaddingValues(Spacing.screenHorizontal, Spacing.l),
            verticalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            if (uiState.messages.isEmpty()) {
                item { WelcomeHint() }
            }
            items(uiState.messages) { msg -> ChatBubble(message = msg) }
            if (uiState.isLoading) {
                item { TypingIndicator() }
            }
        }

        // Suggested prompts (only before first message)
        if (uiState.messages.isEmpty() && !uiState.isLoading) {
            LazyRow(
                contentPadding        = PaddingValues(horizontal = Spacing.screenHorizontal, vertical = Spacing.s),
                horizontalArrangement = Arrangement.spacedBy(Spacing.s)
            ) {
                items(suggestedPrompts) { prompt ->
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(colors.bgCard)
                            .border(1.dp, colors.border, CircleShape)
                            .clickable { input = prompt }
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text       = prompt,
                            fontFamily = DmSans,
                            fontSize   = 12.sp,
                            color      = colors.textSecondary
                        )
                    }
                }
            }
        }

        // Input bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(colors.bgBase)
                .navigationBarsPadding()
                .padding(horizontal = Spacing.screenHorizontal, vertical = Spacing.m),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.m)
        ) {
            OutlinedTextField(
                value         = input,
                onValueChange = { input = it },
                modifier      = Modifier.weight(1f),
                placeholder   = { Text("Ask something…", fontFamily = DmSans, fontSize = 14.sp) },
                singleLine    = true,
                enabled       = !uiState.isLoading,
                shape         = RoundedCornerShape(20.dp),
                colors        = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colors.purple,
                    unfocusedBorderColor = colors.border,
                    focusedTextColor     = colors.textPrimary,
                    unfocusedTextColor   = colors.textPrimary,
                    disabledTextColor    = colors.textMuted,
                    cursorColor          = colors.purple
                )
            )

            val canSend = input.trim().isNotEmpty() && !uiState.isLoading
            IconButton(
                onClick  = {
                    if (canSend) {
                        viewModel.sendMessage(input.trim())
                        input = ""
                    }
                },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (canSend) colors.purple else colors.bgCard)
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send",
                    tint               = if (canSend) androidx.compose.ui.graphics.Color.White
                                         else colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun WelcomeHint() {
    val colors = SleepSenseTheme.colors
    Column(
        modifier            = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(colors.purple.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector        = Icons.Filled.SmartToy,
                contentDescription = null,
                tint               = colors.purple,
                modifier           = Modifier.size(32.dp)
            )
        }
        Spacer(Modifier.height(Spacing.l))
        Text(
            text       = "SleepSense AI",
            fontFamily = DmSans,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 16.sp,
            color      = colors.textPrimary
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text       = "Ask me anything about your sleep data,\nhabits, or health insights.",
            fontFamily = DmSans,
            fontSize   = 13.sp,
            color      = colors.textMuted,
            textAlign  = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun ChatBubble(message: ChatMessage) {
    val colors = SleepSenseTheme.colors
    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = if (message.isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment     = Alignment.Bottom
    ) {
        if (!message.isUser) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(
                        if (message.isError) colors.red.copy(alpha = 0.15f)
                        else colors.purple.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.SmartToy,
                    contentDescription = null,
                    tint               = if (message.isError) colors.red else colors.purple,
                    modifier           = Modifier.size(14.dp)
                )
            }
            Spacer(Modifier.width(Spacing.s))
        }

        val bubbleBg = when {
            message.isUser  -> colors.purple
            message.isError -> colors.red.copy(alpha = 0.12f)
            else            -> colors.bgCard
        }
        val bubbleBorder = when {
            message.isUser  -> colors.purple
            message.isError -> colors.red.copy(alpha = 0.4f)
            else            -> colors.border
        }
        val textColor = when {
            message.isUser  -> androidx.compose.ui.graphics.Color.White
            message.isError -> colors.red
            else            -> colors.textPrimary
        }

        val shape = RoundedCornerShape(
            topStart    = 16.dp,
            topEnd      = 16.dp,
            bottomStart = if (message.isUser) 16.dp else 4.dp,
            bottomEnd   = if (message.isUser) 4.dp else 16.dp
        )

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(shape)
                .background(bubbleBg)
                .border(1.dp, bubbleBorder, shape)
                .padding(horizontal = Spacing.l, vertical = Spacing.m)
        ) {
            Text(
                text       = message.text,
                fontFamily = DmSans,
                fontSize   = 14.sp,
                color      = textColor,
                lineHeight = 21.sp
            )
        }
    }
}

@Composable
private fun TypingIndicator() {
    val colors = SleepSenseTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        CircularProgressIndicator(
            modifier    = Modifier.size(14.dp),
            color       = colors.purple,
            strokeWidth = 2.dp
        )
        Spacer(Modifier.width(Spacing.s))
        Text(
            text       = "SleepSense is thinking…",
            fontFamily = JetBrainsMono,
            fontSize   = 10.sp,
            color      = colors.textMuted
        )
    }
}
