package com.circadianx.sleepsense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.local.db.dao.SleepRecordDao
import com.circadianx.sleepsense.data.local.db.dao.StepDao
import com.circadianx.sleepsense.data.network.ChatContext
import com.circadianx.sleepsense.data.network.ChatHabitsContext
import com.circadianx.sleepsense.data.network.ChatRepository
import com.circadianx.sleepsense.data.network.ChatSleepContext
import com.circadianx.sleepsense.data.network.ChatStepsContext
import com.circadianx.sleepsense.domain.repository.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val isError: Boolean = false
)

data class ChatUiState(
    val messages: List<ChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val backendReachable: Boolean? = null   // null = not yet checked
)

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val chatRepository: ChatRepository,
    private val sleepRecordDao: SleepRecordDao,
    private val stepDao: StepDao,
    private val prefs: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        checkBackend()
    }

    private fun checkBackend() {
        viewModelScope.launch {
            val reachable = chatRepository.isReachable()
            _uiState.value = _uiState.value.copy(backendReachable = reachable)
        }
    }

    fun sendMessage(question: String) {
        if (question.isBlank()) return

        val userMsg = ChatMessage(text = question, isUser = true)
        _uiState.value = _uiState.value.copy(
            messages  = _uiState.value.messages + userMsg,
            isLoading = true
        )

        viewModelScope.launch {
            val context = buildContext()
            val result  = chatRepository.sendMessage(question, context)

            val reply = result.fold(
                onSuccess = { answer -> ChatMessage(text = answer, isUser = false) },
                onFailure = { e ->
                    ChatMessage(
                        text    = "Couldn't reach SleepSense AI. Make sure the backend is running.\n(${e.message})",
                        isUser  = false,
                        isError = true
                    )
                }
            )

            _uiState.value = _uiState.value.copy(
                messages  = _uiState.value.messages + reply,
                isLoading = false,
                backendReachable = result.isSuccess
            )
        }
    }

    private suspend fun buildContext(): ChatContext {
        val recentSleeps = sleepRecordDao.observeRecent(14).first()
        val recentSteps  = stepDao.observeRecent(7).first()
        val goals        = prefs.primaryGoals.first().toList()

        val sleepCtx = if (recentSleeps.isEmpty()) null else ChatSleepContext(
            recentScores           = recentSleeps.map { it.sleepScore },
            recentDurationsMinutes = recentSleeps.map {
                TimeUnit.MILLISECONDS.toMinutes(it.endTimeMs - it.startTimeMs)
            },
            recentDisturbances     = recentSleeps.map { it.disturbanceCount }
        )

        val stepsCtx = if (recentSteps.isEmpty()) null else ChatStepsContext(
            recentSteps = recentSteps.map { it.steps }
        )

        return ChatContext(
            sleep  = sleepCtx,
            steps  = stepsCtx,
            habits = ChatHabitsContext(),   // TODO: wire routine completion rate
            goals  = goals
        )
    }
}
