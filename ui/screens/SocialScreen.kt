package com.circadianx.sleepsense.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.StickyNote2
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.circadianx.sleepsense.data.local.db.entity.GroupChallengeEntity
import com.circadianx.sleepsense.data.local.db.entity.StoryEntity
import com.circadianx.sleepsense.ui.components.SsEmptyState
import com.circadianx.sleepsense.ui.components.SsTopBar
import com.circadianx.sleepsense.ui.theme.DmSans
import com.circadianx.sleepsense.ui.theme.JetBrainsMono
import com.circadianx.sleepsense.ui.theme.SleepSenseTheme
import com.circadianx.sleepsense.ui.theme.Spacing
import com.circadianx.sleepsense.viewmodel.SocialViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(viewModel: SocialViewModel = hiltViewModel()) {
    val state      by viewModel.uiState.collectAsStateWithLifecycle()
    val colors     = SleepSenseTheme.colors
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var selectedTab by remember { mutableIntStateOf(0) }
    var showSheet   by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = colors.bgDeep,
        floatingActionButton = {
            FloatingActionButton(
                onClick        = { showSheet = true },
                containerColor = colors.purple,
                contentColor   = androidx.compose.ui.graphics.Color.White,
                modifier       = Modifier.navigationBarsPadding()
            ) {
                Icon(Icons.Filled.Add, contentDescription = "New post")
            }
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.bgDeep)
                .padding(inner)
        ) {
            SsTopBar(tag = "Social", title = "Groups & Stories")

            TabRow(
                selectedTabIndex = selectedTab,
                containerColor   = colors.bgDeep,
                contentColor     = colors.purple,
                indicator        = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier
                            .tabIndicatorOffset(tabPositions[selectedTab])
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)),
                        color    = colors.purple
                    )
                },
                divider = {}
            ) {
                listOf("Groups", "Stories").forEachIndexed { index, label ->
                    Tab(
                        selected = selectedTab == index,
                        onClick  = { selectedTab = index },
                        text     = {
                            Text(
                                text       = label,
                                fontFamily = DmSans,
                                fontWeight = if (selectedTab == index) FontWeight.SemiBold else FontWeight.Normal,
                                fontSize   = 14.sp,
                                color      = if (selectedTab == index) colors.purple else colors.textMuted
                            )
                        }
                    )
                }
            }

            LazyColumn(
                modifier        = Modifier.fillMaxSize(),
                contentPadding  = PaddingValues(
                    horizontal = Spacing.screenHorizontal,
                    vertical   = Spacing.l
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.m)
            ) {
                if (selectedTab == 0) {
                    if (state.groups.isEmpty()) {
                        item {
                            SsEmptyState(
                                icon     = Icons.Filled.Groups,
                                title    = "No groups yet",
                                body     = "Tap + to create a group challenge and invite friends.",
                                ctaLabel = "Create group",
                                onCta    = { showSheet = true }
                            )
                        }
                    } else {
                        items(state.groups, key = { it.id }) { g ->
                            GroupCard(g)
                        }
                    }
                } else {
                    if (state.stories.isEmpty()) {
                        item {
                            SsEmptyState(
                                icon     = Icons.Filled.StickyNote2,
                                title    = "No stories yet",
                                body     = "Share what helped you sleep better — inspire the community.",
                                ctaLabel = "Share a story",
                                onCta    = { showSheet = true }
                            )
                        }
                    } else {
                        items(state.stories, key = { it.id }) { s ->
                            StoryCard(s)
                        }
                    }
                }
                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    if (showSheet) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
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
            if (selectedTab == 0) {
                CreateGroupContent(
                    onDismiss = { showSheet = false },
                    onCreate  = { title -> viewModel.createGroup(title); showSheet = false }
                )
            } else {
                CreateStoryContent(
                    onDismiss = { showSheet = false },
                    onCreate  = { title, body -> viewModel.publishStory(title, body); showSheet = false }
                )
            }
        }
    }
}

@Composable
private fun GroupCard(group: GroupChallengeEntity) {
    val colors = SleepSenseTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(Spacing.l),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(colors.purple.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.Filled.People,
                    contentDescription = null,
                    tint               = colors.purple,
                    modifier           = Modifier.size(18.dp)
                )
            }
            Column(modifier = Modifier.padding(start = Spacing.m)) {
                Text(
                    text       = group.title,
                    fontFamily = DmSans,
                    fontWeight = FontWeight.SemiBold,
                    fontSize   = 14.sp,
                    color      = colors.textPrimary
                )
                Text(
                    text       = "Local group",
                    fontFamily = JetBrainsMono,
                    fontSize   = 10.sp,
                    color      = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun StoryCard(story: StoryEntity) {
    val colors = SleepSenseTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(colors.bgCard)
            .border(1.dp, colors.border, RoundedCornerShape(14.dp))
            .padding(Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.s)
    ) {
        Text(
            text       = story.title,
            fontFamily = DmSans,
            fontWeight = FontWeight.SemiBold,
            fontSize   = 15.sp,
            color      = colors.textPrimary
        )
        Text(
            text       = story.body,
            fontFamily = DmSans,
            fontSize   = 13.sp,
            color      = colors.textSecondary,
            lineHeight = 20.sp
        )
    }
}

@Composable
private fun CreateGroupContent(onDismiss: () -> Unit, onCreate: (String) -> Unit) {
    val colors = SleepSenseTheme.colors
    var title  by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Text("New Group", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.textPrimary)

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Group name", fontFamily = DmSans) },
            modifier = Modifier.fillMaxWidth(),
            colors   = fieldColors(colors)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp)) {
                Text("Cancel", fontFamily = DmSans, color = colors.textMuted)
            }
            Button(
                onClick  = { onCreate(title) },
                enabled  = title.trim().isNotEmpty(),
                modifier = Modifier.weight(1f).height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = colors.purple)
            ) {
                Text("Create", fontFamily = DmSans, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(Spacing.l))
    }
}

@Composable
private fun CreateStoryContent(onDismiss: () -> Unit, onCreate: (String, String) -> Unit) {
    val colors = SleepSenseTheme.colors
    var title  by remember { mutableStateOf("") }
    var body   by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = Spacing.xl, vertical = Spacing.l),
        verticalArrangement = Arrangement.spacedBy(Spacing.m)
    ) {
        Text("Share a Story", fontFamily = DmSans, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = colors.textPrimary)

        OutlinedTextField(
            value = title, onValueChange = { title = it },
            label = { Text("Title", fontFamily = DmSans) },
            modifier = Modifier.fillMaxWidth(),
            colors   = fieldColors(colors)
        )
        OutlinedTextField(
            value = body, onValueChange = { body = it },
            label = { Text("What worked for you?", fontFamily = DmSans) },
            modifier = Modifier.fillMaxWidth().height(120.dp),
            maxLines = 5,
            colors   = fieldColors(colors)
        )

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Spacing.m)) {
            TextButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(48.dp)) {
                Text("Cancel", fontFamily = DmSans, color = colors.textMuted)
            }
            Button(
                onClick  = { onCreate(title, body) },
                enabled  = title.trim().isNotEmpty() && body.trim().isNotEmpty(),
                modifier = Modifier.weight(1f).height(48.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = colors.purple)
            ) {
                Text("Publish", fontFamily = DmSans, fontWeight = FontWeight.Medium)
            }
        }
        Spacer(Modifier.height(Spacing.l))
    }
}

@Composable
private fun fieldColors(colors: com.circadianx.sleepsense.ui.theme.SleepSenseColors) =
    OutlinedTextFieldDefaults.colors(
        focusedBorderColor   = colors.purple,
        unfocusedBorderColor = colors.border,
        focusedLabelColor    = colors.purple,
        unfocusedLabelColor  = colors.textMuted,
        focusedTextColor     = colors.textPrimary,
        unfocusedTextColor   = colors.textPrimary,
        cursorColor          = colors.purple
    )
