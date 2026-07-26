package com.expenseai.manager.presentation.ocr

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.common.InputImage
import com.expenseai.manager.util.OCRProcessor
import com.expenseai.manager.util.OCRResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OCRUiState(
    val isProcessing: Boolean = false,
    val result: OCRResult? = null,
    val error: String? = null,
    val capturedImageUri: Uri? = null
)

@HiltViewModel
class OCRViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(OCRUiState())
    val uiState: StateFlow<OCRUiState> = _uiState.asStateFlow()

    private val processor = OCRProcessor()

    fun processImage(image: InputImage, imageUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, error = null, capturedImageUri = imageUri) }
            try {
                val result = processor.processImage(image)
                _uiState.update { it.copy(isProcessing = false, result = result) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isProcessing = false, error = "OCR failed: ${e.message}") }
            }
        }
    }

    fun clearResult() = _uiState.update { it.copy(result = null, error = null, capturedImageUri = null) }

    override fun onCleared() {
        super.onCleared()
        processor.release()
    }
}
