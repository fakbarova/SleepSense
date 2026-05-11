package com.circadianx.sleepsense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.local.db.dao.SocialDao
import com.circadianx.sleepsense.data.local.db.entity.GroupChallengeEntity
import com.circadianx.sleepsense.data.local.db.entity.StoryEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SocialUiState(
    val groups: List<GroupChallengeEntity> = emptyList(),
    val stories: List<StoryEntity> = emptyList()
)

@HiltViewModel
class SocialViewModel @Inject constructor(
    private val dao: SocialDao
) : ViewModel() {
    private val _uiState = MutableStateFlow(SocialUiState())
    val uiState: StateFlow<SocialUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.observeGroups().collectLatest { groups ->
                _uiState.update { it.copy(groups = groups) }
            }
        }
        viewModelScope.launch {
            dao.observeStories().collectLatest { stories ->
                _uiState.update { it.copy(stories = stories) }
            }
        }
    }

    fun createGroup(title: String) {
        val t = title.trim()
        if (t.isEmpty()) return
        viewModelScope.launch {
            dao.insertGroup(
                GroupChallengeEntity(
                    title = t,
                    createdAtMs = System.currentTimeMillis()
                )
            )
        }
    }

    fun publishStory(title: String, body: String) {
        val t = title.trim()
        val b = body.trim()
        if (t.isEmpty() || b.isEmpty()) return
        viewModelScope.launch {
            dao.insertStory(
                StoryEntity(
                    title = t,
                    body = b,
                    createdAtMs = System.currentTimeMillis()
                )
            )
        }
    }
}

