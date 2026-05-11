package com.circadianx.sleepsense.viewmodel

import android.content.Context
import android.graphics.BitmapFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.circadianx.sleepsense.data.local.db.dao.ProgressPhotoDao
import com.circadianx.sleepsense.data.local.db.entity.ProgressPhotoEntity
import com.circadianx.sleepsense.data.local.security.PhotoCipher
import com.circadianx.sleepsense.util.Base64Utils
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.time.LocalDate
import javax.inject.Inject

data class ProgressPhotosUiState(
    val photos: List<ProgressPhotoEntity> = emptyList()
)

@HiltViewModel
class ProgressPhotosViewModel @Inject constructor(
    private val dao: ProgressPhotoDao,
    @ApplicationContext private val context: Context
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProgressPhotosUiState())
    val uiState: StateFlow<ProgressPhotosUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            dao.observeAll().collectLatest { list ->
                _uiState.update { it.copy(photos = list) }
            }
        }
    }

    fun saveEncryptedPhoto(jpegBytes: ByteArray, challengeId: Long? = null) {
        viewModelScope.launch {
            val encrypted = PhotoCipher.encrypt(jpegBytes)
            val fileName = "p_${System.currentTimeMillis()}.bin"
            val dir = File(context.filesDir, "progress_photos").apply { mkdirs() }
            File(dir, fileName).writeBytes(encrypted.ciphertext)

            dao.insert(
                ProgressPhotoEntity(
                    challengeId = challengeId,
                    takenAtMs = System.currentTimeMillis(),
                    epochDay = LocalDate.now().toEpochDay(),
                    fileName = fileName,
                    ivBase64 = Base64Utils.encode(encrypted.iv)
                )
            )
        }
    }

    fun loadDecryptedJpeg(photo: ProgressPhotoEntity): ByteArray? {
        val dir = File(context.filesDir, "progress_photos")
        val file = File(dir, photo.fileName)
        if (!file.exists()) return null
        val cipherBytes = file.readBytes()
        val iv = Base64Utils.decode(photo.ivBase64)
        return PhotoCipher.decrypt(cipherBytes, iv)
    }

    fun canDecode(photo: ProgressPhotoEntity): Boolean {
        val bytes = loadDecryptedJpeg(photo) ?: return false
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size) != null
    }
}

